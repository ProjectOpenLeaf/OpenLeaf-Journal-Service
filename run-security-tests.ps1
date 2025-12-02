<#
==============================================================================
OpenLeaf Journal Service - Security Testing Script (PowerShell)
==============================================================================
This script runs OWASP ZAP security scans against your Journal Service
Usage: .\run-security-tests.ps1 [baseline|api|full|docker]
==============================================================================
#>

param(
    [Parameter(Position=0)]
    [ValidateSet('baseline', 'api', 'full', 'docker', 'help')]
    [string]$Mode = 'baseline'
)

# Configuration
$SERVICE_URL = 'http://localhost:8083'
$REPORT_DIR = '.\zap-reports'
$ZAP_CONFIG_DIR = '.\.zap'

# Create report directory
if (-not (Test-Path $REPORT_DIR)) {
    New-Item -ItemType Directory -Path $REPORT_DIR -Force | Out-Null
    Write-Host "Created report directory: $REPORT_DIR" -ForegroundColor Green
}

# Check if service is running
function Test-ServiceRunning {
    Write-Host '=========================================' -ForegroundColor Cyan
    Write-Host 'Checking if Journal Service is running...' -ForegroundColor Cyan
    Write-Host '=========================================' -ForegroundColor Cyan

    try {
        $response = Invoke-WebRequest -Uri "$SERVICE_URL/actuator/health" -UseBasicParsing -ErrorAction Stop -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host 'Service is running' -ForegroundColor Green
            return $true
        }
    }
    catch {
        Write-Host 'Service is not running' -ForegroundColor Red
        Write-Host 'Please start your service first or use docker mode' -ForegroundColor Yellow
        return $false
    }
}

# Run ZAP Baseline Scan
function Invoke-BaselineScan {
    Write-Host '=========================================' -ForegroundColor Cyan
    Write-Host 'Running OWASP ZAP Baseline Scan' -ForegroundColor Cyan
    Write-Host '=========================================' -ForegroundColor Cyan

    docker run --rm --network="host" `
        -v "${PWD}/${REPORT_DIR}:/zap/wrk/:rw" `
        -v "${PWD}/${ZAP_CONFIG_DIR}:/zap/config:ro" `
        -t ghcr.io/zaproxy/zaproxy:stable `
        zap-baseline.py `
        -t $SERVICE_URL `
        -r zap-baseline-report.html `
        -w zap-baseline-report.md `
        -J zap-baseline-report.json `
        -a -j -l WARN

    Write-Host 'Baseline scan complete' -ForegroundColor Green
}

# Run ZAP API Scan
function Invoke-ApiScan {
    Write-Host '=========================================' -ForegroundColor Cyan
    Write-Host 'Running OWASP ZAP API Scan' -ForegroundColor Cyan
    Write-Host '=========================================' -ForegroundColor Cyan

    if (-not (Test-Path "$ZAP_CONFIG_DIR\openapi.yaml")) {
        Write-Host 'OpenAPI spec not found' -ForegroundColor Red
        exit 1
    }

    docker run --rm --network="host" `
        -v "${PWD}/${REPORT_DIR}:/zap/wrk/:rw" `
        -v "${PWD}/${ZAP_CONFIG_DIR}:/zap/config:ro" `
        -t ghcr.io/zaproxy/zaproxy:stable `
        zap-api-scan.py `
        -t $SERVICE_URL `
        -f openapi `
        -r zap-api-report.html `
        -w zap-api-report.md `
        -J zap-api-report.json `
        -a -j -l WARN

    Write-Host 'API scan complete' -ForegroundColor Green
}

# Run ZAP Full Scan
function Invoke-FullScan {
    Write-Host '=========================================' -ForegroundColor Cyan
    Write-Host 'Running OWASP ZAP Full Scan' -ForegroundColor Cyan
    Write-Host 'This may take 10-20 minutes' -ForegroundColor Yellow
    Write-Host '=========================================' -ForegroundColor Cyan

    $confirmation = Read-Host 'Continue? (y/n)'
    if ($confirmation -ne 'y' -and $confirmation -ne 'Y') {
        Write-Host 'Full scan cancelled' -ForegroundColor Yellow
        exit 0
    }

    docker run --rm --network="host" `
        -v "${PWD}/${REPORT_DIR}:/zap/wrk/:rw" `
        -v "${PWD}/${ZAP_CONFIG_DIR}:/zap/config:ro" `
        -t ghcr.io/zaproxy/zaproxy:stable `
        zap-full-scan.py `
        -t $SERVICE_URL `
        -r zap-full-report.html `
        -w zap-full-report.md `
        -J zap-full-report.json `
        -a -j -T 20

    Write-Host 'Full scan complete' -ForegroundColor Green
}

