/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package sample.domain

import io.micronaut.data.annotation.Join
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository

/**
 * The Order repository base interface.  Provides the methods for the repo.
 */
@JdbcRepository(dialect = Dialect.POSTGRES)
interface OrderRepository extends CrudRepository<Order, UUID> {

  Optional<Order> findByOrder(String order)

  Optional<Order> findById(UUID uuid)

  Optional<Order> findByUuid(UUID uuid)

  List<Order> list(Pageable pageable)
  List<Order> list()

  Order get(UUID uuid)


}
