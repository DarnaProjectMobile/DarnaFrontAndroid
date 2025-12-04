# Script pour résoudre les problèmes de verrouillage Android Studio
Write-Host "=== Nettoyage des verrous Android Studio ===" -ForegroundColor Cyan

# 1. Tuer tous les processus Android Studio
Write-Host "`n1. Arrêt des processus Android Studio..." -ForegroundColor Yellow
$processes = @("studio64.exe", "studio.exe", "idea64.exe", "idea.exe")
foreach ($proc in $processes) {
    $running = Get-Process -Name $proc -ErrorAction SilentlyContinue
    if ($running) {
        Write-Host "   Arrêt de $proc (PID: $($running.Id))..." -ForegroundColor Yellow
        Stop-Process -Name $proc -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 1
    }
}

# 2. Tuer les processus Java liés à Android Studio
Write-Host "`n2. Arrêt des processus Java d'Android Studio..." -ForegroundColor Yellow
Get-Process java -ErrorAction SilentlyContinue | Where-Object {
    $_.Path -like "*Android Studio*" -or $_.Path -like "*IntelliJ*"
} | ForEach-Object {
    Write-Host "   Arrêt du processus Java (PID: $($_.Id))..." -ForegroundColor Yellow
    Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
}

# 3. Supprimer les fichiers de verrouillage
Write-Host "`n3. Suppression des fichiers de verrouillage..." -ForegroundColor Yellow
$lockPaths = @(
    "$env:LOCALAPPDATA\Google\AndroidStudio*",
    "$env:APPDATA\Google\AndroidStudio*",
    "$env:USERPROFILE\.AndroidStudio*",
    "$env:USERPROFILE\.idea"
)

$lockCount = 0
foreach ($pattern in $lockPaths) {
    $paths = Get-ChildItem -Path (Split-Path $pattern -Parent) -Filter (Split-Path $pattern -Leaf) -ErrorAction SilentlyContinue
    foreach ($path in $paths) {
        if (Test-Path $path) {
            $locks = Get-ChildItem $path -Recurse -Filter "*.lock" -ErrorAction SilentlyContinue
            foreach ($lock in $locks) {
                Remove-Item $lock.FullName -Force -ErrorAction SilentlyContinue
                $lockCount++
                Write-Host "   Supprimé: $($lock.FullName)" -ForegroundColor Green
            }
        }
    }
}

# 4. Supprimer les verrous de port (si possible)
Write-Host "`n4. Vérification des ports utilisés..." -ForegroundColor Yellow
$ports = @(63342, 6942, 6943) # Ports communs d'Android Studio
foreach ($port in $ports) {
    $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connection) {
        Write-Host "   Port $port utilisé par PID: $($connection.OwningProcess)" -ForegroundColor Yellow
        Write-Host "   Utilisez: netstat -ano | findstr :$port pour plus d'infos" -ForegroundColor Gray
    }
}

Write-Host "`n=== Nettoyage terminé ===" -ForegroundColor Green
Write-Host "Fichiers de verrouillage supprimés: $lockCount" -ForegroundColor Cyan
Write-Host "`nVous pouvez maintenant redémarrer Android Studio." -ForegroundColor Green
Write-Host "Si le problème persiste, redémarrez votre ordinateur." -ForegroundColor Yellow













