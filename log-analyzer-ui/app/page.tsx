"use client";
// app/page.tsx
// Main Security Log Analyzer dashboard.
// Handles the full user journey: upload → loading → results.

import { useState, useCallback } from "react";
import { analyzeLogFile } from "@/lib/api";
import { AnalysisResult, ApiError } from "@/types/analysis";
import FileUploadZone from "@/components/FileUploadZone";
import StatCard from "@/components/StatCard";
import ThreatTable from "@/components/ThreatTable";
import SqlAlertList from "@/components/SqlAlertList";

// ── Risk score colour mapping ─────────────────────────────────────────────────
// v2: Uses softer Tailwind colour tokens (rose, amber, emerald) instead of
// raw hex neon values, improving readability on charcoal backgrounds.
function getRiskColour(score: number): string {
  if (score >= 75) return "text-rose-400";
  if (score >= 40) return "text-amber-400";
  return "text-emerald-400";
}

function getRiskLabel(score: number): string {
  if (score >= 75) return "CRITICAL";
  if (score >= 40) return "ELEVATED";
  if (score >= 10) return "MODERATE";
  return "CLEAN";
}

// ── Component ─────────────────────────────────────────────────────────────────
export default function DashboardPage() {
  const [result, setResult]     = useState<AnalysisResult | null>(null);
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState<string | null>(null);
  const [fileName, setFileName] = useState<string | null>(null);

  // Called by FileUploadZone when the user picks or drops a file
  const handleFileSelected = useCallback(async (file: File) => {
    setLoading(true);
    setError(null);
    setResult(null);
    setFileName(file.name);

    try {
      const data = await analyzeLogFile(file);
      setResult(data);
    } catch (err) {
      // err may be an ApiError (structured) or a plain Error (network)
      const apiErr = err as ApiError;
      setError(apiErr.detail ?? (err as Error).message ?? "Unknown error occurred.");
    } finally {
      setLoading(false);
    }
  }, []);

  const handleReset = () => {
    setResult(null);
    setError(null);
    setFileName(null);
  };

  // ── Render ──────────────────────────────────────────────────────────────────
  return (
    <main className="min-h-screen px-6 py-10 md:px-10 lg:px-16 max-w-7xl mx-auto flex flex-col justify-start">

      {/* ── Landing Container ─────────────────────────────────────────────────
          When no result is loaded, center the content both vertically and
          horizontally to eliminate empty white space and fix bad proportions.
      ─────────────────────────────────────────────────────────────────────── */}
      <div className={!result ? "max-w-3xl mx-auto w-full min-h-[75vh] flex flex-col justify-center py-6 animate-[slide-up_0.3s_ease]" : "w-full"}>

        {/* ── Header ─────────────────────────────────────────────────────────── */}
        <header className={`mb-10 ${!result ? "text-center" : ""}`}>
          {/* Top status bar — mono only for the system label (data identity) */}
          <div className={`flex items-center gap-3 mb-4 ${!result ? "justify-center" : ""}`}>
            <span className="font-mono text-[11px] tracking-[0.25em] uppercase text-emerald-500/60">
              THREAT · INTELLIGENCE · PLATFORM
            </span>
            {result && <div className="flex-1 h-px bg-slate-800" />}
            <span className="font-mono text-[11px] text-slate-600">v2.0.0</span>
          </div>

          <h1 className="text-4xl md:text-5xl font-semibold tracking-tight text-slate-100 leading-tight">
            Security{" "}
            <span className="text-emerald-400 glow-accent">Log Analyzer</span>
          </h1>
          {/* Descriptor in Inter — much easier to read than full-page mono */}
          <p className={`mt-4 text-slate-400 text-base leading-relaxed max-w-xl ${!result ? "mx-auto" : ""}`}>
            Upload a server log file. The engine scans for brute-force login
            attempts and SQL-injection probes, then generates a structured threat report.
          </p>
        </header>

        {/* ── Upload zone ──────────────────────────────────────────────────────── */}
        {!result && (
          <section className="w-full">
            <FileUploadZone
              onFileSelected={handleFileSelected}
              loading={loading}
            />

            {/* Error state */}
            {error && (
              <div
                role="alert"
                className="mt-5 flex items-start text-left gap-3 rounded-xl border border-rose-500/20
                           bg-rose-500/5 px-5 py-4 text-rose-400
                           animate-[slide-up_0.3s_ease]"
              >
                <span className="text-base leading-none mt-0.5">⚠</span>
                <div>
                  <p className="font-semibold text-sm text-rose-300">Analysis Failed</p>
                  <p className="text-slate-400 text-sm mt-1 leading-relaxed">{error}</p>
                </div>
              </div>
            )}
          </section>
        )}
      </div>

      {/* ── Results dashboard ─────────────────────────────────────────────────── */}
      {result && (
        <div className="animate-[slide-up_0.4s_ease] w-full">

          {/* Results header + reset */}
          <div className="flex flex-wrap items-center justify-between gap-4 mb-8
                          pb-6 border-b border-slate-800">
            <div>
              {/* Mono for timestamp — it's data */}
              <p className="font-mono text-xs text-slate-500 mb-1.5 tracking-wider">
                ANALYSIS COMPLETE ·{" "}
                {new Date(result.analyzedAt).toLocaleString()}
              </p>
              <h2 className="text-xl font-semibold text-slate-100">
                Report:{" "}
                <span className="font-mono text-base text-slate-400">{fileName}</span>
              </h2>
            </div>
            <button
              onClick={handleReset}
              className="px-4 py-2 border border-slate-700 text-slate-300 text-sm
                         rounded-lg hover:border-emerald-500/40 hover:text-emerald-400
                         hover:bg-emerald-500/5 active:scale-95
                         transition-all duration-150 cursor-pointer"
            >
              ← New Analysis
            </button>
          </div>

          {/* ── Stat cards row ───────────────────────────────────────────────── */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-5 mb-6">
            <StatCard
              label="Risk Score"
              value={`${result.riskScore}/100`}
              sub={getRiskLabel(result.riskScore)}
              valueClass={getRiskColour(result.riskScore)}
              accent
            />
            <StatCard
              label="Lines Scanned"
              value={result.totalLinesScanned.toLocaleString()}
              sub="total log entries"
            />
            <StatCard
              label="Suspicious IPs"
              value={result.suspiciousIpCount}
              sub="brute-force suspects"
              valueClass={result.suspiciousIpCount > 0 ? "text-rose-400" : "text-emerald-400"}
            />
            <StatCard
              label="SQL Probes"
              value={result.sqlInjectionCount}
              sub="injection attempts"
              valueClass={result.sqlInjectionCount > 0 ? "text-amber-400" : "text-emerald-400"}
            />
          </div>

          {/* ── Risk score bar ────────────────────────────────────────────────── */}
          <div className="mb-8 p-5 rounded-xl border border-slate-800 bg-slate-900/60">
            <div className="flex justify-between text-xs text-slate-500 mb-3 font-medium tracking-wider uppercase">
              <span>Threat Level</span>
              <span className={`font-mono font-medium ${getRiskColour(result.riskScore)}`}>
                {result.riskScore}%
              </span>
            </div>
            <div className="h-2 bg-slate-800 rounded-full overflow-hidden">
              <div
                className="h-full rounded-full transition-all duration-1000"
                style={{
                  width: `${result.riskScore}%`,
                  background: result.riskScore >= 75
                    ? "linear-gradient(90deg, #f43f5e, #fb7185)"
                    : result.riskScore >= 40
                    ? "linear-gradient(90deg, #f59e0b, #fbbf24)"
                    : "linear-gradient(90deg, #10b981, #34d399)",
                }}
              />
            </div>
            {/* Risk label beneath the bar for context */}
            <p className="mt-2 text-xs text-slate-500 leading-relaxed">
              {result.riskScore >= 75
                ? "Immediate review required — multiple high-severity indicators detected."
                : result.riskScore >= 40
                ? "Elevated activity detected — manual investigation recommended."
                : "Low risk — log file shows minimal suspicious activity."}
            </p>
          </div>

          {/* ── Suspicious IPs table ─────────────────────────────────────────── */}
          {result.suspiciousIps.length > 0 && (
            <section className="mb-8">
              <SectionHeading icon="🔴" label="Brute-Force Suspects" count={result.suspiciousIpCount} />
              <ThreatTable ips={result.suspiciousIps} />
            </section>
          )}

          {/* ── SQL Injection alerts ──────────────────────────────────────────── */}
          {result.sqlInjectionAlerts.length > 0 && (
            <section className="mb-8">
              <SectionHeading icon="🟡" label="SQL Injection Probes" count={result.sqlInjectionCount} />
              <SqlAlertList alerts={result.sqlInjectionAlerts} />
            </section>
          )}

          {/* ── All-clear state ───────────────────────────────────────────────── */}
          {result.suspiciousIps.length === 0 && result.sqlInjectionAlerts.length === 0 && (
            <div className="flex flex-col items-center justify-center py-20 text-center">
              <div className="text-5xl mb-5">✅</div>
              <h3 className="text-2xl font-semibold text-emerald-400 glow-accent mb-2">
                No Threats Detected
              </h3>
              <p className="text-slate-400 text-sm leading-relaxed">
                {result.totalLinesScanned.toLocaleString()} lines scanned — log file appears clean.
              </p>
            </div>
          )}
        </div>
      )}
    </main>
  );
}

// ── Local helper component ────────────────────────────────────────────────────
function SectionHeading({ icon, label, count }: {
  icon: string;
  label: string;
  count: number;
}) {
  return (
    <div className="flex items-center gap-3 mb-4">
      <span className="text-base">{icon}</span>
      {/* Section title in Inter — clean, professional */}
      <h3 className="text-base font-semibold text-slate-200">{label}</h3>
      <span className="font-mono text-xs text-slate-400 bg-slate-800 px-2.5 py-0.5 rounded-full border border-slate-700">
        {count}
      </span>
      <div className="flex-1 h-px bg-slate-800" />
    </div>
  );
}