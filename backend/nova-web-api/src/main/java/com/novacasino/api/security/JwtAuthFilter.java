package com.novacasino.api.security;

import com.novacasino.api.logging.RequestLoggingFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Extracts the JWT from the {@code Authorization: Bearer <token>} header, validates it and
 * populates the {@link SecurityContextHolder}. If the token is missing or invalid it simply
 * does not authenticate — Spring Security then responds 401 via its AuthenticationEntryPoint.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(final JwtService jwtService, final UserDetailsServiceImpl userDetailsService) {
        this.jwtService         = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain chain) throws ServletException, IOException {
        final String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        final String token = header.substring(7);
        try {
            if (jwtService.isValid(token)) {
                final String email = jwtService.extractEmail(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    final UserDetails details = userDetailsService.loadUserByUsername(email);
                    final UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    // Correlate every subsequent log line with the authenticated user (id only, never email).
                    MDC.put(RequestLoggingFilter.USER_ID, String.valueOf(((NovaUserDetails) details).getUser().getId()));
                }
            } else {
                log.debug("JWT rejected: token invalid or expired");
            }
        } catch (final UsernameNotFoundException | IllegalArgumentException ex) {
            // Valid token but user not found / malformed — leave unauthenticated.
            log.debug("JWT accepted but principal could not be loaded: {}", ex.getMessage());
        }

        chain.doFilter(request, response);
    }
}
