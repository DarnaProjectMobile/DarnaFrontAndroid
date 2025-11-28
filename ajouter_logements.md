# Guide pour ajouter les logements

Ce fichier contient les instructions pour ajouter plusieurs logements au backend.

## Logements à ajouter

1. **Appartement 3 pièces - Centre Ville**
2. **Studio meublé - Lyon** (premier)
3. **Chambre dans T4 - Marseille 8e**
4. **Studio meublé - Lyon** (deuxième)

## Méthode 1 : Utiliser le fichier JSON avec curl

```bash
# Lire l'URL du backend depuis le fichier de configuration
BACKEND_URL="http://192.168.1.109:3007"  # Remplacez par votre URL

# Ajouter chaque logement
curl -X POST "$BACKEND_URL/logement" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Appartement 3 pièces",
    "description": "Appartement spacieux de 3 pièces situé en centre ville, idéal pour la colocation étudiante.",
    "address": "Centre Ville",
    "price": 650.0,
    "rooms": 3,
    "surface": 65.0,
    "available": true,
    "location": {
      "latitude": 45.764043,
      "longitude": 4.835659
    }
  }'

curl -X POST "$BACKEND_URL/logement" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Studio meublé",
    "description": "Studio entièrement meublé, proche des transports en commun et des universités.",
    "address": "Lyon",
    "price": 450.0,
    "rooms": 1,
    "surface": 25.0,
    "available": true,
    "location": {
      "latitude": 45.764043,
      "longitude": 4.835659
    }
  }'

curl -X POST "$BACKEND_URL/logement" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Chambre dans T4",
    "description": "Chambre disponible dans un appartement T4 partagé avec d'autres étudiants.",
    "address": "Marseille 8e",
    "price": 380.0,
    "rooms": 4,
    "surface": 85.0,
    "available": true,
    "location": {
      "latitude": 43.296482,
      "longitude": 5.369780
    }
  }'

curl -X POST "$BACKEND_URL/logement" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Studio meublé",
    "description": "Studio moderne et meublé, proche du centre-ville et des commerces.",
    "address": "Lyon",
    "price": 480.0,
    "rooms": 1,
    "surface": 28.0,
    "available": true,
    "location": {
      "latitude": 45.764043,
      "longitude": 4.835659
    }
  }'
```

## Méthode 2 : Utiliser MongoDB directement

Si vous avez accès à MongoDB, vous pouvez insérer directement :

```javascript
// Se connecter à MongoDB
use darna

// Insérer les logements
db.logements.insertMany([
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
    },
    createdAt: new Date(),
    updatedAt: new Date()
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
    },
    createdAt: new Date(),
    updatedAt: new Date()
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
    },
    createdAt: new Date(),
    updatedAt: new Date()
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
    },
    createdAt: new Date(),
    updatedAt: new Date()
  }
])
```

## Méthode 3 : Utiliser Postman ou un client HTTP

1. Ouvrez Postman ou un autre client HTTP
2. Créez une requête POST vers `http://VOTRE_IP:3007/logement`
3. Ajoutez l'en-tête `Content-Type: application/json`
4. Utilisez le fichier `logements_a_ajouter.json` pour copier chaque logement
5. Envoyez les requêtes une par une

## Vérification

Après avoir ajouté les logements, vérifiez qu'ils sont bien présents :

```bash
# Via curl
curl "$BACKEND_URL/logement"

# Via MongoDB
db.logements.find().pretty()
```

Les logements devraient apparaître dans l'application Android avec les formats suivants :
- "Appartement 3 pièces - Centre Ville"
- "Studio meublé - Lyon"
- "Chambre dans T4 - Marseille 8e"
- "Studio meublé - Lyon"


