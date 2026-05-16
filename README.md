# 🛡️ Security Log Analyzer Platform

A full-stack security tool that detects brute-force login attempts and SQL injection probes in server log files. Upload any `.txt` or `.log` file and receive an instant, responsive threat intelligence report.

*Sunucu log dosyalarında brute-force (kaba kuvvet) giriş girişimlerini ve SQL enjeksiyon taramalarını tespit eden full-stack bir siber güvenlik aracıdır. Herhangi bir `.txt` veya `.log` dosyasını yükleyerek anında tehdit istihbarat raporu almanızı sağlar.*

---

## ✨ Features / Özellikler

| Feature / Özellik | Description / Açıklama |
| :--- | :--- |
| **🔍 Brute-Force Detection** | Flags IPs with ≥5 failed login events; assigns LOW/MEDIUM/HIGH/CRITICAL risk. / *≥5 başarısız giriş yapan IP'leri işaretler; risk seviyesi atar.* |
| **💉 SQL Injection Detection** | Detects `DROP TABLE`, `UNION SELECT`, `OR 1=1` via strict regex patterns. / *Zararlı SQL payload'larını gelişmiş regex şablonları ile yakalar.* |
| **📊 Enterprise Dashboard** | Eye-strain reduced, corporate Mint Green & Deep Charcoal interactive UI. / *Gözü yormayan, kurumsal Nane Yeşili ve Derin Kömür tonlarında interaktif arayüz.* |
| **📁 Smart Fallback Parser** | Client-side regex extracts source IPs even if the backend parser returns null. / *Backend kaynak IP'yi null dönse bile logdan IP'yi cımbızlayan akıllı ön yüz.* |
| **⚡ RFC 7807 & Swagger** | Structured JSON error responses (Problem Detail) and auto-generated API docs. / *Standart siber güvenlik hata dökümleri ve otomatik API dokümantasyonu.* |

---

## 🗂️ Project Structure / Proje Yapısı

```text
security-log-analyzer/
├── log-analyzer-api/   → Spring Boot 3 / Java 17 (Backend Architecture)
└── log-analyzer-ui/    → Next.js 16 / TypeScript / Tailwind CSS v4 (Frontend)