package io.github.alexhumphreys.canonicallog.jdbc

import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder
import javax.sql.DataSource

public fun DataSource.withCanonicalLogging(
    name: String = "canonical-log-jdbc",
    listener: JdbcCanonicalListener = JdbcCanonicalListener(),
): DataSource =
    ProxyDataSourceBuilder.create(this)
        .name(name)
        .listener(listener)
        .build()
