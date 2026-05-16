package com.htekin.loganalyzer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc OpenAPI configuration.
 *
 * <p>Exposes API metadata visible in Swagger UI at
 * {@code http://localhost:8080/swagger-ui.html}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI securityLogOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Security Log Analyzer API")
                        .description("""
                                REST API for detecting security threats in server log files.
                                
                                **Supported threat types:**
                                - Brute-force / credential stuffing (repeated FAILED LOGIN events)
                                - SQL injection probes (DROP TABLE, UNION SELECT, OR 1=1, etc.)
                                
                                Upload a `.txt` or `.log` file and receive a structured JSON report.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Your Name")
                                .url("https://github.com/yourusername")
                                .email("you@example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")));
    }
}