package com.htekin.loganalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single IP address that has exceeded the failed-login threshold.
 *
 * <p>Instances are created by {@link com.htekin.loganalyzer.service.LogParserService}
 * after scanning all log lines, and surfaced through the REST API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspiciousIp {

    /** The IPv4 or IPv6 address that triggered the alert. */
    private String ipAddress;

    /** Total number of "FAILED LOGIN" events attributed to this IP. */
    private int failedLoginCount;

    /**
     * Qualitative risk level derived from the failed-login count.
     * Possible values: LOW, MEDIUM, HIGH, CRITICAL
     */
    private String riskLevel;

    /** Timestamp of the first observed failed login for this IP (raw log string). */
    private String firstSeen;

    /** Timestamp of the most recent failed login for this IP (raw log string). */
    private String lastSeen;
}