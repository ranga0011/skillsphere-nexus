# SkillSphere Nexus Authentication

## Local Configuration

The backend reads database and signing secrets from its process environment. No development or production credential is stored in source control. The initial-admin bootstrap runs only with Spring's `local` profile, which the project launcher enables for the backend. In PowerShell, enter the values in the same session used to start the project:

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

`DB_URL` and `DB_USERNAME` are optional and default to the existing local PostgreSQL URL and `postgres` user. `JWT_SECRET` must be at least 32 UTF-8 bytes. The first local startup creates an `HR_ADMIN` when no HR admin exists. If the configured username already belongs to an HR admin, the local bootstrap applies the privately supplied password to that same account by updating only its BCrypt hash. It refuses to repurpose an employee account or create a second HR admin. Startup logs contain no email or credentials. The launcher validates required values without printing them and strips these variables from the frontend process. Remove `INITIAL_ADMIN_USERNAME` and `INITIAL_ADMIN_PASSWORD` after successful startup so the bootstrap is not re-applied on later launches.

## Accounts and Roles

There is no public registration endpoint. An HR administrator can provision an employee account using `POST /api/auth/users`:

```json
{
  "username": "employee@example.com",
  "password": "a-unique-password-at-least-12-chars",
  "employeeId": 42
}
```

Send the request with the admin bearer token. The backend assigns `EMPLOYEE`; role selection is not accepted from the client. `employeeId` must identify an existing employee and can be linked to only one account. Passwords are BCrypt-hashed and are never included in API responses.

Students use a separate `students` profile rather than an employee record. HR admins provision student accounts using `POST /api/auth/students` with a student name, email, program, username, and initial password. The server assigns `STUDENT`; a request-supplied role is ignored and cannot elevate privileges. Students may update their own name, email, and program at `/api/students/me`, request skills for their own profile, and read only their own assessments, gap reports, and achievement certificates. Student skill requests always start as `PENDING_VERIFICATION`.

| Role | Backend-enforced access |
| --- | --- |
| `HR_ADMIN` | Manage employee/skill/framework data, assessments and questions, account status, organization analytics, student accounts, and certificates |
| `EMPLOYEE` | Read own employee-linked profile, skills, competency-gap report, take online assessments, and access own certificates |
| `STUDENT` | Read/update own separate profile, request skills, take assessments, view own results/gap report/recommendations, and access own certificates |

No user-facing role selector grants roles. Administrative routes use Spring Security role checks independently of Angular guards.

## API Flow and Permissions

- `POST /api/auth/login` accepts username/password and returns a 15-minute signed bearer token.
- Send the token as `Authorization: Bearer <accessToken>` for API requests. Tokens are kept in Angular memory and are cleared on logout, expiry, or an unauthorized response; reloading the page requires signing in again.
- HR admins can manage employees, skills, mappings, assessments, frameworks, requirements, and certifications.
- Employees can read only their linked employee profile, mappings, assessments, certifications, gap reports, and generated certificates.
- Students can read only their own student profile, skill requests, assessments, gap reports, and generated certificates.
- Employees and students can list active online assessments, start attempts, submit answers, and read their own attempt results/history. Answer keys remain server-side.
- HR admins alone can manage `/api/admin/learning/**`, `/api/admin/users/**`, and `/api/admin/analytics/skill-gaps`.
- Students alone can read `/api/learning/resources/recommendations`; recommendations use their own framework skill gaps and configured resources.
- HR admins and signed-in users can read shared skill/framework catalogs. HR-only mutations and HR-wide lists are enforced by the backend.
- `GET /api/certificates/verify/{token}` is public and returns only the certificate number, skill, issuer, issue date, and current status. It does not expose learner names, email addresses, assessment answers, or internal database IDs.
- `GET /api/certificates/{id}/download` requires the certificate owner or an HR admin. Revoked certificates cannot be downloaded.
- `/actuator/health` and the login endpoint are public. The verification endpoint is the only other public API.

For the existing manual assessment API, clients cannot set verification status; HR admins verify those records and the backend controls their status. Online learner assessment answers are graded on the server from the stored answer key. A verified assessment issues one certificate only when its score meets `CERTIFICATE_PASSING_PERCENTAGE` (default 70%). Database uniqueness on the assessment reference prevents duplicate certificate records. Generated PDF content is stored with certificate metadata. Revocation preserves status, timestamp, actor, and reason.

For competency reports, unverified mappings contribute no verified proficiency. Each skill gap is `max(0, required proficiency - current verified proficiency)`. Readiness is the sum of verified proficiency capped at each requirement divided by the sum of required proficiency, expressed as a percentage. If linked verified assessment evidence is rejected or deleted, the mapping is recalculated from the latest remaining verified assessment or returned to pending. The figure describes requirement coverage only.

Assessment attempts enforce ownership, attempt limits, answer coverage, server-side scoring, and immutable submitted state. A passing result is linked to the existing verified-assessment and certificate services; failed results do not issue certificates. Issuance still requires both assessment and global certificate thresholds.

HR account-status changes are audited. Self-deactivation and deactivation of the last active HR administrator are rejected. Login checks account status, and JWT validation checks it again against the database so deactivation also invalidates previously issued tokens.

Organization analytics are HR-admin-only and framework scoped. Their denominator includes employee and student profiles in the selected framework; only verified proficiency contributes to results, and the response is aggregate rather than learner-level.

## Database Migration

Flyway V1 adds the original `app_users` table. Additive V2 adds the separate `students` table, the student account link and expanded role constraint, and student ownership to existing mappings/assessments when those tables already exist. Additive V3 adds account-status audit, assessment/question/attempt, and learning-resource tables and indexes. Additive V4 adds provenance links for newly verified assessment evidence, allowing it to be recalculated if rejected/deleted. Historical mapping provenance is not inferred and existing statuses are preserved. No existing records are dropped or rewritten. The issued-certificate schema and JPA relationships to pre-existing business tables are created/updated through the application's `ddl-auto=update` behavior, not Flyway alone; deployments must preserve that behavior or provision equivalent schema explicitly.

Automated tests override the datasource with isolated in-memory H2 databases and run Flyway migrations there; they do not connect to the development PostgreSQL database. Fresh PostgreSQL migration, V1/V2 upgrade preservation, concurrent certificate issuance, and transaction behavior are **not verified**. Use a separately created disposable database for those checks, never the development database. PostgreSQL credentials were unavailable in this session; no database was created, dropped, or changed.

The development database has not been inspected from this setup session because its connection credential is intentionally unavailable here. The bootstrap reports whether it creates or skips the chosen username at local startup; it will not reset an existing user or create a second HR admin.

## Manual Checks

1. Set the environment values above and start the app. Open `http://localhost:4200`; the login screen should appear.
2. Sign in using the initial HR account. Confirm employee, skill, assessment, framework, mapping, and manual certification screens still load.
3. Create employee and student accounts through their respective HR-only provisioning endpoints. Confirm that a student has a separate profile and receives only the `STUDENT` role.
4. `GET http://localhost:8080/api/employees` without a token should return `401`; the same request as an employee should return `403`; it should return `200` as HR admin.
5. As an employee/student, requesting another user's profile or certificate should return `403`; own profile and record endpoints should succeed.
6. Verify an assessment below the configured pass percentage does not issue a certificate. Verify a passing assessment issues one valid PDF, repeated verification does not duplicate it, and revocation changes public verification status to `REVOKED`.
7. A browser request from `http://localhost:4200` should pass CORS preflight with the `Authorization` header. Expired tokens are rejected with `401`, and Angular returns to the login page after an unauthorized response.