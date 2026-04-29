package io.canonlog.okhttp

import io.canonlog.CanonicalLog
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

public class OkHttpCanonicalInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startNs = System.nanoTime()

        try {
            val response = chain.proceed(request)
            val durationMs = (System.nanoTime() - startNs) / 1_000_000

            CanonicalLog.increment("http_client_request_count")
            CanonicalLog.increment("http_client_request_duration_ms_total", durationMs)
            when {
                response.code >= 500 -> CanonicalLog.increment("http_client_5xx_count")
                response.code >= 400 -> CanonicalLog.increment("http_client_4xx_count")
            }

            return response
        } catch (e: IOException) {
            val durationMs = (System.nanoTime() - startNs) / 1_000_000
            CanonicalLog.increment("http_client_request_count")
            CanonicalLog.increment("http_client_request_duration_ms_total", durationMs)
            CanonicalLog.increment("http_client_error_count")
            throw e
        }
    }
}
