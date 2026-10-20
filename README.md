# SkillSphere Nexus

**Employee Skill Management System using Angular, Spring Boot, and PostgreSQL**

SkillSphere Nexus is a web application designed to manage employee profiles, skills, proficiency levels, assessments, certifications, and competency frameworks in one place.

## Features

- **Employee Management:** Manage employee profiles and employee information.
- **Skill Catalog:** Maintain a categorized catalog of technical and professional skills.
- **Employee Skill Mapping:** Associate skills with employees and track proficiency.
- **Skill Assessments:** Record skill assessments and verification status.
- **Competency Frameworks:** Define skill requirements and identify competency gaps.
- **Certification Management:** Track employee certifications and expiry dates.
- **Authentication and Authorization:** JWT-based authentication and role-based access control, as implemented in the application.
- **REST API:** Spring Boot backend for application data and business logic.
- **Responsive Web Interface:** Angular frontend.

## Technology Stack

| Component | Technology |
|---|---|
| Frontend | Angular 20, TypeScript |
| Backend | Java, Spring Boot |
| Database | PostgreSQL |
| Authentication | JWT and Spring Security |
| Database Migrations | Flyway |
| Build Tool | Maven Wrapper |
| Package Manager | npm |

## Project Structure

```text
SkillSphere-Nexus/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
├── frontend/
│   ├── src/
│   ├── angular.json
│   ├── package.json
│   └── package-lock.json
├── .gitignore
├── SECURITY.md
├── start-project.ps1
└── README.md
```

## Prerequisites

Install the following software before running the project:

1. **Java JDK:** Install a JDK compatible with the backend's Maven and Spring Boot configuration.
2. **Node.js and npm:** Install a Node.js version supported by the Angular version in `frontend/package.json`.
3. **PostgreSQL:** Install and start PostgreSQL.
4. **Git:** Required to clone the repository.
5. **Visual Studio Code:** Recommended for development.

Verify your installations in PowerShell:

```powershell
java -version
node --version
npm --version
```

To check PostgreSQL, use `psql --version` if the PostgreSQL command-line tools are on your PATH.

## 1. Clone the Repository

Choose either repository.

### Repository 1

```powershell
git clone https://github.com/viggu-debuggu/Infosistraining.git
cd Infosistraining
```

### Repository 2

```powershell
git clone https://github.com/ranga0011/skillsphere-nexus.git
cd skillsphere-nexus
```

If you already have the project locally, open the existing `SkillSphere-Nexus` folder instead.

## 2. Configure PostgreSQL

Make sure the PostgreSQL service is running.

Create a database named `skillsphere_db` if your application configuration uses that name:

```sql
CREATE DATABASE skillsphere_db;
```

You can execute this SQL using pgAdmin or the PostgreSQL command line.

Configure the database connection using environment variables. The application may support these variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Check `backend/src/main/resources/application.properties` to confirm the expected values and defaults.

Do not commit real database passwords or other credentials to GitHub.

## 3. Configure Backend Environment Variables

The backend's security configuration may require the following environment variables:

```text
DB_PASSWORD
JWT_SECRET
INITIAL_ADMIN_USERNAME
INITIAL_ADMIN_PASSWORD
```

Set the values in your local PowerShell session before starting the backend. Replace the example placeholders with your own secure local values.

```powershell
$env:DB_PASSWORD = "YOUR_DATABASE_PASSWORD"
$env:JWT_SECRET = "YOUR_LONG_RANDOM_SECRET_AT_LEAST_32_UTF8_BYTES"
$env:INITIAL_ADMIN_USERNAME = "YOUR_ADMIN_USERNAME"
$env:INITIAL_ADMIN_PASSWORD = "YOUR_STRONG_ADMIN_PASSWORD"
```

Use a strong, unique JWT secret and administrator password. Do not publish these values or paste real secrets into source files.

Refer to `SECURITY.md` and the backend configuration for the exact authentication requirements and local administrator initialization behavior.

## 4. Run the Spring Boot Backend

Open PowerShell in the project root:

```powershell
cd backend
```

On Windows, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend is expected to start at:

```text
http://localhost:8080
```

Keep this terminal open while using the application.

If startup fails, check the database service, connection settings, environment variables, and the error messages in the terminal.

## 5. Run the Angular Frontend

Open a **second terminal** in the project root:

```powershell
cd frontend
```

Install dependencies:

```powershell
npm ci
```

Start the Angular development server:

```powershell
npm start
```

If the `start` script is not defined in `frontend/package.json`, use:

```powershell
npx ng serve
```

Open the local frontend URL shown in the terminal. The default Angular development URL is:

```text
http://localhost:4200
```

Keep the frontend terminal open while using the application.

## 6. Run Both Applications

You need two running processes during development:

| Application | Command | Expected URL |
|---|---|---|
| Spring Boot backend | `.\mvnw.cmd spring-boot:run` | `http://localhost:8080` |
| Angular frontend | `npm start` or `npx ng serve` | `http://localhost:4200` |

Start PostgreSQL first, then start the backend, followed by the frontend.

## 7. Optional: Start Using the PowerShell Script

The project root contains `start-project.ps1`.

Review the script before running it to understand how it starts the applications and handles configuration.

From the project root:

```powershell
.\start-project.ps1
```

If PowerShell blocks script execution, review your execution policy and the script's contents before changing any security settings.

## 8. Run Backend Tests

From the project root:

```powershell
cd backend
.\mvnw.cmd test
```

This runs the backend tests configured in the Maven project.

## 9. Build the Frontend

From the project root:

```powershell
cd frontend
npm ci
npm run build
```

Check `frontend/package.json` if the build script has a different name.

## 10. Troubleshooting

### PostgreSQL connection failure

- Confirm that the PostgreSQL service is running.
- Verify the database name, username, password, and port.
- Confirm that the configured database exists.
- Check the backend logs for the specific connection error.

### Backend fails to start

- Run `java -version`.
- Verify the Java version required by `backend/pom.xml`.
- Check required environment variables.
- Review the terminal output for Maven or Spring Boot errors.

### Frontend dependencies fail to install

- Confirm Node.js and npm are installed.
- Run `npm ci` from the `frontend` directory.
- Check the Node.js version supported by the project's Angular dependencies.

### Port 4200 or 8080 is already in use

Stop the process using the port or configure the application to use another available port.

### Login fails

- Confirm the backend is running.
- Check the authentication endpoint and browser network errors.
- Verify the required environment variables and local administrator initialization configuration.
- Refer to `SECURITY.md` for the authentication setup.

## Security Notes

- Never commit `.env` files, database passwords, JWT secrets, access tokens, or private keys.
- Use strong credentials for administrator accounts.
- Do not use development credentials in production.
- Verify authorization rules before exposing the application to external users.
- Review `SECURITY.md` for the project's intended security configuration.

## GitHub Repositories

- [Infosistraining](https://github.com/viggu-debuggu/Infosistraining)
- [SkillSphere Nexus](https://github.com/ranga0011/skillsphere-nexus)

## License

No license has been specified in this README. Add a license file if you intend to distribute the project under a particular open-source license.
