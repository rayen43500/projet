# Test d'import BVMT — exemple ARTES/FINAC (2026-01-15)
# Prérequis : backend sur http://localhost:8081, MongoDB démarré

$ErrorActionPreference = "Stop"
$BaseUrl = if ($env:FGM_API_URL) { $env:FGM_API_URL } else { "http://localhost:8081" }
$Root = Split-Path -Parent $PSScriptRoot
$BvmtDir = Join-Path $Root "testdata\bvmt"

$txFile   = Join-Path $BvmtDir "transactions_20260115.txt"
$interFile = Join-Path $BvmtDir "intermediaires_20260115.txt"
$valFile  = Join-Path $BvmtDir "valeurs_20260115.txt"

foreach ($f in @($txFile, $interFile, $valFile)) {
    if (-not (Test-Path $f)) {
        Write-Error "Fichier manquant: $f"
    }
}

Write-Host "=== Login ===" -ForegroundColor Cyan
$loginBody = @{ username = "admin@fgm.local"; password = "Admin123!" } | ConvertTo-Json
$login = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
$token = $login.token
if (-not $token) { Write-Error "Login échoué" }

$headers = @{ Authorization = "Bearer $token" }

Write-Host "=== Préparation séance 2026-01-15 ===" -ForegroundColor Cyan
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/seances/preparer?date=2026-01-15" -Method POST -Headers $headers | Out-Null
} catch {
    Write-Host "(séance peut déjà exister)" -ForegroundColor Yellow
}

Write-Host "=== Import BVMT ===" -ForegroundColor Cyan
$boundary = [System.Guid]::NewGuid().ToString()
$LF = "`r`n"

function Add-FilePart([string]$name, [string]$path) {
    $bytes = [System.IO.File]::ReadAllBytes($path)
    $fileName = [System.IO.Path]::GetFileName($path)
    $header = "--$boundary${LF}Content-Disposition: form-data; name=`"$name`"; filename=`"$fileName`"${LF}Content-Type: text/plain${LF}${LF}"
    $footer = $LF
    $enc = [System.Text.Encoding]::UTF8
    $result = New-Object System.Collections.Generic.List[byte]
    $result.AddRange($enc.GetBytes($header))
    $result.AddRange($bytes)
    $result.AddRange($enc.GetBytes($footer))
    return $result.ToArray()
}

$bodyParts = New-Object System.Collections.Generic.List[byte]
$bodyParts.AddRange([System.Text.Encoding]::UTF8.GetBytes((Add-FilePart "transactionsFile" $txFile)))
$bodyParts.AddRange([System.Text.Encoding]::UTF8.GetBytes((Add-FilePart "intermediairesFile" $interFile)))
$bodyParts.AddRange([System.Text.Encoding]::UTF8.GetBytes((Add-FilePart "valeursFile" $valFile)))
$bodyParts.AddRange([System.Text.Encoding]::UTF8.GetBytes("--$boundary${LF}Content-Disposition: form-data; name=`"dateSeance`"${LF}${LF}2026-01-15${LF}"))
$bodyParts.AddRange([System.Text.Encoding]::UTF8.GetBytes("--$boundary--$LF"))

# Simpler approach with curl if available
if (Get-Command curl -ErrorAction SilentlyContinue) {
    $result = curl.exe -s -X POST "$BaseUrl/api/import/session" `
        -H "Authorization: Bearer $token" `
        -F "transactionsFile=@$txFile" `
        -F "intermediairesFile=@$interFile" `
        -F "valeursFile=@$valFile" `
        -F "dateSeance=2026-01-15"
    Write-Host $result
    $json = $result | ConvertFrom-Json
} else {
    Write-Host "curl non trouvé — utilisez l'import manuel dans le dashboard" -ForegroundColor Yellow
    exit 0
}

Write-Host "`n=== Résultats attendus ===" -ForegroundColor Green
Write-Host "R_val (ARTES/FINAC) ≈ 628 DT"
Write-Host "RS (suspens)        ≈ 56 DT"
if ($json.statistiques) {
    Write-Host "`n=== Statistiques import ===" -ForegroundColor Cyan
    $json.statistiques | Format-List
}
