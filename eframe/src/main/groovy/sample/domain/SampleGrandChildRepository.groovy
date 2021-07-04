/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package sample.domain

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository

/**
 * The SampleGrandChild repository base interface.  Provides the methods for the repo.
 */
@SuppressWarnings('unused')
@JdbcRepository(dialect = Dialect.POSTGRES)
interface SampleGrandChildRepository extends CrudRepository<SampleGrandChild, UUID> {
  Optional<SampleGrandChild> findByUuid(UUID uuid)
  List<SampleGrandChild> list()
  List<SampleGrandChild> findAllBySampleChild(SampleChild sampleChild)
}
