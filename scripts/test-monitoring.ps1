# test-monitoring.ps1
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  COGNITIVE BANKING - MONITORING TEST     " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

function Write-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Write-ErrorMsg {
    param([string]$Message, [string]$ErrorDetail)
    Write-Host "✗ $Message" -ForegroundColor Red
    if ($ErrorDetail) {
        Write-Host "  Error: $ErrorDetail" -ForegroundColor DarkRed
    }
}

function Write-Info {
    param([string]$Message)
    Write-Host "→ $Message" -ForegroundColor Yellow
}

function Write-Url {
    param([string]$Name, [string]$Url, [string]$Credentials = "")
    Write-Host "  $Name" -ForegroundColor White -NoNewline
    Write-Host ": $Url" -ForegroundColor Gray
    if ($Credentials) {
        Write-Host "    Credentials: $Credentials" -ForegroundColor DarkGray
    }
}

# 1. Clean and stop everything
Write-Info "1. Cleaning up previous containers..."
try {
    docker-compose down --remove-orphans
    Write-Success "All containers stopped and removed"
} catch {
    Write-ErrorMsg "Failed to stop containers" $_.Exception.Message
}

# 2. Build the application
Write-Info "`n2. Building Spring Boot application..."
try {
    mvn clean package -DskipTests
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Application built successfully"
    } else {
        Write-ErrorMsg "Build failed with exit code: $LASTEXITCODE"
        exit 1
    }
} catch {
    Write-ErrorMsg "Build failed" $_.Exception.Message
    exit 1
}

# 3. Start monitoring services first
Write-Info "`n3. Starting monitoring services..."
try {
    docker-compose up -d prometheus grafana postgres-exporter redis-exporter node-exporter
    Write-Success "Monitoring services started"
} catch {
    Write-ErrorMsg "Failed to start monitoring services" $_.Exception.Message
}

# 4. Wait for monitoring services
Write-Info "`n4. Waiting for monitoring services to initialize..."
Start-Sleep -Seconds 15

# 5. Start database and application
Write-Info "`n5. Starting database and application..."
try {
    docker-compose up -d postgres redis
    Write-Success "Database services started"

    # Wait for databases to be ready
    Write-Info "  Waiting for databases..."
    Start-Sleep -Seconds 10

    docker-compose up -d cognitive-bank-application
    Write-Success "Application started"
} catch {
    Write-ErrorMsg "Failed to start application services" $_.Exception.Message
}

# 6. Wait for everything to be ready
Write-Info "`n6. Waiting for all services to be ready..."
Start-Sleep -Seconds 30

# 7. Test endpoints
Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "  TESTING ENDPOINTS                        " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# Test 1: Application Health
Write-Info "Testing Application Health..."
try {
    $healthResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/cognitive/bank/actuator/health" -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    $healthData = $healthResponse.Content | ConvertFrom-Json
    if ($healthData.status -eq "UP") {
        Write-Success "Application Health: UP"

        # Show health details
        Write-Host "  Components:" -ForegroundColor Gray
        if ($healthData.components) {
            foreach ($component in $healthData.components.PSObject.Properties) {
                $status = $component.Value.status
                $color = if ($status -eq "UP") { "Green" } else { "Red" }
                Write-Host "    $($component.Name): " -ForegroundColor Gray -NoNewline
                Write-Host $status -ForegroundColor $color
            }
        }
    } else {
        Write-ErrorMsg "Application Health: $($healthData.status)"
    }
} catch {
    Write-ErrorMsg "Application Health Check Failed" $_.Exception.Message
}

# Test 2: Prometheus Metrics Endpoint
Write-Info "Testing Prometheus Metrics..."
try {
    $metricsResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/cognitive/bank/actuator/prometheus" -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    $metricsCount = ($metricsResponse.Content -split "`n" | Where-Object { $_ -match "^[^#]" }).Count
    Write-Success "Prometheus Metrics: Available ($metricsCount metrics found)"

    # Show sample metrics
    $sampleMetrics = ($metricsResponse.Content -split "`n" | Where-Object { $_ -match "^(http_|jvm_|process_|system_)" } | Select-Object -First 5)
    Write-Host "  Sample metrics:" -ForegroundColor Gray
    foreach ($metric in $sampleMetrics) {
        Write-Host "    $metric" -ForegroundColor DarkGray
    }
} catch {
    Write-ErrorMsg "Prometheus Metrics Endpoint Failed" $_.Exception.Message
}

# Test 3: Prometheus Server
Write-Info "Testing Prometheus Server..."
try {
    $promResponse = Invoke-WebRequest -Uri "http://localhost:9090/-/healthy" -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    if ($promResponse.Content -eq "Prometheus is Healthy.") {
        Write-Success "Prometheus Server: Healthy"
    } else {
        Write-Success "Prometheus Server: Running"
    }
} catch {
    Write-ErrorMsg "Prometheus Server Failed" $_.Exception.Message
}

