package com.acme.salary.auth;

import jakarta.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * One HR Manager account protects every API endpoint.
 *
 * <p>Requests carry HTTP Basic credentials and the server keeps no session. Without cookies
 * there is nothing for CSRF to exploit, and the frontend can live on a different domain.
 */
@Configuration
public class SecurityConfig {

    static final String ROLE = "HR_MANAGER";

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                .build();
    }

    /**
     * Answers 401 as problem JSON. No {@code WWW-Authenticate} header is sent,
     * so browsers show the app's sign-in page instead of their own pop-up.
     */
    private static AuthenticationEntryPoint unauthorized() {
        return (request, response, e) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.getWriter().write("""
                    {"type":"about:blank","title":"Unauthorized","status":401,\
                    "detail":"Sign in with the HR Manager account"}""");
        };
    }

    /**
     * The single account. With no password configured, a random one is generated and logged,
     * so a deployment can never run with a known default password.
     */
    @Bean
    UserDetailsService users(@Value("${app.auth.username:hr}") String username,
                             @Value("${app.auth.password:}") String password,
                             PasswordEncoder encoder) {
        String effectivePassword = password;
        if (password.isBlank()) {
            effectivePassword = randomPassword();
            log.warn("No app.auth.password set. Generated password for '{}': {}", username, effectivePassword);
        }
        return new InMemoryUserDetailsManager(User.withUsername(username)
                .password(encoder.encode(effectivePassword))
                .roles(ROLE)
                .build());
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Allows the deployed frontend's origin(s); allows all origins when not configured. */
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins:}") String origins) {
        CorsConfiguration cors = new CorsConfiguration();
        List<String> allowedOrigins = Arrays.stream(origins.split(",")).map(String::trim).filter(o -> !o.isEmpty()).toList();
        if (allowedOrigins.isEmpty()) {
            cors.setAllowedOriginPatterns(List.of("*"));
        } else {
            cors.setAllowedOrigins(allowedOrigins);
        }
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cors);
        return source;
    }

    private static String randomPassword() {
        byte[] bytes = new byte[18];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
