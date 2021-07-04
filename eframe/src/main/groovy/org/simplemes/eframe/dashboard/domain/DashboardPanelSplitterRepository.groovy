/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package org.simplemes.eframe.dashboard.domain

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository

/**
 * The DashboardPanelSplitter repository base interface.  Provides the methods for the repo.
 */
@JdbcRepository(dialect = Dialect.POSTGRES)
interface DashboardPanelSplitterRepository extends CrudRepository<DashboardPanelSplitter, UUID> {
  Optional<DashboardPanelSplitter> findByUuid(UUID uuid)

  List<DashboardPanelSplitter> findAllByDashboardConfig(DashboardConfig dashboardConfig)

  List<DashboardPanelSplitter> list()
}
