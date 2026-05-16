// types/analysis.ts

export interface SuspiciousIpDto {
  ipAddress: string;
  failedAttempts: number;
}

export interface SqlInjectionAlertDto {
  lineNumber: number;       // Java: lineNumber
  rawLogLine: string;       // Java: rawLogLine
  sourceIp: string | null;  // Java: sourceIp
  matchedPattern: string;   // Java: matchedPattern
  suspiciousUrl: string;    // Java: suspiciousUrl
}

export interface AnalysisResult {
  riskScore: number;
  totalLinesScanned: number;
  suspiciousIpCount: number;
  sqlInjectionCount: number;
  analyzedAt: string;
  suspiciousIps: SuspiciousIpDto[];
  sqlInjectionAlerts: SqlInjectionAlertDto[];
}

export interface ApiError {
  title?: string;
  status?: number;
  detail?: string;
}