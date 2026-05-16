package com.htekin.loganalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single log line that contains an SQL-injection–like pattern.
 *
 * <p>Detected patterns include (but are not limited to):
 * <ul>
 *   <li>DROP TABLE / DROP DATABASE</li>
 *   <li>SELECT * FROM</li>
 *   <li>UNION SELECT</li>
 *   <li>OR 1=1 / OR '1'='1'</li>
 *   <li>xp_cmdshell, EXEC(, EXECUTE(</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlInjectionAlert {

    /** Source IP address extracted from the log line (maybe null if not parseable). */
    private String sourceIp;

    /** The HTTP request URL or query string that contains the suspicious payload. */
    private String suspiciousUrl;

    /** The specific SQL pattern keyword or phrase that triggered this alert. */
    private String matchedPattern;

    /** The full raw log line for auditing / evidence purposes. */
    private String rawLogLine;

    /** Line number within the uploaded file (1-based). */
    private int lineNumber;
}