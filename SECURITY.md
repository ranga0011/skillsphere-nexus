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

There is no public registration endpoint. An HR administrator creates an employee account and links it to the existing employee row using `POST /api/auth/users`:

```json
{
  "username": "employee@example.com",
  "password": "a-unique-password-at-least-12-chars",
  "employeeId": 42
}
```

Send the request with the admin bearer token. The backend assigns `EMPLOYEE`; role selection is not accepted from the client. `employeeId` must identify an existing employee and can be linked to only one account. Passwords are BCrypt-hashed and are never included in API responses.

## API Flow and Permissions

- `POST /api/auth/login` accepts username/password and returns a 15-minute signed bearer token.
- Send the token as `Authorization: Bearer <accessToken>` for API requests. Tokens are kept in Angular memory and are cleared on logout, expiry, or an unauthorized response; reloading the page requires signing in again.
- HR admins can manage employees, skills, mappings, assessments, frameworks, requirements, and certifications.
- Employees can read their own employee profile, mappings, assessments, certifications, and competency gap reports. They cannot list other employees or access other employees' restricted records.
- Both roles can read the shared skill and framework catalogs. HR-only operations are enforced by the backend, independently of Angular visibility and guards.
- `/actuator/health` and the login endpoint are public. Other endpoints require authentication.

## Database Migration

Flyway migration `V1__create_app_users.sql` adds only the `app_users` table and employee-link index. Existing business tables and records are not dropped or rewritten. On an existing non-empty schema without Flyway history, Flyway baselines at version `0` and applies the additive migration. Hibernate retains the project's existing `ddl-auto=update` behavior for its entities.

Automated tests override the datasource with a unique in-memory H2 database and run the additive Flyway migration there; they do not connect to the development PostgreSQL database.

The development database has not been inspected from this setup session because its connection credential is intentionally unavailable here. The bootstrap reports whether it creates or skips the chosen username at local startup; it will not reset an existing user or create a second HR admin.

## Manual Checks

1. Set the environment values above and start the app. Open `http://localhost:4200`; the login screen should appear.
2. Sign in using the initial HR account. Confirm the existing employee, skills, assessment, framework, mapping, and certification screens still load and management controls are available.
3. Create an employee account with the admin-only endpoint, then sign out and sign in as that employee. Confirm the workspace shows only their records and has no HR mutation controls.
4. `GET http://localhost:8080/api/employees` without a token should return `401`; the same request as an employee should return `403`; it should return `200` as HR admin.
5. As an employee, `GET /api/employees/{anotherEmployeeId}` should return `403`; `GET /api/employees/me` should return the linked employee record.
6. A browser request from `http://localhost:4200` should pass CORS preflight with the `Authorization` header. Expired tokens are rejected with `401`, and Angular returns to the login page after an unauthorized response.