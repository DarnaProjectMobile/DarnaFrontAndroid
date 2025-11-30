# Script complet pour resoudre les problemes de verrouillage Android Studio
Write-Host '========================================' -ForegroundColor Cyan
Write-Host '  NETTOYAGE COMPLET ANDROID STUDIO' -ForegroundColor Cyan
Write-Host '========================================' -ForegroundColor Cyan

# 1. Arreter TOUS les processus Android Studio/IntelliJ
Write-Host "`n[1/5] Arret des processus..." -ForegroundColor Yellow
$processes = @('studio64', 'studio', 'idea64', 'idea')
$killed = 0
foreach ($procName in $processes) {
    $procs = Get-Process -Name $procName -ErrorAction SilentlyContinue
    foreach ($proc in $procs) {
        Write-Host "   Arret de $procName (PID: $($proc.Id))..." -ForegroundColor Yellow
        Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        $killed++
        Start-Sleep -Milliseconds 500
    }
}
Write-Host "   OK $killed processus arretes" -ForegroundColor Green

# 2. Arreter les processus Java lies
Write-Host "`n[2/5] Arret des processus Java lies..." -ForegroundColor Yellow
$javaProcs = Get-Process java -ErrorAction SilentlyContinue | Where-Object {
    $_.Path -like '*Android Studio*' -or $_.Path -like '*IntelliJ*' -or $_.MainWindowTitle -like '*Android*'
}
$javaKilled = 0
foreach ($java in $javaProcs) {
    Write-Host "   Arret de Java (PID: $($java.Id))..." -ForegroundColor Yellow
    Stop-Process -Id $java.Id -Force -ErrorAction SilentlyContinue
    $javaKilled++
}
Write-Host "   OK $javaKilled processus Java arretes" -ForegroundColor Green

# 3. Supprimer tous les fichiers .lock
Write-Host "`n[3/5] Suppression des fichiers de verrouillage..." -ForegroundColor Yellow
$lockPaths = @(
    "$env:LOCALAPPDATA\Google",
    "$env:APPDATA\Google",
    "$env:USERPROFILE\.AndroidStudio*",
    "$env:USERPROFILE\.idea",
    "$PWD\.idea"
)

$lockCount = 0
foreach ($basePath in $lockPaths) {
    if (Test-Path $basePath) {
        $locks = Get-ChildItem $basePath -Recurse -Filter '*.lock' -ErrorAction SilentlyContinue
        foreach ($lock in $locks) {
            try {
                Remove-Item $lock.FullName -Force -ErrorAction Stop
                $lockCount++
                Write-Host "   OK $($lock.Name)" -ForegroundColor Green
            } catch {
                Write-Host "   ERREUR: $($lock.FullName)" -ForegroundColor Red
            }
        }
    }
}
Write-Host "   OK $lockCount fichiers de verrouillage supprimes" -ForegroundColor Green

# 4. Verifier les ports
Write-Host "`n[4/5] Verification des ports..." -ForegroundColor Yellow
$ports = @(63342, 6942, 6943, 8080)
foreach ($port in $ports) {
    $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($conn) {
        Write-Host "   ATTENTION Port $port utilise par PID: $($conn.OwningProcess)" -ForegroundColor Yellow
    }
}

# 5. Verification finale
Write-Host "`n[5/5] Verification finale..." -ForegroundColor Yellow
$remaining = Get-Process | Where-Object {$_.ProcessName -match 'studio|idea'}
if ($remaining) {
    Write-Host '   ATTENTION Processus restants:' -ForegroundColor Yellow
    $remaining | ForEach-Object { Write-Host "      - $($_.ProcessName) (PID: $($_.Id))" -ForegroundColor Yellow }
} else {
    Write-Host '   OK Aucun processus Android Studio en cours' -ForegroundColor Green
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host '  NETTOYAGE TERMINE' -ForegroundColor Green
Write-Host '========================================' -ForegroundColor Cyan
Write-Host "`nVous pouvez maintenant redemarrer Android Studio." -ForegroundColor Green
Write-Host 'Si le probleme persiste:' -ForegroundColor Yellow
Write-Host '  1. Redemarrez votre ordinateur' -ForegroundColor White
Write-Host '  2. Utilisez Reset Settings dans Android Studio' -ForegroundColor White
Write-Host '  3. Verifiez les permissions administrateur' -ForegroundColor White
