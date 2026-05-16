package com.htekin.loganalyzer.service;

import com.htekin.loganalyzer.dto.SqlInjectionAlert;
import com.htekin.loganalyzer.dto.SuspiciousIp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Low-level log parsing service.
 *
 * <p><b>Single Responsibility:</b> this class is only concerned with extracting
 * structured threat data from raw log lines. It knows nothing about file I/O,
 * HTTP, or the response DTO — those concerns live in other layers.
 *
 * <p>All regex patterns are pre-compiled as constants for performance.
 */
@Slf4j
@Service
public class LogParserService {

    // ── Regex Patterns ─────────────────────────────────────────────────────────

    /**
     * Matches a standard Apache/Nginx combined-log IP at the start of the line.
     * Example: 192.168.1.101 - frank [10/Oct/2000:13:55:36 -0700] ...
     */
    private static final Pattern IP_PATTERN =
            Pattern.compile("^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");

    /**
     * Detects failed authentication events.
     * Covers: "FAILED LOGIN", "authentication failure", "Invalid password", "401".
     */
    private static final Pattern FAILED_LOGIN_PATTERN =
            Pattern.compile(
                    "FAILED LOGIN|authentication failure|Invalid password|\\b401\\b",
                    Pattern.CASE_INSENSITIVE);

    /**
     * SQL-injection keyword detector.
     * Ordered from most dangerous to least, so the first match wins.
     */
    private static final Pattern SQL_INJECTION_PATTERN =
            Pattern.compile(
                    "DROP\\s+(?:TABLE|DATABASE)|"
                            + "UNION\\s+SELECT|"
                            + "OR\\s+'?1'?\\s*=\\s*'?1|"
                            + "SELECT\\s+\\*|"
                            + "INSERT\\s+INTO|"
                            + "xp_cmdshell|"
                            + "EXEC(?:UTE)?\\s*\\(|"
                            + ";\\s*--",
                    Pattern.CASE_INSENSITIVE);

    /**
     * Tries to extract a URL or request path from the log line.
     * Matches the first quoted string containing a slash (HTTP request notation).
     */
    private static final Pattern URL_PATTERN =
            Pattern.compile("\"([^\"]*?/[^\"]*?)\"");

    /**
     * Matches an optional timestamp at the start of the line
     * (ISO-8601 or Apache bracket notation).
     */
    private static final Pattern TIMESTAMP_PATTERN =
            Pattern.compile(
                    "\\[(\\d{2}/\\w+/\\d{4}[^]]*)]|"
                            + "(\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2})");

    // ── Configurable threshold ─────────────────────────────────────────────────

    /** Minimum number of failed logins that turns an IP into a suspect. */
    @Value("${app.security.brute-force-threshold:5}")
    private int bruteForceThreshold;

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Scans every line of the log file and returns a list of IPs that exceeded
     * the brute-force threshold, sorted by failed-login count (descending).
     *
     * @param lines all lines from the uploaded log file
     * @return list of {@link SuspiciousIp} objects, may be empty
     */
    public List<SuspiciousIp> detectBruteForce(List<String> lines) {

        // ip → list of timestamps for that IP's failed-login events
        Map<String, List<String>> failedLoginMap = new LinkedHashMap<>();

        for (String line : lines) {
            if (!FAILED_LOGIN_PATTERN.matcher(line).find()) continue;

            String ip = extractIp(line);
            if (ip == null) continue;

            String timestamp = extractTimestamp(line);
            failedLoginMap.computeIfAbsent(ip, k -> new ArrayList<>()).add(timestamp);
        }

        return failedLoginMap.entrySet().stream()
                .filter(e -> e.getValue().size() >= bruteForceThreshold)
                .map(e -> buildSuspiciousIp(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingInt(SuspiciousIp::getFailedLoginCount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Scans every line for SQL-injection–like patterns.
     *
     * @param lines all lines from the uploaded log file
     * @return list of {@link SqlInjectionAlert} objects, in file order
     */
    public List<SqlInjectionAlert> detectSqlInjection(List<String> lines) {
        List<SqlInjectionAlert> alerts = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher sqlMatcher = SQL_INJECTION_PATTERN.matcher(line);

            if (sqlMatcher.find()) {
                alerts.add(SqlInjectionAlert.builder()
                        .lineNumber(i + 1)                      // 1-based
                        .sourceIp(extractIp(line))
                        .suspiciousUrl(extractUrl(line))
                        .matchedPattern(sqlMatcher.group().toUpperCase())
                        .rawLogLine(truncate(line, 300))         // cap line length
                        .build());
            }
        }

        log.info("SQL injection scan complete — {} alerts found in {} lines",
                alerts.size(), lines.size());
        return alerts;
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    /** Builds a {@link SuspiciousIp} and assigns a qualitative risk level. */
    private SuspiciousIp buildSuspiciousIp(String ip, List<String> timestamps) {
        int count = timestamps.size();
        return SuspiciousIp.builder()
                .ipAddress(ip)
                .failedLoginCount(count)
                .riskLevel(calculateRiskLevel(count))
                .firstSeen(timestamps.get(0))
                .lastSeen(timestamps.get(timestamps.size() - 1))
                .build();
    }

    /**
     * Maps a failed-login count to a human-readable risk level.
     *
     * <ul>
     *   <li>&lt; 10  → LOW</li>
     *   <li>&lt; 20  → MEDIUM</li>
     *   <li>&lt; 50  → HIGH</li>
     *   <li>≥  50  → CRITICAL</li>
     * </ul>
     */
    private String calculateRiskLevel(int count) {
        if (count >= 50) return "CRITICAL";
        if (count >= 20) return "HIGH";
        if (count >= 10) return "MEDIUM";
        return "LOW";
    }

    /** Extracts the leading IP address from a log line, or {@code null}. */
    private String extractIp(String line) {
        Matcher m = IP_PATTERN.matcher(line.trim());
        return m.find() ? m.group(1) : null;
    }

    /** Extracts the first recognisable timestamp from a log line, or an empty string. */
    private String extractTimestamp(String line) {
        Matcher m = TIMESTAMP_PATTERN.matcher(line);
        if (m.find()) {
            return m.group(1) != null ? m.group(1) : m.group(2);
        }
        return "";
    }

    /** Extracts the first URL-like quoted token from a log line, or the line itself. */
    private String extractUrl(String line) {
        Matcher m = URL_PATTERN.matcher(line);
        return m.find() ? m.group(1) : line;
    }

    /** Truncates a string to {@code maxLen} characters and appends an ellipsis. */
    private String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "…";
    }
}