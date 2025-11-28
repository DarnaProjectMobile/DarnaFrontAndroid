# Guide de Dépannage - Problème de Connexion au Serveur

## ✅ Corrections Apportées

1. **Utilisation de `getBaseUrl(context)` au lieu de `BASE_URL`**
   - Les écrans utilisent maintenant `NetworkConfig.getBaseUrl(context)` qui lit le fichier `backend_url.txt`
   - Fichiers modifiés :
     - `LoginScreen.kt`
     - `HomeScreen.kt`
     - `SignUpScreen.kt`
     - `ReviewsScreen.kt`

2. **Amélioration des messages d'erreur**
   - Les messages d'erreur affichent maintenant l'URL utilisée et des instructions claires

## 🔧 Étapes pour Résoudre le Problème

### 1. Vérifier l'IP de votre ordinateur

**Windows (PowerShell) :**
```powershell
ipconfig | findstr IPv4
```

**Linux/Mac :**
```bash
ifconfig | grep inet
```

Cherchez une adresse de type `192.168.x.x` ou `192.168.0.x`.

⚠️ **IMPORTANT** : N'utilisez PAS les adresses `169.254.x.x` (APIPA) car elles ne sont pas accessibles depuis d'autres appareils.

### 2. Mettre à jour l'URL dans `backend_url.txt`

1. Ouvrez le fichier : `app/src/main/assets/backend_url.txt`
2. Remplacez l'IP par celle de votre ordinateur :
   ```
   http://192.168.1.XXX:3007/
   ```
   (Remplacez `XXX` par votre IP locale)

3. **Recompilez l'application** :
   ```bash
   ./gradlew clean build
   ```
   Ou dans Android Studio : `Build > Rebuild Project`

4. **Réinstallez l'application** sur votre téléphone/émulateur

### 3. Vérifier que le serveur NestJS est démarré

Dans le dossier `DarnaBackendNest`, exécutez :
```bash
npm run start
```

Vous devriez voir un message indiquant que le serveur écoute sur le port 3007.

### 4. Vérifier le réseau WiFi

- ✅ Le téléphone et l'ordinateur doivent être sur le **même réseau WiFi**
- ✅ Vérifiez que le WiFi est actif sur les deux appareils

### 5. Vérifier le firewall

**Windows :**
1. Ouvrez "Pare-feu Windows Defender"
2. Cliquez sur "Paramètres avancés"
3. Vérifiez que le port 3007 n'est pas bloqué

**Linux/Mac :**
```bash
sudo ufw allow 3007
```

### 6. Tester la connexion

Depuis votre téléphone, ouvrez un navigateur et allez à :
```
http://192.168.1.XXX:3007/
```

Si vous voyez une réponse du serveur, la connexion fonctionne.

## 📱 Configuration selon le Type d'Appareil

### Émulateur Android
Utilisez : `http://10.0.2.2:3007/`
(10.0.2.2 redirige vers localhost de la machine hôte)

### Téléphone Réel
Utilisez l'IP locale de votre ordinateur : `http://192.168.1.XXX:3007/`

## 🔍 Dépannage Avancé

### Vérifier que le serveur écoute sur toutes les interfaces

Dans votre backend NestJS, assurez-vous que le serveur écoute sur `0.0.0.0` et non seulement sur `localhost` :

```typescript
// Dans main.ts
await app.listen(3007, '0.0.0.0');
```

### Vérifier les logs Android

Dans Android Studio, ouvrez Logcat et filtrez par "OkHttp" ou "Retrofit" pour voir les erreurs de connexion détaillées.

### Tester avec curl

Depuis votre ordinateur :
```bash
curl http://192.168.1.XXX:3007/auth/login
```

## 📝 Fichiers Modifiés

- ✅ `app/src/main/java/com/sim/darna/screens/LoginScreen.kt`
- ✅ `app/src/main/java/com/sim/darna/screens/HomeScreen.kt`
- ✅ `app/src/main/java/com/sim/darna/screens/SignUpScreen.kt`
- ✅ `app/src/main/java/com/sim/darna/screens/ReviewsScreen.kt`
- ✅ `app/src/main/java/com/sim/darna/auth/LoginViewModel.kt`

## 🎯 Prochaines Étapes

1. Mettez à jour `backend_url.txt` avec votre IP actuelle
2. Recompilez l'application
3. Réinstallez sur votre téléphone
4. Testez la connexion

Si le problème persiste, vérifiez les logs Android dans Logcat pour plus de détails.




