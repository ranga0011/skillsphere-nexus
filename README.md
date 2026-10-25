# SkillSphere Nexus

SkillSphere Nexus is an employee and student skill-management application built with Spring Boot, PostgreSQL, and Angular. Existing employee, skill-catalog, assessment, framework, and external-certification workflows remain available. Students use a distinct profile and role.

## Requirements

- Java 25
- Node.js and npm
- PostgreSQL with a `skillsphere_db` database (or a configured `DB_URL`)

## Local setup

In PowerShell, set secrets in the same session that launches the application. Do not save these values in source control:

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

.\start-project.ps1
```

The launcher starts the backend at `http://localhost:8080` and Angular at `http://localhost:4200`. The initial HR admin bootstrap is local-profile-only. Remove `INITIAL_ADMIN_USERNAME` and `INITIAL_ADMIN_PASSWORD` after bootstrap so startup does not re-apply the configured password.

Optional backend settings:

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/skillsphere_db` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `postgres` | PostgreSQL username |
| `CERTIFICATE_ISSUER` | `SkillSphere Nexus` | Neutral certificate issuer identity |
| `CERTIFICATE_PASSING_PERCENTAGE` | `70` | Minimum verified assessment percentage for automatic certificate issuance (1–100) |
| `CERTIFICATE_FONT_PATH` | platform font discovery | Path to a Unicode TrueType font for PDF rendering |

The PDF generator looks for Arial on Windows and DejaVu Sans/Liberation Sans on common Linux installations. Configure `CERTIFICATE_FONT_PATH` to a properly licensed Unicode TTF file in deployment environments where those fonts are unavailable.

## Roles

| Role | Access |
| --- | --- |
| `HR_ADMIN` | Existing employee, skill, assessment, framework, and certification administration; student provisioning; question bank, account status, organization analytics, and certificate administration |
| `EMPLOYEE` | Existing employee-linked profile, skill data, assessment-taking workflow, gap report, and own certificates |
| `STUDENT` | Separate student profile, skill requests, assessment-taking workflow, gap report, learning recommendations, and own certificates |

Only an HR administrator can create student accounts through `POST /api/auth/students`. The server assigns `STUDENT`; role values sent by clients are not used. No public student registration or student-supplied assessment scores are enabled.

## Automatic achievement certificates

An authorized HR assessor creates an assessment, then explicitly marks it `VERIFIED`. A verified result at or above `CERTIFICATE_PASSING_PERCENTAGE` issues one unique certificate for that assessment. A failed or unverified assessment and a skill request alone do not issue certificates. Re-verifying an assessment does not duplicate its certificate.

The generated certificate and PDF bytes are persisted with the certificate record. Learners can download their own PDF; HR admins can revoke a certificate with an audit reason. Public verification uses an unguessable token and returns only the certificate number, skill, issuer, issue date, and status.

## Skill-gap calculation

Only verified proficiency contributes to readiness. Verified assessment evidence is linked to the mapping; if its assessment is rejected or deleted, the mapping falls back to the latest remaining verified assessment, or returns to pending if none remains. For each framework skill:

```text
Gap = max(0, required proficiency - current verified proficiency)
Readiness = sum(min(current verified proficiency, required proficiency))
            / sum(required proficiency) * 100
