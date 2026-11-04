$ErrorActionPreference = "Stop"

$root = $PSScriptRoot
$frontend = Join-Path $root "frontend"
$backend = Join-Path $root "backend"

if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    $env:DB_PASSWORD = "local"
}

if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET) -or [System.Text.Encoding]::UTF8.GetByteCount($env:JWT_SECRET) -lt 32) {
    $env:JWT_SECRET = "local-secret-key-skillsphere-nexus-enterprise-platform-2026-v2"
}

if ([string]::IsNullOrWhiteSpace($env:INITIAL_ADMIN_USERNAME)) {
    $env:INITIAL_ADMIN_USERNAME = "admin@skillsphere.com"
}
if ([string]::IsNullOrWhiteSpace($env:INITIAL_ADMIN_PASSWORD)) {
    $env:INITIAL_ADMIN_PASSWORD = "admin123456789"
}

if (-not (Test-Path (Join-Path $frontend "package.json"))) {
    throw "Angular project not found in the frontend folder."
}

if (-not (Test-Path (Join-Path $backend "pom.xml"))) {
    throw "Spring Boot project not found in the backend folder."
}

Write-Host "Starting SkillSphere Nexus..." -ForegroundColor Cyan

$backendCommand = @"
`$env:DB_PASSWORD = '$($env:DB_PASSWORD)'
`$env:JWT_SECRET = '$($env:JWT_SECRET)'
`$env:INITIAL_ADMIN_USERNAME = '$($env:INITIAL_ADMIN_USERNAME)'
`$env:INITIAL_ADMIN_PASSWORD = '$($env:INITIAL_ADMIN_PASSWORD)'
Set-Location '$backend'
& '.\mvnw.cmd' "-Dspring-boot.run.profiles=local" spring-boot:run
"@
$backendEncodedCommand = [Convert]::ToBase64String([System.Text.Encoding]::Unicode.GetBytes($backendCommand))

Start-Process powershell.exe -ArgumentList @(
    "-NoExit",
    "-EncodedCommand",
    $backendEncodedCommand
)

Start-Process powershell.exe -ArgumentList @(
    "-NoExit",
    "-Command",
    "Set-Location '$frontend'; npm start"
)

Write-Host "Startup commands launched." -ForegroundColor Green
Write-Host "Frontend: http://localhost:4200"
Write-Host "Backend API: http://localhost:8080/api/employees"
