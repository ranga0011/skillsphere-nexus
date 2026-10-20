
$ErrorActionPreference = "Stop"

$root = $PSScriptRoot
$frontend = Join-Path $root "frontend"
$backend = Join-Path $root "backend"

if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    throw "Set DB_PASSWORD in this PowerShell session before starting SkillSphere Nexus."
}

if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET) -or [System.Text.Encoding]::UTF8.GetByteCount($env:JWT_SECRET) -lt 32) {
    throw "Set JWT_SECRET to at least 32 UTF-8 bytes before starting SkillSphere Nexus."
}

if ([string]::IsNullOrWhiteSpace($env:INITIAL_ADMIN_USERNAME) -ne [string]::IsNullOrWhiteSpace($env:INITIAL_ADMIN_PASSWORD)) {
    throw "Set both INITIAL_ADMIN_USERNAME and INITIAL_ADMIN_PASSWORD, or neither."
}

if (-not (Test-Path (Join-Path $frontend "package.json"))) {
    throw "Angular project not found in the frontend folder."
}

if (-not (Test-Path (Join-Path $backend "pom.xml"))) {
    throw "Spring Boot project not found in the backend folder."
}

Write-Host "Starting SkillSphere Nexus..." -ForegroundColor Cyan

$backendCommand = @"
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
    "Remove-Item Env:DB_PASSWORD,Env:JWT_SECRET,Env:INITIAL_ADMIN_USERNAME,Env:INITIAL_ADMIN_PASSWORD -ErrorAction SilentlyContinue; Set-Location '$frontend'; npm start"
)

Write-Host "Startup commands launched." -ForegroundColor Green
Write-Host "Frontend: http://localhost:4200"
Write-Host "Backend API: http://localhost:8080/api/employees"
