// components/ThreatTable.tsx
import { SuspiciousIpDto } from "@/types/analysis";

interface ThreatTableProps {
  ips: SuspiciousIpDto[];
}

export default function ThreatTable({ ips }: ThreatTableProps) {
  return (
    <div className="w-full overflow-hidden rounded-xl border border-slate-800/80 bg-slate-900/10">
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-slate-800 bg-slate-900/40 text-xs font-semibold tracking-wider text-slate-400 uppercase font-sans">
              <th className="px-6 py-4">IP Address</th>
              <th className="px-6 py-4 text-right">Failed Attempts</th>
              <th className="px-6 py-4 text-right">Action State</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/50 text-sm font-sans">
            {ips.map((item, index) => (
              <tr key={index} className="hover:bg-slate-900/30 transition-colors">
                <td className="px-6 py-4 font-mono text-slate-300 selection:bg-rose-500/20">
                  {item.ipAddress}
                </td>
                <td className="px-6 py-4 text-right font-mono text-rose-400 font-medium">
                  {item.failedAttempts}
                </td>
                <td className="px-6 py-4 text-right">
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-rose-500/10 px-2.5 py-0.5 text-xs font-medium text-rose-400 border border-rose-500/10">
                    Flagged
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}