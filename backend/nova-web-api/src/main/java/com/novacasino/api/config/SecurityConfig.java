package com.novacasino.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.security.JwtAuthFilter;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final LocaleResolver localeResolver;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          UserDetailsService userDetailsService,
                          MessageSource messageSource,
                          ObjectMapper objectMapper,
                          LocaleResolver localeResolver) {
        this.jwtAuthFilter      = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.messageSource      = messageSource;
        this.objectMapper       = objectMapper;
        this.localeResolver     = localeResolver;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                .requestMatchers("/api/v1/operator/**").hasRole("OPERATOR")
                .requestMatchers("/api/v1/math/**").hasRole("MATH_ANALYST")
                .requestMatchers("/api/v1/player/**").hasRole("PLAYER")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                // 401 — sin token o token inválido
                .authenticationEntryPoint((request, response, e) -> {
                    Locale locale = localeResolver.resolveLocale(request);
                    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
                    problem.setType(URI.create("about:blank"));
                    problem.setTitle(messageSource.getMessage("error.unauthorized.title", null, locale));
                    problem.setDetail(messageSource.getMessage("error.unauthorized.detail", null, locale));
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                    objectMapper.writeValue(response.getWriter(), problem);
                })
                // 403 — token válido pero rol insuficiente
                .accessDeniedHandler((request, response, e) -> {
                    Locale locale = localeResolver.resolveLocale(request);
                    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
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

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
