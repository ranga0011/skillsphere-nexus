# SkillSphere Nexus

**Employee and Student Skill Management System using Angular, Spring Boot, and PostgreSQL**

SkillSphere Nexus is an enterprise web application designed to manage employee and student profiles, skill mappings, proficiency levels, assessments, automatic achievement certificates, learning resources, and competency frameworks in one unified platform. Existing employee, skill-catalog, assessment, framework, and external-certification workflows are seamlessly integrated with student self-service and MCQ assessment capabilities.

---

## Features

- **Employee & Student Management:** Distinct profiles, role separation (`HR_ADMIN`, `EMPLOYEE`, `STUDENT`), and self-service dashboards.
- **Skill Catalog & Mapping:** Maintain categorized skills, associate them with employees/students, and track verified proficiency.
- **MCQ Learning & Assessment Engine:** Question bank management, automated server-graded MCQ assessments, attempt limits, and submission immutability.
- **Automatic Achievement Certificates:** Auto-issuance of PDF certificates with embedded Unicode fonts upon passing verified assessments (threshold-based), featuring unique certificate numbers and cryptographic public verification tokens.
- **Competency Frameworks & Gap Analysis:** Measure verified skill proficiency against framework targets, calculate individual/organization readiness, and provide tailored learning resource recommendations.
- **Account Administration & Security Audit:** Activation/deactivation controls, audit logs, and safeguards preventing self-deactivation or disabling the last active HR admin.
- **Robust Security:** Stateless JWT authentication, Spring Security role-based access control (RBAC), and route guards.

---

## 🔐 Demo & Evaluation Login Credentials

The application is pre-configured with active accounts for testing all role-based dashboards:

| Portal / Role | Email / Username | Password | Access Scope |
| :--- | :--- | :--- | :--- |
| **HR Administrator** | `admin@skillsphere.com` | `admin123456789` | Full administrative dashboard, employee & student auditing, competency frameworks, course catalog, LMS sync, analytics. |
| **Employee Portal** | `employee@skillsphere.com` | `employee12345` | Employee self-service dashboard, career roadmap, certification registration & management, internal job postings. |
| **Student Portal** | `student@skillsphere.com` | `student12345` | Student learning dashboard, MCQ assessments, automated PDF achievement certificates, learning path tracking. |

