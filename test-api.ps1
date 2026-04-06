#!/usr/bin/env pwsh
<#
.SYNOPSIS
Smart Campus API Test Script
Tests all API endpoints to verify the server is working correctly
.DESCRIPTION
This script uses Invoke-RestMethod to test:
- Discovery endpoint
- Room CRUD operations
- Sensor CRUD operations
- Sensor readings
- Error handling
#>

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Smart Campus API - Automated Test Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$BASE_URL = "http://localhost:8080"
$API_URL = "$BASE_URL/api/v1"
$ContentType = "application/json"

# Color functions
function Write-Success { Write-Host "✅ $args" -ForegroundColor Green }
function Write-Fail { Write-Host "❌ $args" -ForegroundColor Red }
function Write-Step { Write-Host "`n>>> $args" -ForegroundColor Cyan }
function Write-Response { Write-Host "   Response: $args" -ForegroundColor Gray }

# Test counter
$testsPassed = 0
$testsFailed = 0

# Helper function to test API endpoints
function Test-API {
    param(
        [string]$TestName,
        [string]$Method,
        [string]$Uri,
        [hashtable]$Body,
        [int]$ExpectedStatusCode = 200,
        [scriptblock]$Validation
    )
    
    Write-Host ""
    Write-Host "  TEST: $TestName" -ForegroundColor Yellow
    Write-Host "  $Method $Uri" -ForegroundColor Gray
    
    try {
        $params = @{
            Uri     = $Uri
            Method  = $Method
            ContentType = $ContentType
            ErrorAction = "Stop"
        }
        
        if ($Body -and ($Method -eq "POST" -or $Method -eq "PUT")) {
            $params['Body'] = $Body | ConvertTo-Json
        }
        
        $response = Invoke-RestMethod @params
        
        # Check if we got the expected structure
        if ($Validation) {
            & $Validation $response
        }
        
        Write-Success "PASSED"
        $script:testsPassed++
        return $response
        
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        $errorMsg = $_.Exception.Response.StatusDescription
        
        if ($statusCode -eq $ExpectedStatusCode) {
            Write-Success "PASSED (Expected error: $statusCode)"
            $script:testsPassed++
        } else {
            Write-Fail "FAILED - Expected status $ExpectedStatusCode, got $statusCode"
            Write-Response "$errorMsg"
            $script:testsFailed++
        }
        
        return $null
    }
}

# Wait for server to be ready
Write-Step "Waiting for server to be ready..."
$maxRetries = 10
$retry = 0
$serverReady = $false

while ($retry -lt $maxRetries -and -not $serverReady) {
    try {
        $response = Invoke-RestMethod -Uri $API_URL -Method GET -ErrorAction Stop
        $serverReady = $true
        Write-Success "Server is ready!"
    } catch {
        $retry++
        if ($retry -lt $maxRetries) {
            Write-Host "  Waiting... ($retry/$maxRetries)" -ForegroundColor Gray
            Start-Sleep -Seconds 1
        }
    }
}

if (-not $serverReady) {
    Write-Fail "Server did not start within timeout period"
    Write-Host "Make sure to start the server first with setup.ps1"
    exit 1
}

Write-Host ""
Write-Host "Starting tests..." -ForegroundColor Cyan

# ====================
# 1. Test Discovery Endpoint
# ====================
Write-Step "1. Discovery Endpoint"
$discovery = Test-API `
    -TestName "GET Discovery / API Info" `
    -Method "GET" `
    -Uri "$API_URL/" `
    -Validation {
        param($response)
        if ($response.apiName -eq "Smart Campus API" -and $response.version) {
            Write-Response "API Version: $($response.version)"
        }
    }

# ====================
# 2. Test Room CRUD Operations
# ====================
Write-Step "2. Room Management"

# Create a new room
$newRoomBody = @{
    roomId = "TEST101"
    name = "Test Room 101"
    building = "Test Building"
    floor = 1
    capacity = 25
}

