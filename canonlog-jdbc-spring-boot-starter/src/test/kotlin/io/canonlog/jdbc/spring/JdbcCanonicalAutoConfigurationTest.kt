package io.canonlog.jdbc.spring

import io.canonlog.CanonicalLogContext
import io.canonlog.Outcome
import io.canonlog.WorkUnit
import io.canonlog.WorkUnitAdapter
import io.canonlog.jdbc.withCanonicalLogging
import io.canonlog.withCanonicalLogBlocking
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import net.ttddyy.dsproxy.support.ProxyDataSource
import org.h2.jdbcx.JdbcDataSource
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.time.Instant
import javax.sql.DataSource

private val nullAdapter = object : WorkUnitAdapter<String> {
    override fun describe(input: String): WorkUnit = WorkUnit(input, "test", Instant.now())
    override fun enrich(ctx: CanonicalLogContext, input: String, outcome: Outcome) {}
}

private fun h2DataSource(): DataSource = JdbcDataSource().apply {
    setURL("jdbc:h2:mem:starter-test-${System.nanoTime()};DB_CLOSE_DELAY=-1")
    user = "sa"
}

class JdbcCanonicalAutoConfigurationTest : DescribeSpec({

    val runner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                JdbcCanonicalAutoConfiguration::class.java,
                DataSourceAutoConfiguration::class.java,
            ),
        )
        .withPropertyValues(
            "spring.datasource.url=jdbc:h2:mem:test-${System.nanoTime()}",
            "spring.datasource.driver-class-name=org.h2.Driver",
        )

    describe("JdbcCanonicalAutoConfiguration") {
        it("wraps the auto-configured DataSource with the canonical proxy") {
            runner.run { ctx ->
                val ds = ctx.getBean(DataSource::class.java)
                (ds is ProxyDataSource) shouldBe true
            }
        }

        it("the wrapped DataSource emits canonical fields when used") {
            runner.run { ctx ->
                val ds = ctx.getBean(DataSource::class.java)
                var snap: Map<String, Any> = emptyMap()
                withCanonicalLogBlocking(nullAdapter, "wu", { snap = it.snapshot() }) {
                    ds.connection.use { c ->
                        c.createStatement().use { it.execute("SELECT 1") }
                        c.createStatement().use { it.execute("SELECT 2") }
                    }
                }
                snap["db_query_count"] shouldBe 2L
            }
        }

        it("does not double-wrap a DataSource that is already a ProxyDataSource") {
            ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JdbcCanonicalAutoConfiguration::class.java))
                .withBean("dataSource", DataSource::class.java, {
                    h2DataSource().withCanonicalLogging(name = "pre-wrapped")
                })
                .run { ctx ->
                    val ds = ctx.getBean(DataSource::class.java)
                    val proxy = ds.shouldBeProxy()
                    proxy.proxyConfig.dataSourceName shouldBe "pre-wrapped"
                }
        }
    }
})

private fun DataSource.shouldBeProxy(): ProxyDataSource {
    check(this is ProxyDataSource) { "expected ProxyDataSource, got ${this::class.qualifiedName}" }
    return this
}
