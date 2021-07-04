/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package sample.domain

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository

/**
 * The OrderLine repository base interface.  Provides the methods for the repo.
 */
@SuppressWarnings('unused')
@JdbcRepository(dialect = Dialect.POSTGRES)
interface OrderLineRepository extends CrudRepository<OrderLine, UUID> {

  List<OrderLine> findAllByOrder(Order order)

  Optional<OrderLine> findByUuid(UUID uuid)

  List<OrderLine> list()

}
