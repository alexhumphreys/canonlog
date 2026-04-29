package io.canonlog.spring

import io.canonlog.CanonicalLogContext
import io.canonlog.withCanonicalLogBlocking
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.LoggerFactory
import org.springframework.web.filter.OncePerRequestFilter

public class CanonicalLogFilter : OncePerRequestFilter() {
    private val canonicalLogger = LoggerFactory.getLogger("canonical")
    private val adapter = HttpWorkUnitAdapter()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val exchange = HttpExchange(request, response)
        withCanonicalLogBlocking(adapter, exchange, ::emit) {
            filterChain.doFilter(request, response)
        }
    }

    private fun emit(ctx: CanonicalLogContext) {
        canonicalLogger.info("canonical", StructuredArguments.entries(ctx.snapshot()))
    }
}
