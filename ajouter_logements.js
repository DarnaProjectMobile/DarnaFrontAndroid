/**
 * Script pour ajouter plusieurs logements au backend Darna
 * 
 * Usage:
 *   node ajouter_logements.js
 * 
 * Assurez-vous que le backend est démarré et accessible.
 */

const logements = [
  {
    title: "Appartement 3 pièces",
    description: "Appartement spacieux de 3 pièces situé en centre ville, idéal pour la colocation étudiante.",
    address: "Centre Ville",
    price: 650.0,
    rooms: 3,
    surface: 65.0,
    available: true,
    location: {
      latitude: 45.764043,
      longitude: 4.835659
    }
  },
  {
    title: "Studio meublé",
    description: "Studio entièrement meublé, proche des transports en commun et des universités.",
    address: "Lyon",
    price: 450.0,
    rooms: 1,
    surface: 25.0,
    available: true,
    location: {
      latitude: 45.764043,
      longitude: 4.835659
    }
  },
  {
    title: "Chambre dans T4",
    description: "Chambre disponible dans un appartement T4 partagé avec d'autres étudiants.",
    address: "Marseille 8e",
    price: 380.0,
    rooms: 4,
    surface: 85.0,
    available: true,
    location: {
      latitude: 43.296482,
      longitude: 5.369780
    }
  },
  {
    title: "Studio meublé",
    description: "Studio moderne et meublé, proche du centre-ville et des commerces.",
    address: "Lyon",
    price: 480.0,
    rooms: 1,
    surface: 28.0,
    available: true,
    location: {
      latitude: 45.764043,
      longitude: 4.835659
    }
  }
];

// URL du backend - modifiez selon votre configuration
const BACKEND_URL = process.env.BACKEND_URL || "http://192.168.56.1:3007";

async function ajouterLogement(logement) {
  try {
    const response = await fetch(`${BACKEND_URL}/logement`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(logement)
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Erreur HTTP ${response.status}: ${errorText}`);
    }

    const result = await response.json();
    return result;
  } catch (error) {
    console.error(`Erreur lors de l'ajout du logement "${logement.title}":`, error.message);
    throw error;
  }
}

async function ajouterTousLesLogements() {
  console.log(`🚀 Début de l'ajout des logements vers ${BACKEND_URL}\n`);
  
  const resultats = [];
  
  for (let i = 0; i < logements.length; i++) {
    const logement = logements[i];
    console.log(`[${i + 1}/${logements.length}] Ajout de: ${logement.title} - ${logement.address}`);
    
    try {
      const resultat = await ajouterLogement(logement);
      resultats.push({ success: true, logement: logement.title, resultat });
      console.log(`  ✅ Ajouté avec succès (ID: ${resultat._id || resultat.id || 'N/A'})\n`);
    } catch (error) {
      resultats.push({ success: false, logement: logement.title, error: error.message });
      console.log(`  ❌ Échec: ${error.message}\n`);
    }
    
    // Petite pause entre les requêtes
    await new Promise(resolve => setTimeout(resolve, 500));
  }
  
  // Résumé
  console.log('\n📊 Résumé:');
  const reussis = resultats.filter(r => r.success).length;
  const echecs = resultats.filter(r => !r.success).length;
  console.log(`  ✅ Réussis: ${reussis}/${logements.length}`);
  console.log(`  ❌ Échecs: ${echecs}/${logements.length}`);
  
  if (echecs > 0) {
    console.log('\n❌ Logements en échec:');
    resultats.filter(r => !r.success).forEach(r => {
      console.log(`  - ${r.logement}: ${r.error}`);
    });
  }
  
  return resultats;
}

// Vérifier si fetch est disponible (Node.js 18+)
if (typeof fetch === 'undefined') {
  console.error('❌ Ce script nécessite Node.js 18+ ou installez node-fetch:');
  console.error('   npm install node-fetch');
  process.exit(1);
}

// Exécuter le script
ajouterTousLesLogements()
  .then(() => {
    console.log('\n✨ Terminé!');
    process.exit(0);
  })
  .catch(error => {
    console.error('\n💥 Erreur fatale:', error);
    process.exit(1);
  });


