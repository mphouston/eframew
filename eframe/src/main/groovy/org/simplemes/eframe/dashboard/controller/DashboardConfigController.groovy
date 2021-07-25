/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package org.simplemes.eframe.dashboard.controller

import groovy.util.logging.Slf4j
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import org.simplemes.eframe.controller.BaseCrudRestController
import org.simplemes.eframe.controller.StandardModelAndView
import org.simplemes.eframe.dashboard.domain.DashboardConfig

import io.micronaut.core.annotation.Nullable
import java.security.Principal

/**
 * Provides definition-time access to a dashboard configuration.  Mainly used for CRUD/REST access.  No GUI access.
 */
@Slf4j
@Secured(['ADMIN', 'DESIGNER'])
@Controller('/dashboardConfig')
class DashboardConfigController extends BaseCrudRestController {

  /**
   * Specify the domain for this controller.
   */
  @SuppressWarnings('unused')
  static domainClass = DashboardConfig


  /**
   * Displays the editor dialog page.
   * @param principal The user logged in.
   * @return The model/view to display.
   */
  @Produces(MediaType.TEXT_HTML)
  @Get("/editor")
  StandardModelAndView editor(HttpRequest request, @Nullable Principal principal) {
    def modelAndView = new StandardModelAndView("dashboard/editor", principal, this)
    log.trace('index(): {}', modelAndView)
    return modelAndView
  }

  /**
   * Displays the main details dialog page.
   * @param principal The user logged in.
   * @return The model/view to display.
   */
  @Produces(MediaType.TEXT_HTML)
  @Get("/detailsDialog")
  StandardModelAndView detailsDialog(@Nullable Principal principal) {
    def modelAndView = new StandardModelAndView("dashboard/detailsDialog", principal, this)
    log.trace('detailsDialog(): {}', modelAndView)
    return modelAndView
  }

  /**
   * Displays the panel details dialog page.
   * @param principal The user logged in.
   * @return The model/view to display.
   */
  @Produces(MediaType.TEXT_HTML)
  @Get("/panelDetailsDialog")
  StandardModelAndView panelDetailsDialog(@Nullable Principal principal) {
    def modelAndView = new StandardModelAndView("dashboard/panelDetailsDialog", principal, this)
    log.trace('panelDetailsDialog(): {}', modelAndView)
    return modelAndView
  }

  /**
   * Displays the button details dialog page.
   * @param principal The user logged in.
   * @return The model/view to display.
   */
  @Produces(MediaType.TEXT_HTML)
  @Get("/buttonDetailsDialog")
  StandardModelAndView buttonDetailsDialog(@Nullable Principal principal) {
    def modelAndView = new StandardModelAndView("dashboard/buttonDetailsDialog", principal, this)
    log.trace('buttonDetailsDialog(): {}', modelAndView)
    return modelAndView
  }

}
