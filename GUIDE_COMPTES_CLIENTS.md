# 👥 Guide - Création de Plusieurs Comptes Clients

## ✅ Oui, vous pouvez créer plusieurs comptes clients !

L'application permet de créer **autant de comptes clients que vous voulez**. Chaque compte doit avoir :
- ✅ Un **email unique** (différent pour chaque compte)
- ✅ Un **nom d'utilisateur unique**
- ✅ Un **mot de passe** (minimum 6 caractères)

## 🚀 Comment Créer un Nouveau Compte

### Étape 1 : Ouvrir l'écran d'inscription

1. Lancez l'application
2. Sur l'écran de connexion, cliquez sur **"Créer un compte"** (bouton en bas)

### Étape 2 : Remplir le formulaire

Remplissez tous les champs :
- **Nom d'utilisateur** : doit être unique (ex: `client1`, `ahmed123`, etc.)
- **Nom complet** : votre nom complet
- **Date de naissance** : format `AAAA-MM-JJ` (ex: `1995-05-15`)
- **Email** : doit être unique (ex: `client1@test.com`, `ahmed@example.com`)
- **Numéro de téléphone** : format `+216 12 345 678` ou `12345678`
- **Genre** : Homme, Femme, ou Autre
- **Mot de passe** : minimum 6 caractères
- **Confirmer le mot de passe** : doit correspondre

### Étape 3 : Valider

Cliquez sur **"Créer un compte"**. Si tout est correct, vous verrez "Inscription réussie 🎉"

## 📋 Exemples de Comptes de Test

Voici des exemples de comptes que vous pouvez créer pour tester :

### Compte 1 - Client Standard
```
Nom d'utilisateur: client1
Email: client1@test.com
Mot de passe: client123
Téléphone: +216 12 345 678
Date de naissance: 1995-05-15
Genre: Homme
```

### Compte 2 - Client Féminin
```
Nom d'utilisateur: sarah_client
Email: sarah@test.com
Mot de passe: sarah123
Téléphone: +216 98 765 432
Date de naissance: 1998-08-20
Genre: Femme
```

### Compte 3 - Client Étudiant
```
Nom d'utilisateur: etudiant2024
Email: etudiant@test.com
Mot de passe: etudiant123
Téléphone: +216 55 123 456
Date de naissance: 2000-01-10
Genre: Homme
```

### Compte 4 - Client Professionnel
```
Nom d'utilisateur: pro_client
Email: pro@test.com
Mot de passe: pro123456
Téléphone: +216 71 234 567
Date de naissance: 1990-12-25
Genre: Femme
```

## 🔐 Compte Existant (yosra@test.com)

Le compte `yosra@test.com` avec le mot de passe `yosra123` est déjà créé et fonctionne. Vous pouvez :
- ✅ Vous connecter avec ce compte
- ✅ Créer d'autres comptes avec des emails différents
- ✅ Basculer entre les comptes en vous déconnectant et reconnectant

## 🔄 Comment Changer de Compte

### Méthode 1 : Déconnexion depuis l'app
1. Allez dans **Profil**
2. Cliquez sur **Déconnexion**
3. Reconnectez-vous avec un autre compte ou créez-en un nouveau

### Méthode 2 : Supprimer les données de l'app
1. Paramètres Android > Applications > Darna
2. **Effacer les données** ou **Désinstaller/Réinstaller**
3. Relancez l'app et connectez-vous avec un autre compte

## ⚠️ Règles Importantes

### Emails Uniques
- ❌ Vous **ne pouvez pas** créer deux comptes avec le même email
- ✅ Chaque compte doit avoir un email différent

### Noms d'utilisateur Uniques
- ❌ Vous **ne pouvez pas** utiliser le même nom d'utilisateur deux fois
- ✅ Chaque compte doit avoir un nom d'utilisateur unique

### Validation
- ✅ Email doit être au format valide (ex: `user@domain.com`)
- ✅ Mot de passe minimum 6 caractères
- ✅ Téléphone doit contenir uniquement des chiffres (avec ou sans +)
- ✅ Date de naissance au format `AAAA-MM-JJ`

## 🧪 Scénarios de Test Recommandés

### Test 1 : Créer 3 comptes différents
Créez 3 comptes avec des emails différents et testez la connexion avec chacun.

### Test 2 : Tester les réservations
- Connectez-vous avec le compte 1
- Réservez une visite
- Déconnectez-vous
- Connectez-vous avec le compte 2
- Vérifiez que vous voyez vos propres réservations

### Test 3 : Tester les rôles
- Créez un compte **client** (role = "client")
- Créez un compte **collocator** (si l'interface le permet)
- Vérifiez que les interfaces sont différentes selon le rôle

## 📱 Interface d'Inscription

L'écran d'inscription (`SignUpScreen`) contient :
- ✅ Formulaire complet avec validation
- ✅ Messages d'erreur clairs
- ✅ Indicateur de chargement
- ✅ Confirmation de succès

## 🔍 Vérification dans le Backend

Tous les comptes créés sont stockés dans MongoDB. Vous pouvez vérifier :
- Via Swagger : `http://192.168.1.109:3007/api`
- Endpoint : `GET /users` (nécessite authentification admin)

## 💡 Astuce

Pour tester rapidement, créez des comptes avec des emails simples :
- `test1@test.com`, `test2@test.com`, `test3@test.com`
- Mots de passe simples : `test123`, `test456`, etc.

Cela facilite les tests sans avoir à retenir des informations complexes.

---

**Note** : Le compte `yosra@test.com` est un compte de test existant. Vous pouvez créer autant de nouveaux comptes que nécessaire pour vos tests !




