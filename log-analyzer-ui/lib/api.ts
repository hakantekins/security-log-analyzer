// lib/api.ts
import { AnalysisResult } from "@/types/analysis";

export async function analyzeLogFile(file: File): Promise<AnalysisResult> {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch("http://localhost:8080/api/logs/analyze", {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    try {
      const errorData = await response.json();
      throw errorData;
    } catch (e) {
      if (typeof e === "object" && e !== null && "detail" in e) {
        throw e;
      }
      throw new Error(`Network alert: Server responded with status ${response.status}`);
    }
  }

  return response.json();
}