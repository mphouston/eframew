/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package org.simplemes.eframe.custom.domain

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository

/**
 * The FlexField repository base interface.  Provides the methods for the repo.
 */
@SuppressWarnings('unused')
@JdbcRepository(dialect = Dialect.POSTGRES)
interface FlexFieldRepository extends CrudRepository<FlexField, UUID> {
  Optional<FlexField> findByUuid(UUID uuid)

  List<FlexField> findAllByFlexType(FlexType flexType)

  List<FlexField> findAllByFieldNameIlike(String fieldName)

  List<FlexField> list()
}