# Run with docker-compose
function Invoke-DockerMode {
    Write-Host '=========================================' -ForegroundColor Cyan
    Write-Host 'Running Security Tests with Docker Compose' -ForegroundColor Cyan
    Write-Host '=========================================' -ForegroundColor Cyan

    # Stop any existing containers
    Write-Host 'Cleaning up existing containers...'
    docker-compose -f docker-compose.security-test.yml down -v 2>$null

    # Start services and run scan
    Write-Host 'Building and starting services...' -ForegroundColor Yellow
    docker-compose -f docker-compose.security-test.yml up --build --abort-on-container-exit

    # Cleanup
    Write-Host 'Cleaning up...'
    docker-compose -f docker-compose.security-test.yml down -v

    Write-Host 'Docker mode complete' -ForegroundColor Green
}

# Parse ZAP JSON report and display summary
function Show-Summary {
    $reportPath = "$REPORT_DIR\zap-baseline-report.json"

    if (Test-Path $reportPath) {
        Write-Host '=========================================' -ForegroundColor Cyan
        Write-Host 'Security Scan Summary' -ForegroundColor Cyan
        Write-Host '=========================================' -ForegroundColor Cyan

        try {
            $report = Get-Content $reportPath -Raw | ConvertFrom-Json

            $alerts = $report.site[0].alerts
            $high = ($alerts | Where-Object { $_.riskcode -eq '3' }).Count
            $medium = ($alerts | Where-Object { $_.riskcode -eq '2' }).Count
            $low = ($alerts | Where-Object { $_.riskcode -eq '1' }).Count
            $info = ($alerts | Where-Object { $_.riskcode -eq '0' }).Count

            Write-Host ''
            Write-Host '+--------------+-------+'
            Write-Host '| Severity     | Count |'
            Write-Host '+--------------+-------+'
            Write-Host "| High         |  $high    |"
            Write-Host "| Medium       |  $medium    |"
            Write-Host "| Low          |  $low    |"
            Write-Host "| Info         |  $info    |"
            Write-Host '+--------------+-------+'
            Write-Host ''

            if ($high -gt 0 -or $medium -gt 0) {
                Write-Host 'Action required: High or Medium severity issues found' -ForegroundColor Red
            }
            else {
                Write-Host 'No critical security issues found' -ForegroundColor Green
            }
        }
        catch {
            Write-Host 'Could not parse JSON report' -ForegroundColor Yellow
        }

        Write-Host ''
        Write-Host 'Detailed reports available:'
        Write-Host "   HTML: $REPORT_DIR\zap-baseline-report.html"
        Write-Host "   Markdown: $REPORT_DIR\zap-baseline-report.md"
        Write-Host "   JSON: $REPORT_DIR\zap-baseline-report.json"
    }
}

# Show usage
function Show-Usage {
    Write-Host 'Usage: .\run-security-tests.ps1 [MODE]'
    Write-Host ''
    Write-Host 'Modes:'
    Write-Host '  baseline  - Quick passive scan (requires running service)'
    Write-Host '  api       - API scan using OpenAPI spec (requires running service)'
    Write-Host '  full      - Comprehensive active scan (requires running service)'
    Write-Host '  docker    - Run service and scan in Docker (standalone)'
    Write-Host '  help      - Show this help message'
    Write-Host ''
    Write-Host 'Examples:'
    Write-Host '  .\run-security-tests.ps1 baseline'
    Write-Host '  .\run-security-tests.ps1 docker'
}

# Main execution
Write-Host '=========================================' -ForegroundColor Cyan
Write-Host 'OpenLeaf Security Testing' -ForegroundColor Cyan
Write-Host '=========================================' -ForegroundColor Cyan

switch ($Mode) {
    'baseline' {
        if (-not (Test-ServiceRunning)) { exit 1 }
        Invoke-BaselineScan
        Show-Summary
    }
    'api' {
        if (-not (Test-ServiceRunning)) { exit 1 }
        Invoke-ApiScan
        Show-Summary
    }
    'full' {
        if (-not (Test-ServiceRunning)) { exit 1 }
        Invoke-FullScan
        Show-Summary
    }
    'docker' {
        Invoke-DockerMode
        Show-Summary
    }
    'help' {
        Show-Usage
    }
    default {
        Show-Usage
        exit 1
    }
}

Write-Host ''
Write-Host 'All done!' -ForegroundColor Green