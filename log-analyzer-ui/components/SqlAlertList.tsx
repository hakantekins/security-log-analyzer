// components/SqlAlertList.tsx
import { SqlInjectionAlertDto } from "@/types/analysis";

interface SqlAlertListProps {
  alerts: SqlInjectionAlertDto[];
}

export default function SqlAlertList({ alerts }: SqlAlertListProps) {
  // Backend'den IP null gelirse, log metninden akıllıca cımbızlayan yardımcı fonksiyon
  const getDisplayIp = (rawLine: string, sourceIp: string | null): string => {
    if (sourceIp) return sourceIp;
    const ipMatch = rawLine.match(/\b(?:\d{1,3}\.){3}\d{1,3}\b/);
    return ipMatch ? ipMatch[0] : "Unknown IP";
  };

  return (
    <div className="space-y-4">
      {alerts.map((alert, index) => (
        <div
          key={index}
          className="group rounded-xl border border-slate-800/80 bg-slate-900/10 p-5 hover:border-amber-500/20 transition-all duration-200"
        >
          <div className="flex flex-wrap items-center justify-between gap-3 mb-3 text-xs font-sans">
            <div className="flex items-center gap-2.5">
              <span className="inline-flex items-center rounded-md bg-amber-500/10 px-2 py-0.5 text-xs font-medium text-amber-400 border border-amber-500/10">
                SQLi Payload
              </span>
              <span className="text-slate-500 font-medium font-mono">
                Line #{alert.lineNumber}
              </span>
            </div>
            <div className="text-slate-400 font-mono bg-slate-900/50 px-2 py-0.5 rounded border border-slate-800/60">
              Source: {getDisplayIp(alert.rawLogLine, alert.sourceIp)}
            </div>
          </div>
          <div className="overflow-x-auto rounded-lg bg-slate-950/80 p-3.5 border border-slate-900/60 font-mono text-xs text-amber-300/90 leading-relaxed whitespace-pre-wrap break-all selection:bg-amber-500/20">
            {alert.rawLogLine}
          </div>
        </div>
      ))}
    </div>
  );
}