# test-monitoring.ps1
Write-Host "=== Testing Monitoring Setup ===" -ForegroundColor Green

# Stop everything first
Write-Host "`n1. Stopping all services..." -ForegroundColor Yellow
docker-compose down

# Build the application
Write-Host "`n2. Building application..." -ForegroundColor Yellow
mvn clean package -DskipTests

# Start all services
Write-Host "`n3. Starting monitoring services..." -ForegroundColor Yellow
docker-compose up -d prometheus grafana postgres-exporter redis-exporter node-exporter

Start-Sleep -Seconds 10

Write-Host "`n4. Starting main application..." -ForegroundColor Yellow
docker-compose up -d cognitive-bank-application postgres redis

# Wait for everything to start
Write-Host "`n5. Waiting for services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Test endpoints
Write-Host "`n6. Testing endpoints..." -ForegroundColor Cyan

# Test 1: Application Health
try {
    $health = Invoke-WebRequest -Uri "http://localhost:8080/api/cognitive/bank/actuator/health" -UseBasicParsing
    Write-Host "✓ Application Health: OK" -ForegroundColor Green
} catch {
    Write-Host "✗ Application Health Failed" -ForegroundColor Red
}

# Test 2: Prometheus Metrics
try {
    $metrics = Invoke-WebRequest -Uri "http://localhost:8080/api/cognitive/bank/actuator/prometheus" -UseBasicParsing
    Write-Host "✓ Prometheus Metrics: Available" -ForegroundColor Green
} catch {
    Write-Host "✗ Prometheus Metrics Failed" -ForegroundColor Red
}

# Test 3: Prometheus Server
try {
    $prom = Invoke-WebRequest -Uri "http://localhost:9090/-/healthy" -UseBasicParsing
    Write-Host "✓ Prometheus Server: Healthy" -ForegroundColor Green
} catch {
    Write-Host "✗ Prometheus Server Failed" -ForegroundColor Red
}

# Test 4: Grafana
try {
    $grafana = Invoke-WebRequest -Uri "http://localhost:3000/api/health" -UseBasicParsing
    Write-Host "✓ Grafana: Running" -ForegroundColor Green
} catch {
    Write-Host "✗ Grafana Failed" -ForegroundColor Red
}

# Show dashboard URLs
Write-Host "`n=== Dashboard URLs ===" -ForegroundColor Yellow
Write-Host "Grafana: http://localhost:3000 (admin/admin)" -ForegroundColor White
Write-Host "Prometheus: http://localhost:9090" -ForegroundColor White
Write-Host "Application: http://localhost:8080/api/cognitive/bank" -ForegroundColor White
Write-Host "Health Check: http://localhost:8080/api/cognitive/bank/actuator/health" -ForegroundColor White

# Check if metrics are being scraped
Write-Host "`n=== Checking Prometheus Targets ===" -ForegroundColor Cyan
Start-Sleep -Seconds 5
Start-Process "http://localhost:9090/targets"

Write-Host "`n=== Opening Grafana ===" -ForegroundColor Cyan
Start-Sleep -Seconds 2
Start-Process "http://localhost:3000"

Write-Host "`nTest completed!" -ForegroundColor Green