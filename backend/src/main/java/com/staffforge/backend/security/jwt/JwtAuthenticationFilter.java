package com.staffforge.backend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, @Lazy @Autowired(required = false) UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length()).trim();

        try {
            if (jwtUtils.validateToken(jwt)) {
                final String username = jwtUtils.extractUsername(jwt);

                if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = null;
                    if (userDetailsService != null) {
                        try {
                            userDetails = userDetailsService.loadUserByUsername(username);
                        } catch (UsernameNotFoundException e) {
                            log.debug("User '{}' not found in UserDetailsService, falling back to token claims", username);
                        }
                    }

                    UsernamePasswordAuthenticationToken authentication;
                    if (userDetails != null && jwtUtils.isTokenValid(jwt, userDetails)) {
                        authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                    } else {
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        String role = jwtUtils.extractRole(jwt);
                        if (StringUtils.hasText(role)) {
                            if (!role.startsWith("ROLE_")) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                            }
                            authorities.add(new SimpleGrantedAuthority(role));
                        }
                        authentication = new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities
                        );
                    }

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication in security context: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
