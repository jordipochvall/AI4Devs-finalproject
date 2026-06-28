package com.novacasino.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests for the Bucket4j-backed {@link RateLimitFilter} (login + spin throttling). */
class RateLimitFilterTest {

    private MessageSource messageSource;
    private LocaleResolver localeResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(any(), any(), any())).thenReturn("msg");
        localeResolver = mock(LocaleResolver.class);
        when(localeResolver.resolveLocale(any())).thenReturn(Locale.ENGLISH);
    }

    private RateLimitFilter filter(final boolean enabled, final int loginCap, final int spinCap) {
        final RateLimitProperties props = new RateLimitProperties(enabled,
                new RateLimitProperties.Limit(loginCap, 60),
                new RateLimitProperties.Limit(spinCap, 60));
        return new RateLimitFilter(props, messageSource, objectMapper, localeResolver);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest req(final String method, final String uri, final String ip) {
        final MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr(ip);
        return request;
    }

    private void authenticateAs(final String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of()));
    }

    @Test
    void login_allowsUpToCapacityThenReturns429() throws Exception {
        final RateLimitFilter filter = filter(true, 2, 5);
        final FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 2; i++) {
            final MockHttpServletResponse ok = new MockHttpServletResponse();
            filter.doFilter(req("POST", "/api/v1/auth/login", "9.9.9.9"), ok, chain);
            assertThat(ok.getStatus()).isEqualTo(200);
        }

        final MockHttpServletResponse blocked = new MockHttpServletResponse();
        filter.doFilter(req("POST", "/api/v1/auth/login", "9.9.9.9"), blocked, chain);

        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getContentType()).contains("application/problem+json");
        assertThat(blocked.getHeader("Retry-After")).isNotNull();
        assertThat(blocked.getHeader("X-Rate-Limit-Remaining")).isEqualTo("0");
        verify(chain, times(2)).doFilter(any(), any()); // the blocked request did not pass through
    }

    @Test
    void login_bucketsAreIndependentPerIp() throws Exception {
        final RateLimitFilter filter = filter(true, 1, 5);
        final FilterChain chain = mock(FilterChain.class);

        final MockHttpServletResponse a = new MockHttpServletResponse();
        filter.doFilter(req("POST", "/api/v1/auth/login", "1.1.1.1"), a, chain);
        final MockHttpServletResponse b = new MockHttpServletResponse();
        filter.doFilter(req("POST", "/api/v1/auth/login", "2.2.2.2"), b, chain);

        assertThat(a.getStatus()).isEqualTo(200);
        assertThat(b.getStatus()).isEqualTo(200); // different IP → own bucket, not throttled
        verify(chain, times(2)).doFilter(any(), any());
    }

    @Test
    void spin_isKeyedByAuthenticatedUser_notIp() throws Exception {
        final RateLimitFilter filter = filter(true, 5, 1);
        final FilterChain chain = mock(FilterChain.class);

        // user1 spins once (ok), then again from a DIFFERENT ip → still throttled (same user bucket).
        authenticateAs("user1");
        final MockHttpServletResponse first = new MockHttpServletResponse();
        filter.doFilter(req("POST", "/api/v1/player/games/3/spin", "10.0.0.1"), first, chain);
        assertThat(first.getStatus()).isEqualTo(200);

        final MockHttpServletResponse sameUserOtherIp = new MockHttpServletResponse();
        filter.doFilter(req("POST", "/api/v1/player/games/3/spin", "10.0.0.2"), sameUserOtherIp, chain);
        assertThat(sameUserOtherIp.getStatus()).isEqualTo(429);

        // A different user has an independent bucket.
        SecurityContextHolder.clearContext();
        authenticateAs("user2");
        final MockHttpServletResponse otherUser = new MockHttpServletResponse();
        filter.doFilter(req("POST", "/api/v1/player/games/3/spin", "10.0.0.1"), otherUser, chain);
        assertThat(otherUser.getStatus()).isEqualTo(200);

        verify(chain, times(2)).doFilter(any(), any()); // first + user2 passed; sameUserOtherIp blocked
    }

    @Test
    void login_honoursXForwardedFor() throws Exception {
        final RateLimitFilter filter = filter(true, 1, 5);
        final FilterChain chain = mock(FilterChain.class);

        // Two requests from the same forwarded client IP (different remote addr) share a bucket.
        final MockHttpServletRequest r1 = req("POST", "/api/v1/auth/login", "127.0.0.1");
        r1.addHeader("X-Forwarded-For", "7.7.7.7, 1.2.3.4");
        final MockHttpServletResponse a = new MockHttpServletResponse();
        filter.doFilter(r1, a, chain);
        assertThat(a.getStatus()).isEqualTo(200);

        final MockHttpServletRequest r2 = req("POST", "/api/v1/auth/login", "127.0.0.2");
        r2.addHeader("X-Forwarded-For", "7.7.7.7");
        final MockHttpServletResponse b = new MockHttpServletResponse();
        filter.doFilter(r2, b, chain);
        assertThat(b.getStatus()).isEqualTo(429); // same forwarded IP → throttled

        // A different forwarded IP is independent.
        final MockHttpServletRequest r3 = req("POST", "/api/v1/auth/login", "127.0.0.1");
        r3.addHeader("X-Forwarded-For", "8.8.8.8");
        final MockHttpServletResponse c = new MockHttpServletResponse();
        filter.doFilter(r3, c, chain);
        assertThat(c.getStatus()).isEqualTo(200);
    }

    @Test
    void spin_isThrottledByItsOwnBucket() throws Exception {
        final RateLimitFilter filter = filter(true, 5, 1);
        final FilterChain chain = mock(FilterChain.class);

        final MockHttpServletResponse ok = new MockHttpServletResponse();
        filter.doFilter(req("POST", "/api/v1/player/games/3/spin", "5.5.5.5"), ok, chain);
        assertThat(ok.getStatus()).isEqualTo(200);

        final MockHttpServletResponse blocked = new MockHttpServletResponse();
        filter.doFilter(req("POST", "/api/v1/player/games/3/spin", "5.5.5.5"), blocked, chain);
        assertThat(blocked.getStatus()).isEqualTo(429);
        verify(chain, times(1)).doFilter(any(), any());
    }

    @Test
    void disabled_passesEverythingThrough() throws Exception {
        final RateLimitFilter filter = filter(false, 1, 1);
        final FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 5; i++) {
            final MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req("POST", "/api/v1/auth/login", "9.9.9.9"), res, chain);
            assertThat(res.getStatus()).isEqualTo(200);
        }
        verify(chain, times(5)).doFilter(any(), any());
    }

    @Test
    void nonTargetEndpoint_isNotThrottled() throws Exception {
        final RateLimitFilter filter = filter(true, 1, 1);
        final FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 3; i++) {
            final MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req("GET", "/api/v1/player/wallet", "9.9.9.9"), res, chain);
            assertThat(res.getStatus()).isEqualTo(200);
        }
        verify(chain, times(3)).doFilter(any(), any());
    }
}
