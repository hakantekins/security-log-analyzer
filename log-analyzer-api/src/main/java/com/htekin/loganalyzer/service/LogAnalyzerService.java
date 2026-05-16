package com.htekin.loganalyzer.service;

import com.htekin.loganalyzer.dto.AnalysisResultDto;
import com.htekin.loganalyzer.exception.InvalidLogFileException;
import com.htekin.loganalyzer.dto.SqlInjectionAlert;
import com.htekin.loganalyzer.dto.SuspiciousIp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestration service that coordinates the full log-analysis pipeline.
 *
 * <p><b>Responsibilities</b>
 * <ol>
 *   <li>Validate the incoming {@link MultipartFile}</li>
 *   <li>Read every line from the file into memory</li>
 *   <li>Delegate threat detection to {@link LogParserService}</li>
 *   <li>Compute a composite risk score</li>
 *   <li>Assemble and return the {@link AnalysisResultDto}</li>
 * </ol>
 *
 * <p>This class does <em>not</em> contain any regex or HTTP-related logic
 * (Open/Closed Principle: extend detection rules in LogParserService only).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogAnalyzerService {

    private final LogParserService logParserService;

    // Maximum file size accepted (50 MB, also enforced in application.yml)
    private static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024;

    /**
     * Full analysis pipeline entry point.
     *
     * @param file the multipart log file uploaded by the user
     * @return structured {@link AnalysisResultDto} ready for JSON serialisation
     * @throws InvalidLogFileException if the file is empty, too large, or unreadable
     */
    public AnalysisResultDto analyze(MultipartFile file) {
        log.info("Starting analysis for file: {} ({} bytes)",
                file.getOriginalFilename(), file.getSize());

        // ── Step 1: Validate ──────────────────────────────────────────────────
        validateFile(file);

        // ── Step 2: Read lines ────────────────────────────────────────────────
        List<String> lines = readLines(file);
        log.debug("Read {} lines from {}", lines.size(), file.getOriginalFilename());

        // ── Step 3: Detect threats ────────────────────────────────────────────
        List<SuspiciousIp> suspiciousIps = logParserService.detectBruteForce(lines);
        List<SqlInjectionAlert> sqlAlerts = logParserService.detectSqlInjection(lines);

        // ── Step 4: Score & assemble ──────────────────────────────────────────
        int riskScore = computeRiskScore(suspiciousIps, sqlAlerts, lines.size());

        AnalysisResultDto result = AnalysisResultDto.builder()
                .analyzedAt(Instant.now())
                .totalLinesScanned(lines.size())
                .suspiciousIpCount(suspiciousIps.size())
                .sqlInjectionCount(sqlAlerts.size())
                .riskScore(riskScore)
                .suspiciousIps(suspiciousIps)
                .sqlInjectionAlerts(sqlAlerts)
                .build();

        log.info("Analysis complete — score: {}, IPs: {}, SQL alerts: {}",
                riskScore, suspiciousIps.size(), sqlAlerts.size());
        return result;
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    /**
     * Validates file presence, size, and extension.
     *
     * @throws InvalidLogFileException on any validation failure
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidLogFileException("Uploaded file is empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidLogFileException(
                    "File exceeds the 50 MB size limit. Size: " + file.getSize() + " bytes.");
        }

        String name = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase()
                : "";

        if (!name.endsWith(".txt") && !name.endsWith(".log")) {
            throw new InvalidLogFileException(
                    "Unsupported file type. Only .txt and .log files are accepted.");
        }
    }

    /**
     * Reads the uploaded file line-by-line using UTF-8 encoding.
     *
     * @throws InvalidLogFileException if the file cannot be read
     */
    private List<String> readLines(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            return reader.lines().collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Failed to read uploaded file", e);
            throw new InvalidLogFileException("Could not read the uploaded file: " + e.getMessage());
        }
    }

    /**
     * Computes a composite 0–100 risk score.
     *
     * <p>Formula (tunable):
     * <ul>
     *   <li>Each suspicious IP contributes up to 10 points (capped at 50)</li>
     *   <li>Each SQL alert contributes up to 5 points (capped at 40)</li>
     *   <li>High total line count adds up to 10 "noise" points</li>
     * </ul>
     */
    private int computeRiskScore(List<SuspiciousIp> ips,
                                 List<SqlInjectionAlert> sqlAlerts,
                                 int totalLines) {
        int ipScore  = Math.min(ips.size() * 10, 50);
        int sqlScore = Math.min(sqlAlerts.size() * 5, 40);
        int noiseScore = totalLines > 10_000 ? 10 : 0;

        return Math.min(ipScore + sqlScore + noiseScore, 100);
    }
}