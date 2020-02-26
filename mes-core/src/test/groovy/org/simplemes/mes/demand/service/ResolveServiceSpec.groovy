package org.simplemes.mes.demand.service

import org.simplemes.eframe.application.Holders
import org.simplemes.eframe.exception.BusinessException
import org.simplemes.eframe.test.BaseSpecification
import org.simplemes.eframe.test.UnitTestUtils
import org.simplemes.eframe.test.annotation.Rollback
import org.simplemes.mes.demand.LSNTrackingOption
import org.simplemes.mes.demand.OrderReleaseRequest
import org.simplemes.mes.demand.ResolveIDRequest
import org.simplemes.mes.demand.ResolveWorkableRequest
import org.simplemes.mes.demand.StartRequest
import org.simplemes.mes.demand.domain.LSNSequence
import org.simplemes.mes.demand.domain.Order
import org.simplemes.mes.product.domain.Product
import org.simplemes.mes.test.MESUnitTestUtils
import org.simplemes.mes.tracking.domain.ActionLog
import org.simplemes.mes.tracking.domain.ProductionLog

/*
 * Copyright Michael Houston 2018. All rights reserved.
 * Original Author: mph
 *
*/

/**
 * Tests the non-routing related scenarios for the Resolve Service actions.
 */
class ResolveServiceSpec extends BaseSpecification {
  @SuppressWarnings("unused")
  static dirtyDomains = [ActionLog, ProductionLog, Order, Product, LSNSequence]

  ResolveService resolveService

  def setup() {
    setCurrentUser()
    resolveService = Holders.getBean(ResolveService)
  }

  @Rollback
  def "test resolveWorkable when using order-only processing"() {
    given: 'a released order'
    def order = MESUnitTestUtils.releaseOrder(lsnTrackingOption: LSNTrackingOption.ORDER_ONLY, qty: 2)

    when: 'the resolve is attempted'
    ResolveWorkableRequest req = new ResolveWorkableRequest(order: order)

    then: 'the correct order is returned'
    resolveService.resolveWorkable(req)[0] == order
  }

  @Rollback
  def "test resolveWorkable when using when using LSN-based processing"() {
    given: 'a released order'
    def order = MESUnitTestUtils.releaseOrder(lsnTrackingOption: LSNTrackingOption.LSN_ONLY, qty: 1)

    when: 'the resolve is attempted'
    ResolveWorkableRequest req = new ResolveWorkableRequest(lsn: order.lsns[0])

    then: 'the correct LSN is returned'
    resolveService.resolveWorkable(req)[0] == order.lsns[0]
    resolveService.resolveWorkable(req).size() == 1
  }

  def "test resolveWorkable when using when passing order and LSN in the request for LSN-based processing"() {
    given: 'a released order'
    Order order = null
    Order.withTransaction {
      Product product = new Product(product: 'PC', /*lsnSequence: LSNSequence.findDefaultSequence(),*/
                                    lsnTrackingOption: LSNTrackingOption.LSN_ALLOWED).save()
      order = new Order(order: '1234', qtyToBuild: 5.0, product: product).save()
      order.populateLSNs()  // Must populate LSNs manually for LSN_ALLOWED case.
      order.save()
      //println "order = $order"
      new OrderService().release(new OrderReleaseRequest(order: order))
    }

    expect: 'the correct LSN is returned'
    Order.withTransaction {
      def order2 = Order.findByOrder('1234')
      ResolveWorkableRequest req = new ResolveWorkableRequest(lsn: order2.lsns[0], order: order2)
      assert resolveService.resolveWorkable(req)[0] == order.lsns[0]
      true
    }
  }

  @Rollback
  def "test resolveWorkable with combination of LSN/Order with only LSN allowed"() {
    given: 'a released order'
    def order = MESUnitTestUtils.releaseOrder(qtyToBuild: 1.0, lsnTrackingOption: LSNTrackingOption.LSN_ONLY)

    when: 'the resolve is attempted'
    ResolveWorkableRequest req = new ResolveWorkableRequest(lsn: order.lsns[0], order: order)

    then: 'the correct LSN is returned'
    resolveService.resolveWorkable(req)[0] == order.lsns[0]
  }

  @Rollback
  def "test resolveWorkable with null input"() {
    when: 'method is called with null'
    resolveService.resolveWorkable(null)

    then: 'fails with an exception'
    def e = thrown(BusinessException)
    UnitTestUtils.assertContainsAllIgnoreCase(e.toString(), ['request'])
  }

  @Rollback
  def "test resolveWorkable with empty request"() {
    when: 'method is called with null'
    resolveService.resolveWorkable(new ResolveWorkableRequest())

    then: 'fails with an exception'
    def e = thrown(BusinessException)
    UnitTestUtils.assertContainsAllIgnoreCase(e.toString(), ['order'])
  }

