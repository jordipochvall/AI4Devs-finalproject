package com.novacasino.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.security.JwtAuthFilter;
import com.novacasino.api.security.RateLimitFilter;
import com.novacasino.api.security.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

/**
 * Stateless JWT security configuration. Routes are authorised by role; authentication (401)
 * and authorization (403) failures are returned as RFC 9457 Problem Details, localised via the
 * {@link LocaleResolver} (these handlers run before the DispatcherServlet, so they cannot rely
 * on {@code LocaleContextHolder}).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(RateLimitProperties.class)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;
    private final UserDetailsService userDetailsService;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final LocaleResolver localeResolver;

    public SecurityConfig(final JwtAuthFilter jwtAuthFilter,
                          final RateLimitFilter rateLimitFilter,
                          final UserDetailsService userDetailsService,
                          final MessageSource messageSource,
                          final ObjectMapper objectMapper,
                          final LocaleResolver localeResolver) {
        this.jwtAuthFilter      = jwtAuthFilter;
        this.rateLimitFilter    = rateLimitFilter;
        this.userDetailsService = userDetailsService;
        this.messageSource      = messageSource;
        this.objectMapper       = objectMapper;
        this.localeResolver     = localeResolver;
    }

    /** Defines the stateless filter chain, per-role authorization and the 401/403 responses. */
    @Bean
    SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/operator/**").hasRole("OPERATOR")
                .requestMatchers("/api/v1/math/**").hasRole("MATH_ANALYST")
                .requestMatchers("/api/v1/player/**").hasRole("PLAYER")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // After JWT auth so the spin limit can key by the authenticated user.
            .addFilterAfter(rateLimitFilter, JwtAuthFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(this::writeUnauthorized) // 401 — missing or invalid token
                .accessDeniedHandler(this::writeForbidden));       // 403 — valid token, insufficient role

        return http.build();
    }

    /** 401 handler: missing or invalid token. */
    private void writeUnauthorized(final HttpServletRequest request, final HttpServletResponse response,
                                   final AuthenticationException ex) throws IOException {
        writeProblem(request, response, HttpStatus.UNAUTHORIZED,
                "error.unauthorized.title", "error.unauthorized.detail");
    }

    /** 403 handler: valid token but insufficient role. */
    private void writeForbidden(final HttpServletRequest request, final HttpServletResponse response,
                                final AccessDeniedException ex) throws IOException {
        writeProblem(request, response, HttpStatus.FORBIDDEN,
                "error.forbidden.title", "error.forbidden.detail");
    }

    /**
     * Writes a localised RFC 9457 Problem Detail. These handlers run before the DispatcherServlet, so
     * they resolve the locale via the {@link LocaleResolver} rather than {@code LocaleContextHolder}.
     */
    private void writeProblem(final HttpServletRequest request, final HttpServletResponse response,
                              final HttpStatus status, final String titleKey, final String detailKey)
            throws IOException {
        final Locale locale = localeResolver.resolveLocale(request);
        final ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(URI.create("about:blank"));
        problem.setTitle(messageSource.getMessage(titleKey, null, locale));
        problem.setDetail(messageSource.getMessage(detailKey, null, locale));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problem);
    }

    // Both filters are @Component beans, so Spring Boot would also auto-register them as plain servlet
    // filters and run them a second time (outside the security chain). For RateLimitFilter that would
    // double-count tokens (halving the limit); we disable the auto-registration and rely solely on the
    // ordered placement inside the security filter chain above.

    @Bean
    FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(final JwtAuthFilter filter) {
        final FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(final RateLimitFilter filter) {
        final FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /** BCrypt password encoder (cost 12). */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /** DAO authentication provider backed by the user-details service and BCrypt. */
    @Bean
    AuthenticationProvider authenticationProvider() {
        final DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(final AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