```

Missing and unverified skills contribute zero verified proficiency. A verified assessment also updates the linked skill mapping to a normalized 1–10 level using `ceil(score * 10 / max score)`, clamped to 1–10. Readiness measures requirement coverage; it is not a hiring probability.

## API highlights

- `POST /api/auth/students` — HR admin creates a separate student profile and account.
- `/api/admin/learning/**` — HR-admin-only assessment definitions, MCQ questions, and learning resources.
- `GET /api/learning/assessments`, `POST /api/learning/assessments/{id}/attempts` — learners list eligible active assessments and start attempts.
- `POST /api/learning/attempts/{id}/submit`, `GET /api/learning/attempts` — submit server-graded answers and view only the caller's results/history. Correct options are not returned before submission.
- `GET /api/admin/users` and `GET /api/admin/users/audit` — paginated HR-admin user and status-audit lists; `PUT /api/admin/users/{id}/status` changes account activation.
- `GET /api/admin/analytics/skill-gaps?frameworkId={id}` — paginated/filterable HR-admin framework analytics.
- `GET /api/learning/resources/recommendations?frameworkId={id}` — student recommendations from configured resources and their actual framework gaps.
- `GET /api/students/me`, `PUT /api/students/me` — student-owned profile access.
- `GET /api/students/me/skills`, `POST /api/students/me/skills` — view/request a skill; new requests are pending verification.
- `GET /api/students/me/assessments` — own assessment history.
- `GET /api/frameworks/{frameworkId}/gap-report/me` — role-scoped employee or student gap report.
- `GET /api/certificates` — own certificates for employees/students; all certificates for HR admins.
- `GET /api/certificates/{id}/download` — authenticated owner or HR-admin PDF download.
- `PUT /api/certificates/{id}/revoke` — HR-admin revocation with reason.
- `GET /api/certificates/verify/{token}` — public, minimal certificate verification.

All private records are protected by backend authorization; Angular guards are supplementary only.

Assessment attempts are graded using stored answer keys, limited by the configured attempt limit, and immutable after submission. A passing attempt is recorded through the existing verified-assessment and certificate services. A certificate is returned only when both the assessment threshold and the global certificate threshold are met; duplicate issuance is constrained by the existing certificate uniqueness rule. Deactivated users cannot log in, and an active-account check rejects their already-issued JWTs. HR status changes are audited, self-deactivation is blocked, and the final active HR administrator cannot be disabled.

Organization analytics use employee and student profiles in the selected framework's reporting denominator. Only verified skill proficiency counts; resource recommendations prioritize configured resources for the learner's positive skill gaps. No external course catalog or URLs are fabricated.

## Database and tests

Flyway V2 adds the separate `students` table, links student accounts, expands the role constraint, and adds student ownership columns to existing mapping/assessment tables when those tables exist. Additive Flyway V3 adds account-status audit, assessment/question/attempt, and learning-resource tables. Additive Flyway V4 tracks which verified assessment backs an employee/student skill mapping, so newly verified evidence can be recalculated if rejected or deleted. Historical mapping provenance is not inferred, and existing statuses are preserved. The application retains `spring.jpa.hibernate.ddl-auto=update`; the issued-certificate schema and entity relationships to pre-existing business tables rely on Hibernate schema updates, not Flyway alone.

Backend tests run Flyway against isolated H2 databases. The PostgreSQL development database was not accessible during this implementation, so PostgreSQL migration, upgrade, concurrency, and transaction behavior are **not verified**. Do not use the development database for migration testing. To smoke-test a fresh disposable PostgreSQL database, first create a uniquely named test database (for example, `skillsphere_nexus_pg_verify_20261021`) using your local PostgreSQL administration tools, then point only the test process to it:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/skillsphere_nexus_pg_verify_20261021"
$env:DB_USERNAME = "your-local-postgres-user"
$dbPassword = Read-Host "Disposable test database password" -AsSecureString
$env:DB_PASSWORD = (New-Object System.Net.NetworkCredential('', $dbPassword)).Password
$env:JWT_SECRET = "replace-with-a-local-random-secret-of-at-least-32-bytes"
Set-Location .\backend
.\mvnw.cmd spring-boot:run
```

Confirm Flyway V1–V4 and Hibernate schema update complete, then stop the test process. For upgrade-preservation testing, restore a sanitized earlier-version database dump into a *different* disposable database and use that database URL; never point the test at `skillsphere_db`. This workspace did not run the PostgreSQL checks.

Run backend tests and the Angular production build:

```powershell
Set-Location .\backend
.\mvnw.cmd test

Set-Location ..\frontend
npm run build
```

For separate manual startup after configuring the environment above, run `.\mvnw.cmd spring-boot:run` from `backend` and `npm start` from `frontend`; the project launcher `.\start-project.ps1` starts both processes.

Automated backend tests use isolated in-memory H2 databases; they do not require or inspect the configured development PostgreSQL database.
