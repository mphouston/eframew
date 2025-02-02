/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package sample.controller

import groovy.util.logging.Slf4j
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import org.simplemes.eframe.application.Holders
import org.simplemes.eframe.controller.BaseCrudRestController
import org.simplemes.eframe.controller.ControllerUtils
import org.simplemes.eframe.controller.StandardModelAndView
import org.simplemes.eframe.web.task.TaskMenuItem
import org.simplemes.eframe.web.ui.webix.ToolkitConstants
import sample.domain.Order
import sample.pogo.FindComponentResponseDetail
import sample.pogo.FindWorkResponse
import sample.pogo.FindWorkResponseDetail

import io.micronaut.core.annotation.Nullable
import java.security.Principal

/**
 * Handles the controller actions for the Order objects.  Includes CRUD actions on the user.
 * <p>
 * Not shipped with the framework.  Used for some GUI tests.
 */
@Slf4j
@Secured("MANAGER")
@Controller("/order")
@SuppressWarnings("unused")
class OrderController extends BaseCrudRestController {

  /**
   * The number of rows available for the dummy findWork() method.
   */
  public static final int WORK_RECORD_COUNT = 105

  static domainClass = Order

  /**
   * Defines the entry(s) in the main Task Menu.
   */
  def taskMenuItems = [new TaskMenuItem(folder: 'sample:9500', name: 'order', uri: '/order',
                                        displayOrder: 9710, clientRootActivity: true)]

  @Get("/orderWorkList")
  @Produces(MediaType.TEXT_HTML)
  StandardModelAndView orderWorkList(@Nullable Principal principal) {
    def res = new StandardModelAndView("sample/order/orderWorkList", principal, this)
    return res
  }

  /**
   * A static list of UUIDs for the dummy work list.
   */
  static List<UUID> workListUUIDs = []

  /**
   * Returns a list (JSON formatted) that contains a POGO list of values.  Dummy static list.
   * @param request The request.
   * @param principal The user logged in.
   * @return The data for the list.
   */
  @SuppressWarnings("GroovyAssignabilityCheck")
  @Get("/findWork")
  HttpResponse findWork(HttpRequest request, @Nullable Principal principal) {
    def params = ControllerUtils.instance.convertToMap(request.parameters)
    def (from, max) = ControllerUtils.instance.calculateFromAndSizeForList(params)
    def (String sortField, String sortDir) = ControllerUtils.instance.calculateSortingForList(params)
    sortField = sortField ?: 'order'
    sortDir = sortDir ?: 'asc'

    if (!workListUUIDs) {
      // Create some UUIDs that can be re-used for later calls.
      for (i in (1..WORK_RECORD_COUNT)) {
        workListUUIDs << UUID.randomUUID()
      }
    }

    // Build some dummy data
    def workList = []
    def products = ['BIKE-27', 'BIKE-24', 'BIKE-21']
    def workCenter = params.workCenter ?: ''
    def rng = new Random()
    for (i in (1..WORK_RECORD_COUNT)) {
      def qtyInQueue = 10.0 * rng.nextDouble() as BigDecimal
      def qtyInWork = 10.0 * rng.nextDouble() as BigDecimal
      def order = "M1${sprintf("%03d", i)}"
      def product = products[rng.nextInt(products.size())]
      workList << new FindWorkResponseDetail(qtyInQueue: qtyInQueue, order: order, qtyInWork: qtyInWork,
                                             product: product, workCenter: workCenter,
                                             id: workListUUIDs[i - 1])
    }

    // Now, sort the list as needed
    workList.sort { FindWorkResponseDetail a, FindWorkResponseDetail b ->
      if (sortDir == 'desc') {
        // Swap the elements to compare
        def x = b
        b = a
        a = x
      }
      def valueA = a[sortField]
      def valueB = b[sortField]
      if (valueA != null && valueB != null) {
        return valueA <=> valueB
      }
      return 0
    }

    def response = new FindWorkResponse()
    response.totalAvailable = workList.size()
    response.list = []

    // Find the right records
    int start = from * max
    for (int i = 0; i <= max; i++) {
      if (start + i < workList.size()) {
        response.list << (workList[start + i] as FindWorkResponseDetail)
      }
    }

    def json = Holders.objectMapper.writeValueAsString([data: response.list, total_count: response.totalAvailable,
                                                        pos : from * max, sort: sortField, sortDir: sortDir])
    return HttpResponse.status(HttpStatus.OK).body(json)
  }


