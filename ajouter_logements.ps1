# Script PowerShell pour ajouter plusieurs logements au backend Darna
# 
# Usage:
#   .\ajouter_logements.ps1
# 
# Assurez-vous que le backend est démarré et accessible.

$BACKEND_URL = if ($env:BACKEND_URL) { $env:BACKEND_URL } else { "http://192.168.1.109:3007" }

$logements = @(
    @{
        title = "Appartement 3 pièces"
        description = "Appartement spacieux de 3 pièces situé en centre ville, idéal pour la colocation étudiante."
        address = "Centre Ville"
        price = 650.0
        rooms = 3
        surface = 65.0
        available = $true
        location = @{
            latitude = 45.764043
            longitude = 4.835659
        }
    },
    @{
        title = "Studio meublé"
        description = "Studio entièrement meublé, proche des transports en commun et des universités."
        address = "Lyon"
        price = 450.0
        rooms = 1
        surface = 25.0
        available = $true
        location = @{
            latitude = 45.764043
            longitude = 4.835659
        }
    },
    @{
        title = "Chambre dans T4"
        description = "Chambre disponible dans un appartement T4 partagé avec d'autres étudiants."
        address = "Marseille 8e"
        price = 380.0
        rooms = 4
        surface = 85.0
        available = $true
        location = @{
            latitude = 43.296482
            longitude = 5.369780
        }
    },
    @{
        title = "Studio meublé"
        description = "Studio moderne et meublé, proche du centre-ville et des commerces."
        address = "Lyon"
        price = 480.0
        rooms = 1
        surface = 28.0
        available = $true
        location = @{
            latitude = 45.764043
            longitude = 4.835659
        }
    }
)

Write-Host "🚀 Début de l'ajout des logements vers $BACKEND_URL" -ForegroundColor Cyan
Write-Host ""

$resultats = @()
$index = 1

foreach ($logement in $logements) {
    Write-Host "[$index/$($logements.Count)] Ajout de: $($logement.title) - $($logement.address)" -ForegroundColor Yellow
    
    try {
        $body = $logement | ConvertTo-Json -Depth 10
        $response = Invoke-RestMethod -Uri "$BACKEND_URL/logement" `
            -Method Post `
            -ContentType "application/json" `
            -Body $body `
            -ErrorAction Stop
        
        $resultats += @{ success = $true; logement = $logement.title; resultat = $response }
        $id = if ($response._id) { $response._id } elseif ($response.id) { $response.id } else { "N/A" }
        Write-Host "  ✅ Ajouté avec succès (ID: $id)" -ForegroundColor Green
        Write-Host ""
    }
    catch {
        $resultats += @{ success = $false; logement = $logement.title; error = $_.Exception.Message }
        Write-Host "  ❌ Échec: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
    }
    
    $index++
    Start-Sleep -Milliseconds 500
}

# Résumé
Write-Host "📊 Résumé:" -ForegroundColor Cyan
$reussis = ($resultats | Where-Object { $_.success }).Count
$echecs = ($resultats | Where-Object { -not $_.success }).Count
Write-Host "  ✅ Réussis: $reussis/$($logements.Count)" -ForegroundColor Green
Write-Host "  ❌ Échecs: $echecs/$($logements.Count)" -ForegroundColor $(if ($echecs -gt 0) { "Red" } else { "Green" })

if ($echecs -gt 0) {
    Write-Host ""
    Write-Host "❌ Logements en échec:" -ForegroundColor Red
    foreach ($resultat in ($resultats | Where-Object { -not $_.success })) {
        Write-Host "  - $($resultat.logement): $($resultat.error)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "✨ Terminé!" -ForegroundColor Cyan


