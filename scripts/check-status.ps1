# check-status.ps1 - Quick status check
Write-Host "=== Quick Status Check ===" -ForegroundColor Cyan

Write-Host "`n1. Container Status:" -ForegroundColor Yellow
docker-compose ps

Write-Host "`n2. Recent Logs:" -ForegroundColor Yellow
docker-compose logs --tail=5 cognitive-bank-application

Write-Host "`n3. Quick Health Check:" -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/api/cognitive/bank/actuator/health" -TimeoutSec 5
    Write-Host "✓ Application: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "✗ Application: Not responding" -ForegroundColor Red
}

try {
    Invoke-WebRequest -Uri "http://localhost:9090/-/healthy" -TimeoutSec 5 | Out-Null
    Write-Host "✓ Prometheus: Healthy" -ForegroundColor Green
} catch {
    Write-Host "✗ Prometheus: Not responding" -ForegroundColor Red
}

try {
    Invoke-WebRequest -Uri "http://localhost:3000/api/health" -TimeoutSec 5 | Out-Null
    Write-Host "✓ Grafana: Running" -ForegroundColor Green
} catch {
    Write-Host "✗ Grafana: Not responding" -ForegroundColor Red
}

Write-Host "`n4. Open Dashboards:" -ForegroundColor Yellow
Write-Host "  Grafana: http://localhost:3000" -ForegroundColor White
Write-Host "  Prometheus: http://localhost:9090" -ForegroundColor White
Write-Host "  Application: http://localhost:8080/api/cognitive/bank" -ForegroundColor White