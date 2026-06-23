package com.novacasino.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.security.JwtAuthFilter;
import com.novacasino.api.security.RateLimitFilter;
import com.novacasino.api.security.RateLimitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
                // 401 — missing or invalid token
                .authenticationEntryPoint((request, response, e) -> {
                    final Locale locale = localeResolver.resolveLocale(request);
                    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
                    problem.setType(URI.create("about:blank"));
                    problem.setTitle(messageSource.getMessage("error.unauthorized.title", null, locale));
                    problem.setDetail(messageSource.getMessage("error.unauthorized.detail", null, locale));
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                    objectMapper.writeValue(response.getWriter(), problem);
                })
                // 403 — valid token but insufficient role
                .accessDeniedHandler((request, response, e) -> {
                    final Locale locale = localeResolver.resolveLocale(request);
                    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
                    problem.setType(URI.create("about:blank"));
                    problem.setTitle(messageSource.getMessage("error.forbidden.title", null, locale));
                    problem.setDetail(messageSource.getMessage("error.forbidden.detail", null, locale));
                    response.setStatus(403);
                    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                    objectMapper.writeValue(response.getWriter(), problem);
                })
            );

        return http.build();
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