$createdRoom = Test-API `
    -TestName "POST Create New Room" `
    -Method "POST" `
    -Uri "$API_URL/rooms" `
    -Body $newRoomBody `
    -ExpectedStatusCode 201 `
    -Validation {
        param($response)
        if ($response.roomId -eq "TEST101") {
            Write-Response "Created room: $($response.name)"
        }
    }

# Get all rooms
$allRooms = Test-API `
    -TestName "GET All Rooms" `
    -Method "GET" `
    -Uri "$API_URL/rooms" `
    -Validation {
        param($response)
        Write-Response "Found $($response.Count) rooms"
    }

# Get specific room
$specificRoom = Test-API `
    -TestName "GET Specific Room (L1)" `
    -Method "GET" `
    -Uri "$API_URL/rooms/L1" `
    -Validation {
        param($response)
        Write-Response "Room: $($response.name) - Capacity: $($response.capacity)"
    }

# Update room
$updateRoomBody = @{
    roomId = "TEST101"
    name = "Updated Test Room"
    building = "Test Building"
    floor = 1
    capacity = 30
}

$updatedRoom = Test-API `
    -TestName "PUT Update Room" `
    -Method "PUT" `
    -Uri "$API_URL/rooms/TEST101" `
    -Body $updateRoomBody `
    -Validation {
        param($response)
        Write-Response "Updated capacity: $($response.capacity)"
    }

# ====================
# 3. Test Sensor Operations
# ====================
Write-Step "3. Sensor Management"

# Create sensor with valid room
$newSensorBody = @{
    roomId = "L1"
    type = "TEMPERATURE"
    status = "ACTIVE"
    location = "Test Location"
}

$createdSensor = Test-API `
    -TestName "POST Create Sensor (Valid Room)" `
    -Method "POST" `
    -Uri "$API_URL/sensors" `
    -Body $newSensorBody `
    -ExpectedStatusCode 201 `
    -Validation {
        param($response)
        Write-Response "Created sensor: $($response.sensorId) in room $($response.roomId)"
    }

# Get all sensors
$allSensors = Test-API `
    -TestName "GET All Sensors" `
    -Method "GET" `
    -Uri "$API_URL/sensors" `
    -Validation {
        param($response)
        Write-Response "Found $($response.Count) sensors"
    }

# Get sensors filtered by type
$tempSensors = Test-API `
    -TestName "GET Sensors Filtered by Type (TEMPERATURE)" `
    -Method "GET" `
    -Uri "$API_URL/sensors?type=TEMPERATURE" `
    -Validation {
        param($response)
        Write-Response "Found $($response.Count) temperature sensors"
    }

# Get specific sensor
$specificSensor = Test-API `
    -TestName "GET Specific Sensor (T001)" `
    -Method "GET" `
    -Uri "$API_URL/sensors/T001" `
    -Validation {
        param($response)
        Write-Response "Sensor: $($response.sensorId) - Type: $($response.type) - Status: $($response.status)"
    }

# Try to create sensor with invalid room
Write-Host ""
Write-Host "  TEST: POST Create Sensor (Invalid Room - Should Fail)" -ForegroundColor Yellow
$invalidSensorBody = @{
    roomId = "INVALID_ROOM"
    type = "HUMIDITY"
    status = "ACTIVE"
    location = "Invalid"
}

try {
    $response = Invoke-RestMethod `
        -Uri "$API_URL/sensors" `
        -Method "POST" `
        -Body ($invalidSensorBody | ConvertTo-Json) `
        -ContentType $ContentType `
        -ErrorAction Stop
    Write-Fail "FAILED - Should have returned 404 error"
    $script:testsFailed++
} catch {
    if ($_.Exception.Response.StatusCode.Value__ -eq 404) {
        Write-Success "PASSED (Got expected 404 Not Found error)"
        $script:testsPassed++
    } else {
        Write-Fail "FAILED - Expected 404, got $($_.Exception.Response.StatusCode.Value__)"
        $script:testsFailed++
    }
}

