# Validation ligne par ligne — exemple ARTES/FINAC (sans backend)
# Usage: .\scripts\validate-bvmt-sample.ps1

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$BvmtDir = Join-Path $Root "testdata\bvmt"

function Safe([string]$s, [int]$start, [int]$end) {
    if ($s.Length -lt $end) { return "" }
    return $s.Substring($start, $end - $start).Trim()
}

function Parse-Double([string]$s) {
    $v = 0.0
    [double]::TryParse($s.Trim(), [ref]$v) | Out-Null
    return $v
}

Write-Host "=== Fichiers BVMT ===" -ForegroundColor Cyan
foreach ($name in @("transactions_20260115.txt", "intermediaires_20260115.txt", "valeurs_20260115.txt")) {
    $p = Join-Path $BvmtDir $name
    if (-not (Test-Path $p)) { Write-Error "Manquant: $p" }
    Write-Host "  OK $name ($((Get-Item $p).Length) octets)"
}

$txLines = Get-Content (Join-Path $BvmtDir "transactions_20260115.txt") -Encoding UTF8
$valLines = Get-Content (Join-Path $BvmtDir "valeurs_20260115.txt") -Encoding UTF8

$clotures = @{}
foreach ($line in $valLines) {
    $lib = Safe $line 25 43
    $clot = Parse-Double (Safe $line 54 63)
    if ($lib) { $clotures[$lib] = $clot }
}

Write-Host "`n=== Transactions ===" -ForegroundColor Cyan
$seuil = 0.06
$delai = 2

foreach ($line in $txLines | Select-Object -Skip 1) {
    $clean = $line.Replace("`r", "").Trim()
    if ($clean.Length -lt 140) { continue }
    $date = Safe $clean 14 22
    $lib = Safe $clean 47 65
    $codeAch = [int](Safe $clean 72 80)
    $libAch = Safe $clean 80 90
    $codeVend = [int](Safe $clean 93 101)
    $libVend = Safe $clean 101 111
    $qty = [int](Safe $clean 114 123)
    $prixTotal = Parse-Double (Safe $clean 135 150)
    $montant = $prixTotal / 1000.0
    $cloture = $clotures[$lib]
    if (-not $cloture) { $cloture = 0 }

    Write-Host "  $date | $lib | vend=$codeVend $libVend -> ach=$codeAch $libAch | q=$qty | $montant TND | C=$cloture"

    # Vendeur ARTES (101)
    if ($codeVend -eq 101) {
        $qNet = -$qty
        $mNetSigned = -$montant
        $pnt = if ($qNet -ge 0) { "+" } else { "-" }
        $pne = if ($mNetSigned -gt 0) { "-" } elseif ($mNetSigned -lt 0) { "+" } else { "+" }
        $Q = [math]::Abs($qNet)
        $M = [math]::Abs($mNetSigned)
        $D = $seuil
        $p = $delai
        $chargeTitres = $cloture * $Q * [math]::Pow(1 + $D, $p)
        $rval = [math]::Abs($chargeTitres - $M)
        $rs = [math]::Abs($cloture * $Q - $M)

        Write-Host "`n=== Position ARTES / $lib (doc explicatif) ===" -ForegroundColor Green
        Write-Host "  PNT signe     : $pnt  (Q nette = $qNet)"
        Write-Host "  PNE signe     : $pne  (M nette = $([math]::Round($mNetSigned, 1)) TND)"
        Write-Host "  R_val (RV J)  : $([math]::Round($rval)) DT  (attendu: 628)"
        Write-Host "  RS (suspens)  : $([math]::Round($rs)) DT   (attendu: 56)"
        Write-Host "  RM (J seul)   : $([math]::Round($rval)) DT"
    }
}

Write-Host "`n=== Format ===" -ForegroundColor Cyan
Write-Host "  Texte fixe BVMT uniquement - CSV/Excel rejetes par BvmtImportValidator"
