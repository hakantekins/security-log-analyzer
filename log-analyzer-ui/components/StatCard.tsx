// components/StatCard.tsx
interface StatCardProps {
  label: string;
  value: string | number;
  sub: string;
  valueClass?: string;
  accent?: boolean;
}

export default function StatCard({ label, value, sub, valueClass = "text-slate-100", accent }: StatCardProps) {
  return (
    <div className={`p-5 rounded-xl border transition-all duration-200 ${
      accent
        ? "bg-slate-900/90 border-emerald-500/20 shadow-[0_4px_24px_rgba(16,185,129,0.04)]"
        : "bg-slate-900/40 border-slate-800/80"
    }`}>
      <p className="text-xs font-medium tracking-wider text-slate-500 uppercase font-sans">
        {label}
      </p>
      <p className={`text-2xl md:text-3xl font-semibold mt-2 tracking-tight font-mono ${valueClass}`}>
        {value}
      </p>
      <p className="text-xs text-slate-400 mt-1 font-sans truncate">
        {sub}
      </p>
    </div>
  );
}