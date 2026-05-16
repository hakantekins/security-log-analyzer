package com.htekin.loganalyzer.controller;

import com.htekin.loganalyzer.dto.AnalysisResultDto;
import com.htekin.loganalyzer.service.LogAnalyzerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller exposing the log-analysis API.
 *
 * <p>All endpoints live under {@code /api/logs}.
 * The controller is intentionally thin — it only handles HTTP concerns
 * (binding, response codes) and delegates every business decision to
 * {@link LogAnalyzerService}.
 *
 * <p>Swagger UI: {@code http://localhost:8080/swagger-ui.html}
 */
@Slf4j
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "Log Analyzer", description = "Upload and analyse server log files for security threats")
public class LogAnalyzerController {

    private final LogAnalyzerService logAnalyzerService;

    // ── POST /api/logs/analyze ─────────────────────────────────────────────────

    /**
     * Accepts a plain-text log file and returns a full threat analysis report.
     *
     * @param file the server_logs.txt (or .log) file to analyse
     * @return {@link AnalysisResultDto} with detected IPs, SQL patterns, and a risk score
     */
    @Operation(
            summary     = "Analyse a server log file",
            description = "Upload a .txt or .log file. The API scans for brute-force "
                    + "login attempts and SQL-injection probes, then returns a "
                    + "structured JSON report including a composite risk score."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analysis successful",
                    content = @Content(schema = @Schema(implementation = AnalysisResultDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file (empty, wrong type, too large)",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content)
    })
    @PostMapping(
            value    = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AnalysisResultDto> analyzeLog(
            @Parameter(description = "Server log file (.txt or .log, max 50 MB)",
                    required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("Received analysis request for file: {}", file.getOriginalFilename());

        AnalysisResultDto result = logAnalyzerService.analyze(file);
        return ResponseEntity.ok(result);
    }

    // ── GET /api/logs/health ───────────────────────────────────────────────────

    /**
     * Simple liveness probe — useful for Docker health checks and CI pipelines.
     */
    @Operation(summary = "Health check", description = "Returns 200 OK when the service is running.")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Security Log Analyzer is operational.");
    }
}