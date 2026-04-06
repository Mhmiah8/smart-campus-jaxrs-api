#!/usr/bin/env pwsh
<#
.SYNOPSIS
Smart Campus API Setup and Build Script
Checks for Java and Maven, then builds the project
.DESCRIPTION
This script:
1. Checks if Java 11+ is installed
2. Checks if Maven 3.6+ is installed
3. Downloads them if needed
4. Builds the Maven project
5. Runs the embedded server
#>

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Smart Campus API - Setup & Build Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$JAVA_MIN_VERSION = 11
$MAVEN_MIN_VERSION = "3.6.0"
$PROJECT_DIR = Split-Path -Parent $PSCommandPath
$JAVA_DOWNLOAD = "https://adoptium.net/download/"
$MAVEN_DOWNLOAD = "https://maven.apache.org/download.cgi"

# Color functions
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error-Custom { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Yellow }
function Write-Step { Write-Host "`n>>> $args" -ForegroundColor Cyan }

# Check Java Installation
Write-Step "Checking Java Installation..."
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Success "✅ Java is installed:"
        Write-Success "   $javaVersion"
    } else {
        throw "Java not found"
    }
} catch {
    Write-Error-Custom "❌ Java is NOT installed or not in PATH"
    Write-Info "Please install Java 11 or later:"
    Write-Info "   1. Download from: $JAVA_DOWNLOAD"
    Write-Info "   2. Run the installer"
    Write-Info "   3. Restart PowerShell"
    Write-Info "   4. Run this script again"
    exit 1
}

# Check Maven Installation
Write-Step "Checking Maven Installation..."
try {
    $mavenVersion = mvn -version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Success "✅ Maven is installed:"
        Write-Success "   $($mavenVersion[0])"
    } else {
        throw "Maven not found"
    }
} catch {
    Write-Error-Custom "❌ Maven is NOT installed or not in PATH"
    Write-Info "Please install Maven 3.6.0 or later:"
    Write-Info "   1. Download from: $MAVEN_DOWNLOAD"
    Write-Info "   2. Extract to a folder (e.g., C:\apache-maven-3.9.0)"
    Write-Info "   3. Add to System Environment Variables:"
    Write-Info "      - MAVEN_HOME = C:\apache-maven-3.9.0"
    Write-Info "      - PATH = add %MAVEN_HOME%\bin"
    Write-Info "   4. Restart PowerShell"
    Write-Info "   5. Run this script again"
    exit 1
}

# Change to project directory
Write-Step "Navigating to project directory..."
Set-Location $PROJECT_DIR
Write-Success "✅ Project directory: $(Get-Location)"

# Clean and build
Write-Step "Building Smart Campus API with Maven..."
Write-Info "This may take 1-2 minutes on first build (downloading dependencies)..."
Write-Host ""

mvn clean package -q

if ($LASTEXITCODE -eq 0) {
    Write-Success "`n✅ BUILD SUCCESSFUL!"
    Write-Success ""
    Write-Success "Generated WAR file: target/smart-campus-api.war"
    Write-Success ""
    
    # Check if embedded server class exists
    if (Test-Path "src\main\java\com\campus\EmbeddedServer.java") {
        Write-Host ""
        Write-Step "Starting Embedded Server..."
        Write-Info "The API will be available at: http://localhost:8080/api/v1/"
        Write-Info "Press Ctrl+C to stop the server"
        Write-Host ""
        
        java -cp "target/classes;target/dependency/*" com.campus.EmbeddedServer
    } else {
        Write-Host ""
        Write-Info "To run without Tomcat, use the embedded server:"
        Write-Info "  java -cp target/classes:target/dependency/* com.campus.EmbeddedServer"
        Write-Info ""
        Write-Info "Or deploy the WAR file to Tomcat:"
        Write-Info "  1. Copy target/smart-campus-api.war to %CATALINA_HOME%/webapps/"
        Write-Info "  2. Start Tomcat"
        Write-Info "  3. Access at http://localhost:8080/smart-campus-api/api/v1/"
    }
} else {
    Write-Error-Custom "❌ BUILD FAILED"
    Write-Error-Custom "Check the output above for error messages"
    exit 1
}
