package com.novacasino.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Extrae el JWT de la cabecera {@code Authorization: Bearer <token>}, lo valida
 * y puebla el {@link SecurityContextHolder}. Si el token es inválido o falta,
 * simplemente no se autentica — Spring Security responderá con 401 vía
 * {@code AuthenticationEntryPoint}.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService         = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            if (jwtService.isValid(token)) {
                String email = jwtService.extractEmail(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails details = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (UsernameNotFoundException | IllegalArgumentException ignored) {
            // Token válido pero usuario no encontrado — no autenticamos
        }

        chain.doFilter(request, response);
    }
}
