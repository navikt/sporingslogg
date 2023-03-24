package no.nav.pensjon.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class CorrelasionFilter: OncePerRequestFilter() {


    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        val requestId =
            REQUEST_ID_HEADER_CANDIDATES
                .firstOrNull { request.getHeader(it) != null }
                ?.let { header -> request.getHeader(header) }
                ?: UUID.randomUUID().toString()

        MDC.put(REQUEST_ID_MDC_KEY, requestId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(REQUEST_ID_MDC_KEY)
        }


    }

    companion object {

        const val REQUEST_ID_MDC_KEY = "x_request_id"
        const val REQUEST_ID_HEADER = "X-Request-Id"
        const val NAV_CALL_ID_HEADER = "Nav-Call-Id"
        const val NAV_CONSUMER_ID = "Nav-Consumer-Id"

        private val REQUEST_ID_HEADER_CANDIDATES = listOf(
            REQUEST_ID_HEADER,
            NAV_CALL_ID_HEADER,
            NAV_CONSUMER_ID)

    }

}