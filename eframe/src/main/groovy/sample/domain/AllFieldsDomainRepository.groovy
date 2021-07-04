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
 * The AllFieldsDomain repository base interface.  Provides the methods for the repo.
 */
@SuppressWarnings('unused')
@JdbcRepository(dialect = Dialect.POSTGRES)
interface AllFieldsDomainRepository extends CrudRepository<AllFieldsDomain, UUID> {

  Optional<AllFieldsDomain> findByName(String name)

  List<AllFieldsDomain> findAllByName(String name)

  List<AllFieldsDomain> findAllByDateTimeLessThan(Date date)

  List<AllFieldsDomain> findAllByDateTimeGreaterThan(Date date)

  Optional<AllFieldsDomain> findByUuid(UUID uuid)

  List<AllFieldsDomain> list(Pageable pageable)

  List<AllFieldsDomain> list()

  // Test that joins the order reference.  This only works for required references.
  @Join(value = "order", type = Join.Type.LEFT_FETCH)
  Optional<AllFieldsDomain> findByTitle(String title)

}
