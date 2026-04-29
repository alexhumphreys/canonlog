package io.canonlog.okhttp

import io.canonlog.CanonicalLogContext
import io.canonlog.Outcome
import io.canonlog.WorkUnit
import io.canonlog.WorkUnitAdapter
import io.canonlog.withCanonicalLogBlocking
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.InetAddress
import java.time.Instant

private val nullAdapter = object : WorkUnitAdapter<String> {
    override fun describe(input: String): WorkUnit = WorkUnit(input, "test", Instant.now())
    override fun enrich(ctx: CanonicalLogContext, input: String, outcome: Outcome) {}
}

private fun client(): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(OkHttpCanonicalInterceptor())
    .build()

private fun get(c: OkHttpClient, url: String): Int =
    c.newCall(Request.Builder().url(url).build()).execute().use { it.code }

class OkHttpCanonicalInterceptorTest : DescribeSpec({

    lateinit var server: MockWebServer

    beforeEach {
        server = MockWebServer()
        server.start()
    }
    afterEach {
        server.close()
    }

    describe("OkHttpCanonicalInterceptor inside an active canonical context") {
        it("counts a 2xx call and accumulates duration") {
            server.enqueue(MockResponse(code = 200, body = "ok"))
            var snap: Map<String, Any> = emptyMap()

            withCanonicalLogBlocking(nullAdapter, "wu", { snap = it.snapshot() }) {
                get(client(), server.url("/").toString()) shouldBe 200
            }

            snap["http_client_request_count"] shouldBe 1L
            (snap["http_client_request_duration_ms_total"] as Long >= 0L) shouldBe true
            snap.containsKey("http_client_4xx_count") shouldBe false
            snap.containsKey("http_client_5xx_count") shouldBe false
            snap.containsKey("http_client_error_count") shouldBe false
        }

        it("counts a 404 as 4xx") {
            server.enqueue(MockResponse(code = 404))
            var snap: Map<String, Any> = emptyMap()

            withCanonicalLogBlocking(nullAdapter, "wu", { snap = it.snapshot() }) {
                get(client(), server.url("/missing").toString()) shouldBe 404
            }

            snap["http_client_request_count"] shouldBe 1L
            snap["http_client_4xx_count"] shouldBe 1L
            snap.containsKey("http_client_5xx_count") shouldBe false
        }

        it("counts a 500 as 5xx") {
            server.enqueue(MockResponse(code = 503))
            var snap: Map<String, Any> = emptyMap()

            withCanonicalLogBlocking(nullAdapter, "wu", { snap = it.snapshot() }) {
                get(client(), server.url("/").toString()) shouldBe 503
            }

            snap["http_client_request_count"] shouldBe 1L
            snap["http_client_5xx_count"] shouldBe 1L
            snap.containsKey("http_client_4xx_count") shouldBe false
        }

        it("counts an IOException as an error") {
            // Point at a closed socket on a routable but non-listening port.
            server.close()
            val deadUrl = "http://127.0.0.1:1/"
            var snap: Map<String, Any> = emptyMap()

            val ex = runCatching {
                withCanonicalLogBlocking(nullAdapter, "wu", { snap = it.snapshot() }) {
                    get(client(), deadUrl)
                }
            }.exceptionOrNull()

            (ex is IOException) shouldBe true
            snap["http_client_request_count"] shouldBe 1L
            snap["http_client_error_count"] shouldBe 1L
        }

        it("accumulates counters across multiple calls") {
            server.enqueue(MockResponse(code = 200))
            server.enqueue(MockResponse(code = 200))
            server.enqueue(MockResponse(code = 404))
            var snap: Map<String, Any> = emptyMap()

            withCanonicalLogBlocking(nullAdapter, "wu", { snap = it.snapshot() }) {
                val c = client()
                get(c, server.url("/a").toString())
                get(c, server.url("/b").toString())
                get(c, server.url("/c").toString())
            }

            snap["http_client_request_count"] shouldBe 3L
            snap["http_client_4xx_count"] shouldBe 1L
        }
    }

    describe("OkHttpCanonicalInterceptor outside an active canonical context") {
        it("is a no-op — request still succeeds, no exception") {
            server.enqueue(MockResponse(code = 200, body = "ok"))
            get(client(), server.url("/").toString()) shouldBe 200
        }
    }
})

private fun localhost(): String = InetAddress.getLoopbackAddress().hostAddress
