# 🚀 Configuration Complète - Application Darna

## ✅ État Actuel

- **IP WiFi de votre ordinateur** : `192.168.56.1` ✅
- **URL dans backend_url.txt** : `http://192.168.56.1:3007/` ✅
- **Configuration réseau Android** : OK ✅
- **Code Android** : Toutes les corrections appliquées ✅

## ⚠️ Problème Identifié

Votre serveur NestJS écoute sur `169.254.133.122` (APIPA) au lieu de `192.168.56.1` (WiFi).

## 🔧 Solution : Configurer le Serveur NestJS

### Option 1 : Écouter sur toutes les interfaces (RECOMMANDÉ)

Dans votre fichier `main.ts` du backend NestJS, modifiez :

```typescript
// Avant
await app.listen(3007);

// Après
await app.listen(3007, '0.0.0.0');
```

Cela permettra au serveur d'écouter sur toutes les interfaces réseau, y compris votre WiFi.

### Option 2 : Écouter spécifiquement sur l'IP WiFi

```typescript
await app.listen(3007, '192.168.56.1');
```

### Vérification

Après avoir modifié et redémarré le serveur, vous devriez voir :

```
🚀 Server running on:
   📍 Local:   http://localhost:3007
   🌐 Network: http://192.168.56.1:3007  ✅ (au lieu de 169.254.133.122)
```

## 📱 Compilation de l'Application Android

### 1. Nettoyer le projet

```powershell
cd "C:\Users\Lenovo\Desktop\YOSRA YOSRA\DarnaFrontAndroid-main"
.\gradlew clean
```

### 2. Reconstruire le projet

```powershell
.\gradlew build
```

Ou dans Android Studio :
- `Build > Clean Project`
- `Build > Rebuild Project`

### 3. Si vous avez des erreurs de daemon Kotlin

Arrêtez tous les daemons Gradle :

```powershell
.\gradlew --stop
```

Puis reconstruisez :

```powershell
.\gradlew clean build
```

## ✅ Vérifications Finales

### 1. Vérifier que backend_url.txt est correct

Fichier : `app/src/main/assets/backend_url.txt`
Contenu attendu :
```
http://192.168.56.1:3007/
```

### 2. Vérifier network_security_config.xml

Le fichier doit contenir `192.168.56.1` dans les domaines autorisés (déjà fait ✅)

### 3. Tester la connexion depuis le navigateur

Depuis votre téléphone (sur le même WiFi), ouvrez :
```
http://192.168.56.1:3007/api
```

Vous devriez voir la documentation Swagger.

### 4. Installer l'application

```powershell
.\gradlew installDebug
```

Ou depuis Android Studio : `Run > Run 'app'`

## 🎯 Checklist de Fonctionnalités

Après compilation, vérifiez que ces fonctionnalités fonctionnent :

- [ ] **Connexion** : Se connecter avec `yosra@test.com` / `yosra123`
- [ ] **Inscription** : Créer un nouveau compte
- [ ] **Accueil** : Voir la liste des logements
- [ ] **Réservation** : Réserver une visite
- [ ] **Mes visites** : Voir les visites réservées
- [ ] **Profil** : Voir et modifier le profil
- [ ] **Demandes de visite** (pour colocataires) : Voir et gérer les demandes

## 🔍 Dépannage

### Problème : "Impossible de joindre le serveur"

1. Vérifiez que le serveur NestJS est démarré :
   ```bash
   cd DarnaBackendNest
   npm run start
   ```

2. Vérifiez que le serveur écoute sur `0.0.0.0` ou `192.168.56.1`

3. Vérifiez que le téléphone et l'ordinateur sont sur le même WiFi

4. Testez depuis le navigateur du téléphone : `http://192.168.56.1:3007/api`

### Problème : Erreurs de compilation Kotlin

```powershell
.\gradlew --stop
.\gradlew clean
.\gradlew build
```

### Problème : L'application ne lit pas backend_url.txt

1. Vérifiez que le fichier existe : `app/src/main/assets/backend_url.txt`
2. Vérifiez le contenu (une seule ligne avec l'URL)
3. Recompilez l'application
4. Réinstallez l'application

## 📝 Fichiers Modifiés (Récapitulatif)

✅ **Code Android corrigé** :
- `LoginScreen.kt` - Utilise `getBaseUrl(context)`
- `HomeScreen.kt` - Utilise `getBaseUrl(context)`
- `SignUpScreen.kt` - Utilise `getBaseUrl(context)`
- `ReviewsScreen.kt` - Utilise `getBaseUrl(context)`
- `LoginViewModel.kt` - Messages d'erreur améliorés

✅ **Configuration** :
- `backend_url.txt` - IP correcte (192.168.56.1)
- `network_security_config.xml` - IP autorisée
- `build.gradle.kts` - URL par défaut correcte

## 🎉 Prochaines Étapes

1. **Modifier le serveur NestJS** pour écouter sur `0.0.0.0`
2. **Redémarrer le serveur NestJS**
3. **Recompiler l'application Android**
4. **Installer sur le téléphone**
5. **Tester toutes les fonctionnalités**

---

**Note** : Si votre IP WiFi change, mettez simplement à jour `backend_url.txt` et recompilez.




