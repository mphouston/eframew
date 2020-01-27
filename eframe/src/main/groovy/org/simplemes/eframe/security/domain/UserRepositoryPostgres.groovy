/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package org.simplemes.eframe.security.domain


import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect

/**
 * The sample order repository base interface.  Provides the methods for the repo,
 * for production or dev (POSTGRES)
 */
@JdbcRepository(dialect = Dialect.POSTGRES)
interface UserRepositoryPostgres extends UserRepository {
}
