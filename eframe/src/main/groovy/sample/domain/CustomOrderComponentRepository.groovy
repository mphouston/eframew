/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package sample.domain

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository

/**
 * The CustomOrderComponent repository base interface.  Provides the methods for the repo.
 */
@SuppressWarnings('unused')
@JdbcRepository(dialect = Dialect.POSTGRES)
interface CustomOrderComponentRepository extends CrudRepository<CustomOrderComponent, UUID> {
  List<CustomOrderComponent> findAllByOrder(Order order)

  Optional<CustomOrderComponent> findById(UUID uuid)

  List<CustomOrderComponent> list()

}
