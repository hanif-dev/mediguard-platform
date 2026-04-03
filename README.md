# 🏥 MediGuard Deutschland

**DSGVO-konformes Patientenverwaltungs- und Sicherheitsportal für das deutsche Gesundheitswesen**

---

## Übersicht

MediGuard Deutschland ist eine vollständige Web-Applikation für Kliniken und medizinische Einrichtungen in Deutschland, entwickelt nach den gesetzlichen Anforderungen des deutschen Gesundheitswesens.

| Gesetzliche Grundlage | Abdeckung |
|---|---|
| **§630f BGB** | Dokumentationspflicht — Akten nur Soft-Delete |
| **§33 DSGVO** | 72h-Meldepflicht bei Datenpannen an BSI |
| **BSI IT-Grundschutz** | Sicherheitsvorfälle, Audit-Protokoll |
| **KHZG** | Krankenhauszukunftsgesetz-konform |

---

## Tech Stack (High Demand Deutschland)

| Layer | Technologie | Warum Deutschland |
|---|---|---|
| Backend | **Java 21 LTS + Spring Boot 3.2** | Standard in Kliniken, Banken, SIer |
| Build | **Maven** | Gängiger als Gradle im Enterprise |
| Security | **Spring Security 6 + JWT** | OWASP-konform |
| ORM | **Spring Data JPA + Hibernate** | Enterprise-Standard |
| API Docs | **SpringDoc OpenAPI 3 (Swagger)** | Automatische Dokumentation |
| DB Dev | **H2** (zero config) | Sofort lauffähig ohne Installation |
| DB Prod | **PostgreSQL** | Standard in Deutschland |
| Frontend | **React 18 + TypeScript** | Meistgefragte Kombination |
| Styling | **Tailwind CSS** | Rapid Enterprise UI |
| Deploy | **Render.com + Netlify** | Kostenlos, Codespace-kompatibel |

---

## Demo-Zugänge

| Rolle | Benutzername | Passwort | Berechtigungen |
|---|---|---|---|
| Admin | `admin` | `MediGuard2024!` | Alle |
| Arzt | `dr.mueller` | `Arzt2024!` | Patienten, Akten, Vorfälle |
| Pfleger | `pfleger.schmidt` | `Pflege2024!` | Patienten, Akten lesen |
| Verwaltung | `verwaltung` | `Verwalt2024!` | Patienten, Dashboard |

---

## Features

### 🏥 Patientenverwaltung
- Vollständige CRUD-Operationen mit automatischer Patienten-Nr. (P-2024-XXXX)
- Versicherungstypen: Gesetzlich, Privat, Berufsgenossenschaft
- Suchfunktion nach Name
- Rollenbasierte Zugriffssteuerung (RBAC)

### 📋 Krankenakten (§630f BGB)
- Unveränderliche Dokumentation (Soft-Delete only — kein hartes Löschen)
- ICD-10 Code Unterstützung
- Eintragstypen: Anamnese, Diagnose, Behandlung, Medikation, Labor, Bildgebung, Entlassung, Notiz
- Vertraulichkeitsflag
- Audit-Trail: wer hat wann was erstellt

### 🚨 Sicherheitsvorfälle (§33 DSGVO)
- Vorfallkategorien: Datenpanne, Phishing, Ransomware, Unbefugter Zugriff, etc.
- Schweregrade: Niedrig, Mittel, Hoch, Kritisch
- **DSGVO §33 Meldepflicht-Tracking** (72h-Frist an BSI)
- Workflow: Offen → In Bearbeitung → Gelöst → Geschlossen
- BSI-Meldungsbestätigung

### 🔍 Audit-Protokoll (DSGVO Art. 5)
- Automatische Protokollierung aller Systemzugriffe
- Unveränderlich, nur Admin-Zugang
- DSGVO-Nachweispflicht erfüllt

---

## Schnellstart (Lokal)

### Option A: GitHub Codespace (empfohlen)
Kein lokales Setup nötig — alles im Browser!
→ Siehe Tutorial-DOCX

### Option B: Lokal mit Maven

```bash
# Backend
cd backend
mvn spring-boot:run
# → http://localhost:8080/swagger-ui.html
# → http://localhost:8080/h2-console

# Frontend (neues Terminal)
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

---

## API Dokumentation

Nach dem Start verfügbar unter:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Wichtige Endpunkte

```
POST /api/v1/auth/login          → JWT Token
POST /api/v1/auth/register       → Neuer Benutzer

GET  /api/v1/patienten           → Alle Patienten
POST /api/v1/patienten           → Patient anlegen

GET  /api/v1/krankenakten/patient/{id}  → Akten eines Patienten
POST /api/v1/krankenakten               → Neuer Eintrag (§630f BGB)

GET  /api/v1/vorfaelle           → Alle Vorfälle
POST /api/v1/vorfaelle           → Vorfall melden
GET  /api/v1/vorfaelle/dsgvo-ausstehend → BSI-Meldungen offen

GET  /api/v1/dashboard/stats     → Dashboard-Statistiken
GET  /api/v1/audit               → Audit-Protokoll (nur Admin)
```

---

## Deployment

### Backend → Render.com

```
Root Directory:  backend
Build Command:   mvn clean package -DskipTests
Start Command:   java -jar target/mediguard-backend-1.0.0.jar
Environment:     JAVA_VERSION=21
```

### Frontend → Netlify

```bash
# Build mit Backend-URL
VITE_API_URL=https://mediguard-api.onrender.com npm run build
# Dann dist/ Ordner zu Netlify hochladen
```

---

## Erstellt von

**Hanif K** | Data Scientist & CTI Analyst  
YSEALI CyberSafe ASEAN Alumni | IBM Cybersecurity Certified