  @Get("/orderComponentList")
  @Produces(MediaType.TEXT_HTML)
  StandardModelAndView orderComponentList(@Nullable Principal principal) {
    def res = new StandardModelAndView("sample/order/orderComponentList", principal, this)
    return res
  }

  /**
   * The dummy component list values.
   */
  static List<FindComponentResponseDetail> componentList

  /**
   * Returns a list (JSON formatted) that contains a POGO list of values.  Dummy static list.
   * @param request The request.
   * @param principal The user logged in.
   * @return The data for the list.
   */
  @SuppressWarnings("GroovyAssignabilityCheck")
  @Get("/findComponents")
  HttpResponse findComponents(HttpRequest request, @Nullable Principal principal) {
    def params = ControllerUtils.instance.convertToMap(request.parameters)
    def (from, max) = ControllerUtils.instance.calculateFromAndSizeForList(params)
    def (String sortField, String sortDir) = ControllerUtils.instance.calculateSortingForList(params)
    sortField = sortField ?: 'sequence'
    sortDir = sortDir ?: 'asc'

    // Build some dummy data
    def rng = new Random(1)
    def sequence = 10
    if (!componentList) {
      def components = ['SEAT', 'WHEEL-27', 'HANDLE BARS', '<b>BRAKE</b> <i>ASSEMBLY</i>']
      def qtyRequires = [1.0, 2.0, 1.0, 2.0]
      componentList = []
      for (int i = 0; i < components.size(); i++) {
        componentList << new FindComponentResponseDetail(component: components[i], qtyRequired: qtyRequires[i],
                                                         sequence: sequence, qtyAssembled: 0.0, canBeRemoved: false,
                                                         id: UUID.randomUUID())
        sequence += 10
      }
    }

    // Each time re-displayed, we change the assembled flag on each.
    for (component in componentList) {
      boolean assembled = rng.nextBoolean()
      component.qtyAssembled = assembled ? component.qtyRequired : 0.0
      component.canBeRemoved = assembled
    }

    // Now, sort the list as needed
    componentList.sort { FindComponentResponseDetail a, FindComponentResponseDetail b ->
      if (sortDir == 'desc') {
        // Swap the elements to compare
        def x = b
        b = a
        a = x
      }
      def valueA = a[sortField]
      def valueB = b[sortField]
      if (valueA != null && valueB != null) {
        return valueA <=> valueB
      }
      return 0
    }

    def json = Holders.objectMapper.writeValueAsString([data: componentList, total_count: componentList.size(),
                                                        pos : from * max, sort: sortField, sortDir: sortDir])
    return HttpResponse.status(HttpStatus.OK).body(json)
  }

  /**
   * The number of times the suggest url is called.
   */
  static int suggestCount = 0

  /**
   * The most recent parameters for the latest suggest request.
   */
  static Map suggestLatestParameters

  /**
   * Test method for a suggest list.  Also provides an instrumented endpoint for use with suggest API tests. Records
   * the parameters in the latest request and a count of the calls.
   * @param request The request.
   * @param principal The user logged in.
   * @return The data for the list.
   */
  @SuppressWarnings("GroovyAssignabilityCheck")
  @Get("/suggestOrder")
  HttpResponse suggestOrder(HttpRequest request, @Nullable Principal principal) {
    def list = []
    def params = ControllerUtils.instance.convertToMap(request.parameters)
    def filter = params[ToolkitConstants.SUGGEST_FILTER_PARAMETER_NAME] as String
    if (filter) {
      for (i in 1..20) {
        def order = "${filter.toUpperCase()}1${sprintf("%03d", i)}".toString()
        list << [value: order, id: "$i".toString()]
      }
    }
    // Record this request for use by GUI tests
    suggestCount++
    suggestLatestParameters = params

    def json = Holders.objectMapper.writeValueAsString(list)
    return HttpResponse.status(HttpStatus.OK).body(json)
  }

  /**
   * Determines the view to display for the given method.  This can be overridden in your controller class to
   * use a different naming scheme.<p>
   * This sub-class points to a sample directory.
   * @param methodName The method that needs the view (e.g. 'index').
   * @return The resulting view path.
   */
  @Override
  String getView(String methodName) {
    return "sample/order/$methodName"
  }
}