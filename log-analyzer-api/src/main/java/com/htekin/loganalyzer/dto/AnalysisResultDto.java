package com.htekin.loganalyzer.dto;

import com.htekin.loganalyzer.dto.SqlInjectionAlert;
import com.htekin.loganalyzer.dto.SuspiciousIp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Data Transfer Object returned by the {@code /api/logs/analyze} endpoint.
 *
 * <p>This is the sole public contract between the backend and the Next.js frontend.
 * Keep field names stable; any breaking change requires a version bump.
 *
 * <p>Example JSON shape:
 * <pre>{@code
 * {
 *   "analyzedAt": "2024-05-16T10:00:00Z",
 *   "totalLinesScanned": 1540,
 *   "suspiciousIpCount": 3,
 *   "sqlInjectionCount": 7,
 *   "riskScore": 82,
 *   "suspiciousIps": [ ... ],
 *   "sqlInjectionAlerts": [ ... ]
 * }
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultDto {

    /** ISO-8601 timestamp of when this analysis was performed. */
    private Instant analyzedAt;

    /** Total number of lines read from the uploaded log file. */
    private int totalLinesScanned;

    /** Number of distinct IPs that exceeded the brute-force threshold. */
    private int suspiciousIpCount;

    /** Number of log lines matching SQL-injection patterns. */
    private int sqlInjectionCount;

    /**
     * Composite risk score from 0 (clean) to 100 (critical).
     * Calculated by {@link com.htekin.loganalyzer.service.LogAnalyzerService}.
     */
    private int riskScore;

    /** Ordered list of brute-force suspects (highest failed-login count first). */
    private List<SuspiciousIp> suspiciousIps;

    /** All detected SQL-injection probe attempts, in file order. */
    private List<SqlInjectionAlert> sqlInjectionAlerts;
}