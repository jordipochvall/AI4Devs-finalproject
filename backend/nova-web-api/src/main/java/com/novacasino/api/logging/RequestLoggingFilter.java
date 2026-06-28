package com.novacasino.api.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Outermost request filter (runs before Spring Security). It establishes a per-request correlation id
 * in the logging {@link MDC} — propagated from {@code X-Request-Id} when present, otherwise generated —
 * and echoes it back in the response so a client/operator can tie a report to the server logs. The
 * authenticated {@code userId} is added to the MDC later by {@code JwtAuthFilter}; both are cleared here
 * when the request ends, so no context leaks across reused worker threads.
 *
 * <p>One access line is logged per request: {@code WARN} for {@code >= 400} (problems are visible at the
 * default INFO level) and {@code DEBUG} for successful requests (the high-volume happy path stays quiet
 * in production). Structured logging emits {@code requestId}/{@code userId} as JSON fields via the MDC.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    /** Dedicated category so access logging can be tuned independently (e.g. set to DEBUG to see all). */
    private static final Logger log = LoggerFactory.getLogger("http.access");

    public static final String REQUEST_ID = "requestId";
    public static final String USER_ID = "userId";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain chain) throws ServletException, IOException {
        final long startNanos = System.nanoTime();
        final String requestId = resolveRequestId(request);
        MDC.put(REQUEST_ID, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            final long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
            final int status = response.getStatus();
            if (status >= 400) {
                log.warn("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), status, elapsedMs);
            } else {
                log.debug("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), status, elapsedMs);
            }
            MDC.clear();
        }
    }

    private String resolveRequestId(final HttpServletRequest request) {
        final String incoming = request.getHeader(REQUEST_ID_HEADER);
        return incoming != null && !incoming.isBlank() ? incoming : UUID.randomUUID().toString();
    }
}