  @Rollback
  def "test resolveProductionRequest with a StartRequest and an order barcode ID"() {
    given: 'a released order'
    def order = MESUnitTestUtils.releaseOrder(id: 'RS', qtyToBuild: 5.0, lsnTrackingOption: LSNTrackingOption.ORDER_ONLY)

    when: 'the resolve is attempted'
    def req = new StartRequest(barcode: order.order)
    resolveService.resolveProductionRequest(req)

    then: 'the request is updated with the resolved info'
    req.order == order
  }

  @Rollback
  def "test resolveProductionRequest with a StartRequest and an LSN barcode ID"() {
    given: 'a released order'
    def order = MESUnitTestUtils.releaseOrder(id: 'RS', qtyToBuild: 1.0, lsnTrackingOption: LSNTrackingOption.LSN_ONLY)

    when: 'the resolve is attempted'
    def req = new StartRequest(barcode: order.lsns[0].lsn)
    resolveService.resolveProductionRequest(req)

    then: 'the request is updated with the resolved info'
    req.order == order
    req.lsn == order.lsns[0]
  }

  @Rollback
  def "test resolveProductionRequest with a barcode and LSN and order are use same ID"() {
    given: 'a released order with matching LSN'
    def order = MESUnitTestUtils.releaseOrder(id: 'RS', qtyToBuild: 1.0, lsnTrackingOption: LSNTrackingOption.LSN_ONLY,
                                              lsns: ['M1000'])
    // Make sure we created LSNs that match.
    assert order.order == order.lsns[0].lsn

    when: 'the resolve is attempted'
    def req = new StartRequest(barcode: order.lsns[0].lsn)
    resolveService.resolveProductionRequest(req)

    then: 'the request is updated with the resolved info'
    req.order == order
    req.lsn == order.lsns[0]
  }

  @Rollback
  def "test resolveProductionRequest with a barcode and more than one matching LSN"() {
    given: 'two released orders with two matching LSN'
    def order1 = MESUnitTestUtils.releaseOrder(id: 'RS', qty: 1.0, lsnTrackingOption: LSNTrackingOption.LSN_ONLY)
    def order2 = MESUnitTestUtils.releaseOrder(id: 'RS2', qty: 1.0, lsnTrackingOption: LSNTrackingOption.LSN_ONLY)

    and: 'LSNs that match'
    order2.lsns[0].lsn = order1.lsns[0].lsn
    order2.save()
    // Make sure we two LSNs that match.
    assert order2.lsns[0].lsn == order1.lsns[0].lsn

    when: 'the resolve is attempted'
    def req = new StartRequest(barcode: order1.lsns[0].lsn)
    resolveService.resolveProductionRequest(req)

    then: 'an exception is thrown'
    def ex = thrown(BusinessException)
    //error.3011.message=More than one LSN matches "{0}".  {1} LSNs exist with the same ID.
    UnitTestUtils.assertExceptionIsValid(ex, ['LSN', order1.lsns[0].lsn], 3011)
  }

  @Rollback
  def "test resolveProductionRequest with a barcode that matches no LSN or Order"() {
    when: 'the resolve is attempted on non-existent LSN/Order'
    def req = new StartRequest(barcode: 'gibberish')
    resolveService.resolveProductionRequest(req)

    then: 'an exception is thrown'
    def ex = thrown(BusinessException)
    // error.3012.message=No Orders or LSNs found for the input {0}.
    UnitTestUtils.allParamsHaveValues(ex)
    UnitTestUtils.assertContainsAllIgnoreCase(ex.toString(), ['LSN', 'gibberish'])
    ex.code == 3012
  }

  @Rollback
  def "test resolveID with duplicate LSN - gracefully fails"() {
    given: 'create two LSNs with the same ID'
    def order1 = MESUnitTestUtils.releaseOrder(lsnTrackingOption: LSNTrackingOption.LSN_ONLY, lsns: ['SNX001'])
    MESUnitTestUtils.releaseOrder(lsnTrackingOption: LSNTrackingOption.LSN_ONLY, lsns: ['SNX001'])

    when: 'the resolve is attempted'
    resolveService.resolveID(new ResolveIDRequest(barcode: order1.lsns[0].lsn))

    then: 'an exception is thrown'
    def ex = thrown(BusinessException)
    //error.3011.message=More than one LSN matches "{0}".  {1} LSNs exist with the same ID.
    UnitTestUtils.assertExceptionIsValid(ex, ['LSN', order1.lsns[0].lsn, '2 LSNs'], 3011)
  }


}