# ====================
# 4. Test Sensor Readings
# ====================
Write-Step "4. Sensor Readings (Sub-Resource)"

# Get reading history for sensor
$readingHistory = Test-API `
    -TestName "GET Reading History for Sensor (T001)" `
    -Method "GET" `
    -Uri "$API_URL/sensors/T001/readings" `
    -Validation {
        param($response)
        Write-Response "Found $($response.Count) readings"
    }

# Add new reading to active sensor
$newReadingBody = @{
    value = 24.5
    unit = "°C"
    timestamp = (Get-Date -Format "yyyy-MM-ddTHH:mm:ss")
}

$newReading = Test-API `
    -TestName "POST Add Reading to Active Sensor (T001)" `
    -Method "POST" `
    -Uri "$API_URL/sensors/T001/readings" `
    -Body $newReadingBody `
    -ExpectedStatusCode 201 `
    -Validation {
        param($response)
        Write-Response "Created reading: $($response.readingId) with value $($response.value)$($response.unit)"
    }

# Try to add reading to maintenance sensor
Write-Host ""
Write-Host "  TEST: POST Add Reading to Maintenance Sensor (T002 - Should Fail)" -ForegroundColor Yellow
$readingForMaintenanceSensorBody = @{
    value = 25.0
    unit = "°C"
}

try {
    $response = Invoke-RestMethod `
        -Uri "$API_URL/sensors/T002/readings" `
        -Method "POST" `
        -Body ($readingForMaintenanceSensorBody | ConvertTo-Json) `
        -ContentType $ContentType `
        -ErrorAction Stop
    Write-Fail "FAILED - Should have returned 503 error for maintenance sensor"
    $script:testsFailed++
} catch {
    if ($_.Exception.Response.StatusCode.Value__ -eq 503) {
        Write-Success "PASSED (Got expected 503 Service Unavailable error)"
        $script:testsPassed++
    } else {
        Write-Fail "FAILED - Expected 503, got $($_.Exception.Response.StatusCode.Value__)"
        $script:testsFailed++
    }
}

# ====================
# 5. Test Error Handling
# ====================
Write-Step "5. Error Handling"

# Try to delete room with sensors
Write-Host ""
Write-Host "  TEST: DELETE Room with Sensors (L1 - Should Fail)" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod `
        -Uri "$API_URL/rooms/L1" `
        -Method "DELETE" `
        -ErrorAction Stop
    Write-Fail "FAILED - Should have returned 409 error for room with sensors"
    $script:testsFailed++
} catch {
    if ($_.Exception.Response.StatusCode.Value__ -eq 409) {
        Write-Success "PASSED (Got expected 409 Conflict error)"
        $script:testsPassed++
    } else {
        Write-Fail "FAILED - Expected 409, got $($_.Exception.Response.StatusCode.Value__)"
        $script:testsFailed++
    }
}

# Delete the test room we created (it has no sensors)
Test-API `
    -TestName "DELETE Room with No Sensors (TEST101)" `
    -Method "DELETE" `
    -Uri "$API_URL/rooms/TEST101" `
    -Validation {
        param($response)
        Write-Response "Deleted room: $($response.roomId)"
    }

# ====================
# Summary
# ====================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Success "Tests Passed: $testsPassed"
if ($testsFailed -gt 0) {
    Write-Fail "Tests Failed: $testsFailed"
} else {
    Write-Success "Tests Failed: $testsFailed"
}
Write-Host ""
Write-Host "Total Tests: $($testsPassed + $testsFailed)" -ForegroundColor Yellow
Write-Host ""

if ($testsFailed -eq 0) {
    Write-Success "ALL TESTS PASSED! ✅"
    Write-Host "Your Smart Campus API is working perfectly!" -ForegroundColor Green
    exit 0
} else {
    Write-Fail "SOME TESTS FAILED ❌"
    exit 1
}
