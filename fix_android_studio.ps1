# Script pour resoudre les problemes de verrous Android Studio
Write-Host "Nettoyage des processus Android Studio..." -ForegroundColor Cyan

# 1. Arreter tous les processus Android Studio
Write-Host "`n1. Arret des processus..." -ForegroundColor Yellow
$processes = @("studio64", "idea64", "java")
foreach ($proc in $processes) {
    $procs = Get-Process -Name $proc -ErrorAction SilentlyContinue | Where-Object { 
        $_.Path -like "*Android Studio*" -or $_.Path -like "*JetBrains*" 
    }
    if ($procs) {
        Write-Host "   Arret de $proc..." -ForegroundColor Gray
        $procs | Stop-Process -Force -ErrorAction SilentlyContinue
        Start-Sleep -Milliseconds 500
    }
}

# 2. Utiliser taskkill pour forcer l'arret
Write-Host "`n2. Arret force avec taskkill..." -ForegroundColor Yellow
taskkill /F /IM studio64.exe /T 2>$null
taskkill /F /IM idea64.exe /T 2>$null
Start-Sleep -Seconds 2

# 3. Nettoyer les verrous
Write-Host "`n3. Nettoyage des verrous..." -ForegroundColor Yellow
$lockPaths = @(
    "$env:USERPROFILE\.AndroidStudio*\system\caches",
    "$env:USERPROFILE\.AndroidStudio*\system\index",
    "$env:USERPROFILE\.AndroidStudio*\system\lock",
    "$env:LOCALAPPDATA\Google\AndroidStudio*\system\caches",
    "$env:LOCALAPPDATA\Google\AndroidStudio*\system\index",
    "$env:LOCALAPPDATA\Google\AndroidStudio*\system\lock"
)

$lockCount = 0
foreach ($path in $lockPaths) {
    if (Test-Path $path) {
        Get-ChildItem $path -Filter "*.lock" -Recurse -ErrorAction SilentlyContinue | ForEach-Object {
            Remove-Item $_.FullName -Force -ErrorAction SilentlyContinue
            $lockCount++
        }
    }
}

if ($lockCount -gt 0) {
    Write-Host "   OK: $lockCount verrou(x) supprime(s)" -ForegroundColor Green
} else {
    Write-Host "   Info: Aucun verrou trouve" -ForegroundColor Gray
}

# 4. Verifier les processus restants
Write-Host "`n4. Verification des processus restants..." -ForegroundColor Yellow
$remaining = Get-Process | Where-Object { 
    $_.ProcessName -like "*studio*" -or 
    ($_.ProcessName -eq "java" -and $_.Path -like "*Android Studio*")
}

if ($remaining) {
    Write-Host "   ATTENTION: Processus restants:" -ForegroundColor Red
    $remaining | ForEach-Object {
        Write-Host "      - $($_.ProcessName) (PID: $($_.Id))" -ForegroundColor Red
    }
    Write-Host "`n   Essayez de les tuer manuellement avec le Gestionnaire des taches" -ForegroundColor Yellow
} else {
    Write-Host "   OK: Aucun processus Android Studio en cours" -ForegroundColor Green
}

Write-Host "`nNettoyage termine!" -ForegroundColor Green
Write-Host "`nVous pouvez maintenant redemarrer Android Studio." -ForegroundColor Cyan