> **Web Application URL**: [http://localhost:4200](http://localhost:4200)  
> **Backend REST API**: [http://localhost:8080](http://localhost:8080)

---

## 🐳 Docker Quick Start

To run the entire system (PostgreSQL 17, Spring Boot Backend, and Nginx Frontend) in Docker:

```bash
docker compose up -d
```

- **Frontend Application**: [http://localhost:4200](http://localhost:4200)
- **Backend API & Health**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **PostgreSQL Database**: Port `5433` (internal `5432`)

---

## Technology Stack

| Component | Technology |
|---|---|
| Frontend | Angular 20, TypeScript, SCSS |
| Backend | Java 25, Spring Boot 3 |
| Database | PostgreSQL (Production / Dev), H2 (Automated Test Isolation) |
| Security | Spring Security, JJWT (JSON Web Token) |
| Database Migrations | Flyway (V1 - V4) |
| Document Generation | OpenPDF (TrueType Unicode Font Rendering) |
| Build Tools | Maven Wrapper (`mvnw`), npm |

---

## Project Structure

```text
SkillSphere-Nexus/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/skillsphere/skill/
│   │   │   │   ├── controller/      # REST API Controllers (Student, Learning, Certs, etc.)
│   │   │   │   ├── entity/          # JPA Domain Entities
│   │   │   │   ├── repository/      # Spring Data Repositories
│   │   │   │   ├── security/        # JWT & Account Administration
│   │   │   │   └── service/         # Business Logic & PDF Generation
│   │   │   └── resources/
│   │   │       ├── db/migration/    # Flyway Migrations (V1 to V4)
│   │   │       └── application.properties
│   │   └── test/                    # Integration & Unit Tests
│   ├── mvnw / mvnw.cmd
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   └── app/                     # Angular Components & Services
│   ├── angular.json
│   ├── package.json
│   └── package-lock.json
├── .gitignore
├── SECURITY.md
├── start-project.ps1
└── README.md
```

---

## Roles & Access Control

| Role | Access Scope |
| --- | --- |
| `HR_ADMIN` | Full administrative control: employee & student provisioning, question bank & assessments, account status, organization analytics, certificate issuance/revocation. |
| `EMPLOYEE` | Employee-linked profile, skill mapping requests, assessment-taking workflow, gap reports, and personal certificates. |
| `STUDENT` | Dedicated student profile, skill catalog requests, MCQ assessment attempts, framework gap reports, learning recommendations, and personal certificates. |

> Only an HR administrator can create student accounts through `POST /api/auth/students`. The server strictly assigns the `STUDENT` role; client-supplied role values are ignored.

---

## Key Business Logic & Calculations

### Automatic Achievement Certificates
An authorized assessor creates an assessment and marks it `VERIFIED`. When a verified score meets or exceeds `CERTIFICATE_PASSING_PERCENTAGE` (default: 70%), a unique certificate and PDF are automatically generated and persisted.
- **Revocation:** HR admins can revoke certificates with an audited reason.
- **Verification:** Anyone can verify a certificate via a public token (`GET /api/certificates/verify/{token}`) without exposing private learner records.

### Skill Gap & Readiness Formula
Only verified proficiency contributes to readiness:
```text
Gap = max(0, required proficiency - current verified proficiency)
Readiness (%) = (sum(min(current verified proficiency, required proficiency)) / sum(required proficiency)) * 100
```
Missing and unverified skills contribute zero verified proficiency. Verified assessment scores are normalized to a 1–10 scale:
$$\text{level} = \text{clamp}\left(\lceil\text{score} \times 10 / \text{maxScore}\rceil, 1, 10\right)$$

---

## API Highlights

- **Student Administration & Self-Service**:
  - `POST /api/auth/students` — HR admin provisions student profile and account.
  - `GET /api/students/me`, `PUT /api/students/me` — Student personal profile management.
  - `GET /api/students/me/skills`, `POST /api/students/me/skills` — View and request skills.
- **Learning & Assessment Engine**:
  - `/api/admin/learning/**` — Assessment definitions, MCQ questions, and learning resources (HR Admin).
  - `GET /api/learning/assessments`, `POST /api/learning/assessments/{id}/attempts` — List eligible assessments and begin attempt.
  - `POST /api/learning/attempts/{id}/submit`, `GET /api/learning/attempts` — Submit graded attempts and view history.
- **Certificates**:
  - `GET /api/certificates` — Learner certificates (or all for HR Admin).
  - `GET /api/certificates/{id}/download` — Download official PDF certificate.
  - `PUT /api/certificates/{id}/revoke` — Revoke certificate (HR Admin).
  - `GET /api/certificates/verify/{token}` — Public minimal certificate verification.
- **Organization Analytics & Recommendations**:
  - `GET /api/admin/analytics/skill-gaps?frameworkId={id}` — Framework gap analytics.
  - `GET /api/learning/resources/recommendations?frameworkId={id}` — Targeted learning resource recommendations.

---

## Prerequisites

1. **Java JDK 25** (or compatible JDK configured in Maven).
2. **Node.js & npm** (supported by Angular 20).
3. **PostgreSQL** (running on port 5432).
4. **Git**.

---

## Quick Start / Local Setup

### 1. Configure Environment Variables (PowerShell)

In your terminal session, set your local credentials:

```powershell
$dbPassword = Read-Host "PostgreSQL password" -AsSecureString
$env:DB_PASSWORD = (New-Object System.Net.NetworkCredential('', $dbPassword)).Password

$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtBytes = New-Object byte[] 48
$rng.GetBytes($jwtBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
$rng.Dispose()

$env:INITIAL_ADMIN_USERNAME = Read-Host "Initial HR admin email"
$adminPassword = Read-Host "Initial HR admin password (12+ characters)" -AsSecureString
$env:INITIAL_ADMIN_PASSWORD = (New-Object System.Net.NetworkCredential('', $adminPassword)).Password
```

### 2. Optional Environment Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/skillsphere_db` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `postgres` | PostgreSQL username |
| `CERTIFICATE_ISSUER` | `SkillSphere Nexus` | Certificate issuer display name |
| `CERTIFICATE_PASSING_PERCENTAGE` | `70` | Pass mark for automatic certificate issuance (1–100) |
| `CERTIFICATE_FONT_PATH` | Platform auto-discovery | Path to Unicode TTF font for PDF generation |

### 3. Launch the Applications

You can start both backend and frontend using the included PowerShell script:
```powershell
.\start-project.ps1
```

Or start them manually in separate terminals:
- **Backend**:
  ```powershell
  cd backend
  .\mvnw.cmd spring-boot:run
  ```
  Backend runs at: `http://localhost:8080`
- **Frontend**:
  ```powershell
  cd frontend
  npm ci
  npm start
  ```
  Frontend runs at: `http://localhost:4200`

---

## Testing & Quality Assurance

Run backend unit and integration tests (isolated H2 in-memory DB):
```powershell
cd backend
.\mvnw.cmd test
```

Build the frontend bundle:
```powershell
cd frontend
npm run build
```

---

## Troubleshooting

- **PostgreSQL Connection Failure:** Verify PostgreSQL service is active and `skillsphere_db` exists.
- **Port Conflicts:** Ensure ports `8080` (Spring Boot) and `4200` (Angular) are available.
- **Login Issues:** Confirm `JWT_SECRET` and `INITIAL_ADMIN_PASSWORD` were initialized. Refer to `SECURITY.md` for details.

---

## GitHub Repositories

- [Infosistraining](https://github.com/viggu-debuggu/Infosistraining)
- [SkillSphere Nexus](https://github.com/ranga0011/skillsphere-nexus)
