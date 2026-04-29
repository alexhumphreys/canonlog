package io.canonlog.jdbc

import io.canonlog.CanonicalLogContext
import io.canonlog.Outcome
import io.canonlog.WorkUnit
import io.canonlog.WorkUnitAdapter
import io.canonlog.withCanonicalLogBlocking
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.SQLException
import java.time.Instant
import javax.sql.DataSource

private val nullAdapter = object : WorkUnitAdapter<String> {
    override fun describe(input: String): WorkUnit = WorkUnit(input, "test", Instant.now())
    override fun enrich(ctx: CanonicalLogContext, input: String, outcome: Outcome) {}
}

private fun PostgreSQLContainer<*>.dataSource(): DataSource = PGSimpleDataSource().also {
    it.setURL(jdbcUrl)
    it.user = username
    it.password = password
}

class JdbcCanonicalListenerTest : DescribeSpec({

    val postgres = PostgreSQLContainer("postgres:16-alpine")

    beforeSpec { postgres.start() }
    afterSpec { postgres.stop() }

    describe("JdbcCanonicalListener") {
        it("counts queries and accumulates duration") {
            val ds = postgres.dataSource().withCanonicalLogging()
            var snap: Map<String, Any> = emptyMap()

            withCanonicalLogBlocking(nullAdapter, "wu", { snap = it.snapshot() }) {
                ds.connection.use { conn ->
                    conn.createStatement().use { it.execute("SELECT 1") }
                    conn.createStatement().use { it.execute("SELECT 2") }
                    conn.createStatement().use { it.execute("SELECT 3") }
                }
            }

            snap["db_query_count"] shouldBe 3L
            (snap["db_query_duration_ms_total"] as Long).shouldBeGreaterThanOrEqual(0L)
            snap.containsKey("db_query_error_count") shouldBe false
        }

        it("counts slow queries above the threshold") {
            // Threshold 0 makes every query slow
            val listener = JdbcCanonicalListener(slowQueryThresholdMs = 0L)
            val ds = postgres.dataSource().withCanonicalLogging(listener = listener)
            var snap: Map<String, Any> = emptyMap()

            withCanonicalLogBlocking(nullAdapter, "wu", { snap = it.snapshot() }) {
                ds.connection.use { conn ->
                    conn.createStatement().use { it.execute("SELECT pg_sleep(0)") }
                }
            }

            snap["db_query_count"] shouldBe 1L
            snap["db_slow_query_count"] shouldBe 1L
        }

        it("counts query errors") {
            val ds = postgres.dataSource().withCanonicalLogging()
            var snap: Map<String, Any> = emptyMap()

            runCatching {
                withCanonicalLogBlocking(nullAdapter, "wu", { snap = it.snapshot() }) {
                    ds.connection.use { conn ->
                        conn.createStatement().use { it.execute("SELECT * FROM nonexistent_table") }
                    }
                }
            }.exceptionOrNull().let { (it is SQLException) shouldBe true }

            snap["db_query_count"] shouldBe 1L
            snap["db_query_error_count"] shouldBe 1L
        }

        it("is a no-op outside an active canonical context") {
            val ds = postgres.dataSource().withCanonicalLogging()
            ds.connection.use { conn ->
                conn.createStatement().use { stmt -> stmt.execute("SELECT 1") shouldBe true }
            }
        }
    }
})
