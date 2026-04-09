package com.java.scheduler.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        // 1. Check header starts with "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                email = jwtUtil.extractEmail(jwt);
            } catch (Exception e) {
                logger.error("Could not extract email from token");
            }
        }

        // 2. If we have an email and the user isn't authenticated yet
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
        	if (jwtUtil.validateToken(jwt, email)) {
        	    // 1. Extract the role from token (e.g., "STAFF")
        	    String role = jwtUtil.extractRole(jwt); 
        	    
        	    // 2. Create Authority list (Spring Security needs this to check permissions)
        	    var authorities = java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(role));

        	    // 3. Pass 'authorities' instead of 'new ArrayList<>()'
        	    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        	            email, null, authorities);
        	    
        	    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        	    
        	    // 4. Set the authentication
        	    SecurityContextHolder.getContext().setAuthentication(authToken);

        	    System.out.println("User authenticated with role: " + role);
        	}
        }
        // 3. Continue the request chain
        chain.doFilter(request, response);
    }
}