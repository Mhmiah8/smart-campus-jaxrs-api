Write-Host "========== Testing currentValue Feature ==========" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api/v1"

# Test 1: Get T001 sensor before posting reading
Write-Host "1. GET T001 Sensor (before posting reading):" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/sensors/T001" -Method Get -UseBasicParsing
    $sensor = $response.Content | ConvertFrom-Json
    Write-Host ($sensor | ConvertTo-Json)
    $Before = $sensor.currentValue
    Write-Host "   currentValue before: $Before" -ForegroundColor Green
} catch {
    Write-Host "   ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 2: POST a new reading with value 25.5
Write-Host "2. POST New Reading (value: 25.5):" -ForegroundColor Yellow
try {
    $reading = @{
        value = 25.5
        timestamp = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
    } | ConvertTo-Json
    
    $response = Invoke-WebRequest -Uri "$baseUrl/sensors/T001/readings" `
        -Method Post `
        -Headers @{"Content-Type" = "application/json"} `
        -Body $reading `
        -UseBasicParsing
    
    Write-Host "   Response: $($response.StatusCode)" -ForegroundColor Green
    Write-Host ($response.Content | ConvertFrom-Json | ConvertTo-Json)
} catch {
    Write-Host "   ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 3: Get T001 sensor after posting reading
Write-Host "3. GET T001 Sensor (after posting reading):" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/sensors/T001" -Method Get -UseBasicParsing
    $sensor = $response.Content | ConvertFrom-Json
    Write-Host ($sensor | ConvertTo-Json)
    $After = $sensor.currentValue
    Write-Host "   currentValue after: $After" -ForegroundColor Green
    
    Write-Host ""
    if ($After -eq 25.5) {
        Write-Host "✓ SUCCESS: currentValue updated from $Before to $After" -ForegroundColor Green
    } else {
        Write-Host "✗ FAILED: Expected 25.5 but got $After" -ForegroundColor Red
    }
} catch {
    Write-Host "   ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========== Test Complete ==========" -ForegroundColor Cyan
