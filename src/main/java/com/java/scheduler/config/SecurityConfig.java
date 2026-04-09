package com.java.scheduler.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor 
public class SecurityConfig {

    // The custom filter that checks for valid JWT tokens in request headers
    private final JwtRequestFilter jwtRequestFilter;
	
    // Bean to handle password encryption using the BCrypt hashing algorithm
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // The main security configuration where we define access rules
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Enable CORS using the settings defined in the corsConfigurationSource bean below
            .cors(Customizer.withDefaults())
            
            // 2. Disable CSRF protection because we are using stateless JWT tokens
            .csrf(csrf -> csrf.disable()) 
            
            // 3. Set session management to STATELESS (no JSESSIONID cookies will be created)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 4. Define which URL paths are public and which are restricted
            .authorizeHttpRequests(auth -> auth
                // Allow browser 'pre-flight' OPTIONS requests for all paths
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                
                // Permit everyone to access login, registration, and password reset endpoints
                .requestMatchers("/api/auth/**").permitAll() 
                
                // Restrict dashboard access to users with either ADMIN or STAFF roles
                .requestMatchers("/api/dashboard/**").hasAnyAuthority("ADMIN", "STAFF")
                
                // Every other request must be authenticated with a valid token
                .anyRequest().authenticated()
            )
            
            // Disable default Spring Security login forms and popups
            .httpBasic(basic -> basic.disable()) 
            .formLogin(form -> form.disable());
            
        // 5. Add our custom JWT filter to the security chain
        // It runs BEFORE the standard UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    // Settings to allow our React frontend (Vite) to communicate with this API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Trust the URL where the React application is running
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); 
        
        // Allow standard HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow specific headers needed for JWT authentication and JSON data
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        
        // Allow the browser to send cookies or auth headers if needed
        configuration.setAllowCredentials(true);
        
        // Ensure the frontend can see the 'Authorization' header in the server response
        configuration.setExposedHeaders(List.of("Authorization"));
        
        // Apply these CORS settings to every path in the application
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}