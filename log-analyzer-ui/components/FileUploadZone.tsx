// components/FileUploadZone.tsx
import { useState, DragEvent, ChangeEvent } from "react";

interface FileUploadZoneProps {
  onFileSelected: (file: File) => void;
  loading: boolean;
}

export default function FileUploadZone({ onFileSelected, loading }: FileUploadZoneProps) {
  const [isDragActive, setIsDragActive] = useState(false);

  const handleDrag = (e: DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setIsDragActive(true);
    } else if (e.type === "dragleave") {
      setIsDragActive(false);
    }
  };

  const handleDrop = (e: DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      onFileSelected(e.dataTransfer.files[0]);
    }
  };

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    e.preventDefault();
    if (e.target.files && e.target.files[0]) {
      onFileSelected(e.target.files[0]);
    }
  };

  return (
    <div
      onDragEnter={handleDrag}
      onDragOver={handleDrag}
      onDragLeave={handleDrag}
      onDrop={handleDrop}
      className={`relative flex flex-col items-center justify-center w-full h-60 border-2 border-dashed rounded-xl transition-all duration-200 p-6 text-center ${
        loading
          ? "bg-slate-900/20 border-slate-800 cursor-not-allowed"
          : isDragActive
          ? "border-emerald-500 bg-emerald-500/5 shadow-[0_0_30px_rgba(52,211,153,0.03)]"
          : "border-slate-800 bg-slate-900/30 hover:border-slate-700 hover:bg-slate-900/50 cursor-pointer"
      }`}
    >
      <input
        type="file"
        id="file-upload"
        className="hidden"
        accept=".txt,.log"
        disabled={loading}
        onChange={handleChange}
      />

      {loading ? (
        <div className="flex flex-col items-center gap-4">
          <div className="w-8 h-8 border-2 border-emerald-500/20 border-t-emerald-400 rounded-full animate-[spin-smooth_0.8s_linear]" />
          <div>
            <p className="text-sm font-medium text-slate-200 font-sans">Analyzing Log File...</p>
            <p className="text-xs text-slate-500 mt-1 font-sans">Parsing strings and computing risk heuristics</p>
          </div>
        </div>
      ) : (
        <label htmlFor="file-upload" className="w-full h-full flex flex-col items-center justify-center cursor-pointer">
          <div className="w-12 h-12 rounded-xl bg-slate-900/60 border border-slate-800 flex items-center justify-center text-xl text-slate-400 mb-4 shadow-sm">
            📄
          </div>
          <p className="text-sm font-medium text-slate-200 font-sans">
            <span className="text-emerald-400 hover:underline">Click to upload</span> or drag and drop
          </p>
          <p className="text-xs text-slate-500 mt-1.5 font-sans">
            Supports .txt and .log files up to 50 MB
          </p>
        </label>
      )}
    </div>
  );
}