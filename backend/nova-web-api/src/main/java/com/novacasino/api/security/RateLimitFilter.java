package com.novacasino.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Anti-bot/anti-abuse rate limiting (token bucket, in-memory single-node via Bucket4j). Enforced on
 * the two abuse-prone, economic-effect endpoints:
 * <ul>
 *   <li>{@code POST /api/v1/auth/login} — keyed by client IP (brute-force protection on a public,
 *       unauthenticated endpoint).</li>
 *   <li>{@code POST /api/v1/player/games/&#42;/spin} — keyed by the authenticated user (or client IP
 *       if the request is not yet authenticated).</li>
 * </ul>
 * On exhaustion it returns {@code 429 Too Many Requests} as an RFC 9457 Problem Detail, localised via
 * the {@link LocaleResolver}, with a {@code Retry-After} header. Buckets are held in a
 * {@link ConcurrentHashMap}; this is per-instance (single-node), matching the deployment model.
 *
 * <p>Registered after {@link JwtAuthFilter} so the spin key can use the authenticated principal.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String PLAYER_GAMES_PREFIX = "/api/v1/player/games/";
    private static final String SPIN_SUFFIX = "/spin";

    private final RateLimitProperties properties;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final LocaleResolver localeResolver;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(final RateLimitProperties properties, final MessageSource messageSource,
                           final ObjectMapper objectMapper, final LocaleResolver localeResolver) {
        this.properties     = properties;
        this.messageSource  = messageSource;
        this.objectMapper   = objectMapper;
        this.localeResolver = localeResolver;
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        if (!properties.enabled() || !"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return target(request) == null;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain chain) throws ServletException, IOException {
        final Target target = target(request);
        if (target == null) {
            chain.doFilter(request, response);
            return;
        }

        final Bucket bucket = buckets.computeIfAbsent(target.key(), k -> newBucket(target.limit()));
        final ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", Long.toString(probe.getRemainingTokens()));
            chain.doFilter(request, response);
            return;
        }

        writeTooManyRequests(request, response, probe);
    }

    // -------------------------------------------------------------------------

    /** Classifies the request into a rate-limited target (login/spin) with its bucket key, or null. */
    private Target target(final HttpServletRequest request) {
        final String path = request.getRequestURI();
        if (LOGIN_PATH.equals(path)) {
            return new Target("login:ip:" + clientIp(request), properties.login());
        }
        if (path.startsWith(PLAYER_GAMES_PREFIX) && path.endsWith(SPIN_SUFFIX)) {
            final String principal = authenticatedPrincipal();
            final String key = principal != null
                    ? "spin:user:" + principal
                    : "spin:ip:" + clientIp(request);
            return new Target(key, properties.spin());
        }
        return null;
    }

    private Bucket newBucket(final RateLimitProperties.Limit limit) {
        final Bandwidth bandwidth = Bandwidth.builder()
                .capacity(limit.capacity())
                .refillGreedy(limit.capacity(), Duration.ofSeconds(limit.refillPeriodSeconds()))
                .build();
        return Bucket.builder().addLimit(bandwidth).build();
    }

    private void writeTooManyRequests(final HttpServletRequest request, final HttpServletResponse response,
                                      final ConsumptionProbe probe) throws IOException {
        final Locale locale = localeResolver.resolveLocale(request);
        final long retryAfterSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));

        final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
        problem.setType(URI.create("about:blank"));
        problem.setTitle(messageSource.getMessage("error.rateLimitExceeded.title", null, locale));
        problem.setDetail(messageSource.getMessage("error.rateLimitExceeded.detail", null, locale));

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(retryAfterSeconds));
        response.setHeader("X-Rate-Limit-Remaining", "0");
        objectMapper.writeValue(response.getWriter(), problem);
    }

    /** Authenticated username if the request already carries a (non-anonymous) authentication. */
    private String authenticatedPrincipal() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }

    /** Client IP, honouring a single {@code X-Forwarded-For} hop when present. */
    private String clientIp(final HttpServletRequest request) {
        final String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /** A resolved rate-limit target: the bucket key and the limit to apply. */
    private record Target(String key, RateLimitProperties.Limit limit) {
    }
}
