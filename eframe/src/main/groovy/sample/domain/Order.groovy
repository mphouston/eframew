/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package sample.domain

import com.fasterxml.jackson.annotation.JsonFilter
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.micronaut.data.annotation.AutoPopulated
import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import org.simplemes.eframe.data.annotation.ExtensibleFieldHolder
import org.simplemes.eframe.date.DateOnly
import org.simplemes.eframe.domain.annotation.DomainEntity
import org.simplemes.eframe.system.BasicStatus
import org.simplemes.eframe.system.EnabledStatus

import io.micronaut.core.annotation.Nullable
import javax.persistence.Column
import javax.persistence.OneToMany

/**
 * A sample domain class that simulates an order.
 * Fields include: order, qtyToBuild, product, status, dueDate
 */
@MappedEntity('ordr')
@DomainEntity
@JsonFilter("searchableFilter")
@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['order'])
@SuppressWarnings("unused")
class Order {
  @Column(name = 'ordr', length = 30, nullable = false)
  String order
  BigDecimal qtyToBuild = 1.0

  @Nullable String product

  @Column(length = 12, nullable = true)
  BasicStatus status = EnabledStatus.instance

  @Nullable
  //@MappedProperty(type = DataType.DATE)
  DateOnly dueDate = new DateOnly()

  @DateCreated
  @MappedProperty(type = DataType.TIMESTAMP, definition = 'TIMESTAMP WITH TIME ZONE')
  Date dateCreated

  @DateUpdated
  @MappedProperty(type = DataType.TIMESTAMP, definition = 'TIMESTAMP WITH TIME ZONE')
  Date dateUpdated

  @OneToMany(mappedBy = "order")
  List<OrderLine> orderLines

  @Column(length = 800, nullable = true)
  // Uses nullable option on @Column for unit tests.  See PersistentPropertySpec.
  // Larger than the default for H2 to allow SQL error generation in unit tests.  See DomainEntityHelperSpec.
  String notes

  @Nullable
  @ExtensibleFieldHolder
  @MappedProperty(type = DataType.JSON)
  String customFields

  Integer version = 0

  @Id @AutoPopulated
  @MappedProperty(type = DataType.UUID)
  UUID uuid

  Order() {
  }

  Order(String order) {
    this.order = order
  }

  Order(Map options) {
    options.each { k, v ->
      //noinspection GroovyAssignabilityCheck
      this[k] = v
    }

  }

  Order(String order, UUID uuid) {
    this.order = order
    this.uuid = uuid
  }

  static fieldOrder = ['order', 'product', 'qtyToBuild', 'status', 'dueDate']

  /**
   * Searchable domain.
   */
  static searchable = true

  /**
   * Sample beforeValidate method.  Will alter the product is set to XYZZY.
   */
  def beforeValidate() {
    if (product == 'XYZZY') {
      product = "XYZZYAlteredByBeforeValidate"
    }
  }

  /**
   * Sample beforeSave method.  Will alter the product is set to XYZZY.
   */
  def beforeSave() {
    if (product == 'XYZZY') {
      product = "XYZZYAlteredByBeforeSave"
    }
  }

  /**
   * Sample beforeSave method.  Will alter the product is set to XYZZY.
   */
  def beforeDelete() {
    if (product == 'XYZZY') {
      product = "PDQAlteredByBeforeSave"
    }
  }

  /**
   * Builds a localized version of the order.
   * @param locale (ignored).
   * @return The order.
   */
  String toStringLocalized(Locale locale = null) {
    return getOrder()
  }
  /**
   * Load initial records - test data.
   */
/*
  @SuppressWarnings("UnnecessaryQualifiedReference")
  static Map<String, List<String>> initialDataLoad() {
    def products = ['BIKE-24','BIKE-27','SEAT','WHEEL','FRAME-24','FRAME-27']
    //def statuses = [EnabledStatus.instance, DisabledStatus.instance]
    Order.withTransaction {
      if (Order.list().size()==0) {
        def random = new Random()
        for (i in 1901..1902) {
          def date = new DateOnly(new DateOnly().time - DateUtils.MILLIS_PER_DAY * (300 - random.nextInt(300)))
          def order = new Order(order: "M$i",
                                product: products[random.nextInt(products.size())],
                                dueDate: date,
                                qtyToBuild: new BigDecimal(random.nextInt(99) + 1)
          )
          order.orderLines << new OrderLine(order: order, product: 'BIKE', sequence: 1)
          order.orderLines << new OrderLine(order: order, product: 'WHEEL', sequence: 2, qty: 2.0)
          order.save()
          for (orderLine in order.orderLines) {
            orderLine.order = order
            orderLine.save()
          }
        }
      }
      //println "order.list() = ${Order.list()*.dateCreated}"
    }
    return null
  }
*/


}
