package io.canonlog.sample

import io.canonlog.okhttp.OkHttpCanonicalInterceptor
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import jakarta.annotation.PreDestroy

@SpringBootApplication
class Application {

    private val upstream = MockWebServer()

    @Bean
    fun mockUpstream(): MockWebServer {
        upstream.dispatcher = object : mockwebserver3.Dispatcher() {
            override fun dispatch(request: mockwebserver3.RecordedRequest): MockResponse {
                if (request.target.startsWith("/comments/")) {
                    return MockResponse(code = 200, body = """{"count":7}""")
                }
                if (request.target.startsWith("/author/")) {
                    return MockResponse(code = 200, body = """{"name":"Alex"}""")
                }
                return MockResponse(code = 404)
            }
        }
        upstream.start()
        return upstream
    }

    @Bean
    fun okHttpClient(interceptor: OkHttpCanonicalInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

    @PreDestroy
    fun shutdown() {
        upstream.close()
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