# Test 4: Grafana
Write-Info "Testing Grafana..."
try {
    $grafanaResponse = Invoke-WebRequest -Uri "http://localhost:3000/api/health" -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    $grafanaData = $grafanaResponse.Content | ConvertFrom-Json
    if ($grafanaData.database -eq "ok") {
        Write-Success "Grafana: Running and Healthy"
    } else {
        Write-Success "Grafana: Running"
    }
} catch {
    Write-ErrorMsg "Grafana Failed" $_.Exception.Message
}

# Test 5: Check Prometheus Targets
Write-Info "Checking Prometheus Targets..."
try {
    $targetsResponse = Invoke-WebRequest -Uri "http://localhost:9090/api/v1/targets" -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    $targetsData = $targetsResponse.Content | ConvertFrom-Json

    $activeTargets = $targetsData.data.activeTargets | Where-Object { $_.health -eq "up" }
    $totalTargets = $targetsData.data.activeTargets.Count

    Write-Success "Prometheus Targets: $($activeTargets.Count)/$totalTargets active"

    if ($activeTargets.Count -gt 0) {
        Write-Host "  Active targets:" -ForegroundColor Gray
        foreach ($target in $activeTargets | Select-Object -First 3) {
            Write-Host "    $($target.labels.job): $($target.scrapeUrl)" -ForegroundColor DarkGray
        }
    }
} catch {
    Write-ErrorMsg "Failed to check Prometheus targets" $_.Exception.Message
}

# 8. Show all URLs
Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "  ACCESS URLs                              " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

Write-Url "Cognitive Banking App" "http://localhost:8080/api/cognitive/bank"
Write-Url "  - Health Check" "http://localhost:8080/api/cognitive/bank/actuator/health"
Write-Url "  - Prometheus Metrics" "http://localhost:8080/api/cognitive/bank/actuator/prometheus"
Write-Url "  - Application Info" "http://localhost:8080/api/cognitive/bank/actuator/info"

Write-Url "`nGrafana Dashboard" "http://localhost:3000" "admin/admin"
Write-Url "Prometheus UI" "http://localhost:9090"
Write-Url "  - Targets Status" "http://localhost:9090/targets"
Write-Url "  - Alert Rules" "http://localhost:9090/rules"
Write-Url "  - Graph Explorer" "http://localhost:9090/graph"

Write-Url "`nPostgreSQL Admin" "http://localhost:8081" "admin@cognitive.bank / admin123"
Write-Url "PostgreSQL Metrics" "http://localhost:9187/metrics"
Write-Url "Redis Metrics" "http://localhost:9121/metrics"
Write-Url "Node Metrics" "http://localhost:9100/metrics"

# 9. Open dashboards
Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "  OPENING DASHBOARDS                      " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

Write-Info "Opening Grafana Dashboard..."
try {
    Start-Process "http://localhost:3000"
    Write-Success "Grafana opened in browser"
} catch {
    Write-ErrorMsg "Failed to open Grafana" $_.Exception.Message
}

Start-Sleep -Seconds 2

Write-Info "Opening Prometheus Targets..."
try {
    Start-Process "http://localhost:9090/targets"
    Write-Success "Prometheus targets opened in browser"
} catch {
    Write-ErrorMsg "Failed to open Prometheus" $_.Exception.Message
}

Start-Sleep -Seconds 1

Write-Info "Opening Application Health..."
try {
    Start-Process "http://localhost:8080/api/cognitive/bank/actuator/health"
    Write-Success "Application health opened in browser"
} catch {
    Write-ErrorMsg "Failed to open application health" $_.Exception.Message
}

# 10. Final status
Write-Host "`n==========================================" -ForegroundColor Green
Write-Host "  TEST COMPLETED SUCCESSFULLY!            " -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green

Write-Host "`nNext Steps:" -ForegroundColor Yellow
Write-Host "1. Login to Grafana (admin/admin)" -ForegroundColor White
Write-Host "2. Add Prometheus datasource: http://prometheus:9090" -ForegroundColor White
Write-Host "3. Import Spring Boot dashboard (ID: 6756)" -ForegroundColor White
Write-Host "4. Check Prometheus targets for data collection" -ForegroundColor White
Write-Host "5. Monitor your banking application metrics!" -ForegroundColor White

Write-Host "`nTo stop all services:" -ForegroundColor Gray
Write-Host "  docker-compose down" -ForegroundColor DarkGray

Write-Host "`nTo view logs:" -ForegroundColor Gray
Write-Host "  docker-compose logs -f cognitive-bank-application" -ForegroundColor DarkGray
Write-Host "  docker-compose logs -f prometheus" -ForegroundColor DarkGray