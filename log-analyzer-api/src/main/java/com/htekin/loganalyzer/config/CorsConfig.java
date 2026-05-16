package com.htekin.loganalyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration.
 *
 * <p>Allows the Next.js dev server (port 3000) and production frontend
 * to call the Spring Boot API without browser CORS errors.
 *
 * <p><b>Production note:</b> replace {@code *} origins with your actual
 * deployed frontend domain before going live.
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        // Allow Next.js dev + any future deployed domain
                        .allowedOrigins(
                                "http://localhost:3000",
                                "http://localhost:3001"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false)
                        .maxAge(3600); // Cache preflight response for 1 hour
            }
        };
    }
}