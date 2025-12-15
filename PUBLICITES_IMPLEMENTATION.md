# Guide d'implémentation - Rubrique Publicités

Ce document décrit l'implémentation complète de la rubrique publicités pour votre application Android.

## 📋 Fichiers créés/modifiés

### Nouveaux fichiers créés :

1. **`app/src/main/java/com/sim/darna/data/remote/StripeService.kt`**
   - Service pour gérer les paiements Stripe

2. **`app/src/main/java/com/sim/darna/data/repository/StripeRepository.kt`**
   - Repository pour les opérations de paiement Stripe

3. **`app/src/main/java/com/sim/darna/viewmodel/StripeViewModel.kt`**
   - ViewModel pour gérer l'état du paiement

4. **`app/src/main/java/com/sim/darna/components/QRCodeDisplay.kt`**
   - Composant pour afficher les QR codes (base64)

5. **`app/src/main/java/com/sim/darna/components/RouletteWheel.kt`**
   - Composant de roulette pour le jeu

6. **`app/src/main/java/com/sim/darna/screens/PubliciteDetailScreen.kt`**
   - Écran de détail d'une publicité

7. **`app/src/main/java/com/sim/darna/screens/StripePaymentScreen.kt`**
   - Écran de paiement Stripe (optionnel)

### Fichiers modifiés :

1. **`app/src/main/java/com/sim/darna/data/model/Publicite.kt`**
   - Modèle mis à jour pour correspondre au backend NestJS

2. **`app/src/main/java/com/sim/darna/screens/AddPubliciteScreen.kt`**
   - Formulaire complet avec les 3 types de publicités

3. **`app/src/main/java/com/sim/darna/screens/PublicitesListScreen.kt`**
   - Liste mise à jour avec affichage des sponsors

4. **`app/src/main/java/com/sim/darna/viewmodel/PubliciteViewModel.kt`**
   - Ajout de la méthode `loadPubliciteDetail`

5. **`app/src/main/java/com/sim/darna/navigation/NavGraph.kt`**
   - Ajout des routes pour les publicités

6. **`app/src/main/java/com/sim/darna/di/NetworkModule.kt`**
   - Ajout du service Stripe

7. **`app/src/main/java/com/sim/darna/screens/HomeScreen.kt`**
   - Mise à jour de la navigation

## 🔧 Configuration backend nécessaire

### Endpoint Stripe

Votre backend NestJS doit avoir un endpoint pour créer un PaymentIntent :

```typescript
@Post('stripe/create-payment-intent')
async createPaymentIntent(@Body() body: { amount: number, currency: string }, @Request() req) {
  // Utiliser votre clé secrète Stripe
  const paymentIntent = await stripe.paymentIntents.create({
    amount: body.amount,
    currency: body.currency || 'eur',
  });
  
  return {
    clientSecret: paymentIntent.client_secret,
    paymentIntentId: paymentIntent.id
  };
}
```

### Clé publique Stripe

La clé publique fournie (`pk_test_51SWhKDHzDVVYaCTRXPPjTHX3wP0Qsz5aFDkOfK2ji9vd26xwucYJsFFKx271d767HVHN3f6hVC07wb6a0cnEcR5Y00UqB3vKCH`) est déjà utilisée dans le code.

## 📱 Fonctionnalités implémentées

### 1. Liste des publicités (`PublicitesListScreen`)
- ✅ Affichage de toutes les publicités
- ✅ Affichage du nom et logo du sponsor
- ✅ Bouton flottant pour ajouter (sponsors uniquement)
- ✅ Boutons modifier/supprimer (sponsor propriétaire uniquement)
- ✅ Navigation vers les détails au clic

### 2. Ajout/Modification (`AddPubliciteScreen`)
- ✅ Formulaire complet avec validation
- ✅ 3 types de publicités :
  - **Réduction** : champ pourcentage + conditions
  - **Promotion** : champ offre + conditions
  - **Jeu** : description + liste de gains pour la roulette
- ✅ Upload d'image (URL ou fichier)
- ✅ Paiement Stripe obligatoire avant publication (10€)
- ✅ Date d'expiration et catégorie

### 3. Détail d'une publicité (`PubliciteDetailScreen`)
- ✅ Affichage complet des informations
- ✅ QR Code pour les réductions
- ✅ Roulette interactive pour les jeux
- ✅ Bouton modifier (sponsor propriétaire uniquement)

### 4. Jeu de roulette (`RouletteWheel`)
- ✅ Animation de rotation
- ✅ Sélection aléatoire d'un gain
- ✅ Limitation à une seule partie par étudiant (à implémenter côté backend)

### 5. QR Code (`QRCodeDisplay`)
- ✅ Affichage du QR code (base64)
- ✅ Affichage du code promo

## 🔐 Sécurité et permissions

- ✅ Seuls les sponsors peuvent créer/modifier/supprimer des publicités
- ✅ Un sponsor ne peut modifier/supprimer que ses propres publicités
- ✅ Paiement obligatoire avant publication

## 🎨 Interface utilisateur

L'interface suit le design Material 3 avec :
- Cards pour les publicités
- Icons Material pour les actions
- Couleurs adaptées selon le type de publicité
- Animations pour la roulette

## 📝 Notes importantes

1. **Paiement Stripe** : Actuellement, le paiement ouvre une URL dans le navigateur. Pour une intégration complète, vous devrez :
   - Soit utiliser le SDK Stripe Android
   - Soit implémenter un webhook pour confirmer le paiement côté backend

2. **Upload d'image** : Le sélecteur d'image n'est pas encore implémenté. Vous pouvez utiliser une bibliothèque comme `accompanist-permissions` et `ImagePicker`.

3. **Gestion des jeux** : Pour limiter à une seule partie par étudiant, vous devrez :
   - Stocker les IDs des étudiants qui ont déjà joué côté backend
   - Vérifier avant d'autoriser le jeu

4. **UserSessionManager** : Vous devrez peut-être adapter la vérification du sponsor propriétaire pour utiliser l'ID utilisateur réel au lieu du token.

## 🚀 Prochaines étapes

1. Implémenter le sélecteur d'image pour l'upload
2. Intégrer complètement le SDK Stripe Android
3. Ajouter la gestion des jeux (limitation une partie)
4. Ajouter des filtres par catégorie dans la liste
5. Ajouter une recherche dans la liste des publicités

## 📦 Dépendances utilisées

Toutes les dépendances nécessaires sont déjà dans votre `build.gradle.kts`. Aucune dépendance supplémentaire n'est requise pour le moment.

Pour une intégration Stripe complète, vous pourriez ajouter :
```kotlin
implementation("com.stripe:stripe-android:20.37.0")
```

Mais ce n'est pas nécessaire pour l'implémentation actuelle qui utilise une URL de checkout.

