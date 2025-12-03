# Guide : Gérer les Changements de WiFi

## 🔄 Problème : L'application ne fonctionne plus après un changement de WiFi

Quand vous changez de réseau WiFi, l'IP de votre ordinateur change aussi. L'application Android doit être mise à jour avec la nouvelle IP.

## ✅ Solution Rapide

### Étape 1 : Trouver la nouvelle IP du serveur

Quand vous démarrez le serveur NestJS, il affiche l'IP réseau :

```
🚀 Server running on:
   📍 Local:   http://localhost:3007
   🌐 Network: http://192.168.1.XXX:3007  ← Utilisez cette IP
```

### Étape 2 : Mettre à jour l'URL dans l'application

1. Ouvrez le fichier : `app/src/main/assets/backend_url.txt`
2. Remplacez l'ancienne IP par la nouvelle :
   ```
   http://192.168.1.XXX:3007/
   ```
   (Remplacez XXX par les chiffres de votre nouvelle IP)

3. **Recompilez l'application** :
   ```powershell
   .\gradlew clean build
   ```
   Ou dans Android Studio : `Build > Rebuild Project`

4. **Réinstallez l'application** :
   ```powershell
   .\gradlew installDebug
   ```
   Ou dans Android Studio : `Run > Run 'app'`

## 🔍 Trouver l'IP Manuellement

Si vous ne voyez pas l'IP dans la console du serveur :

**Windows :**
```powershell
ipconfig | findstr IPv4
```

Cherchez une IP qui commence par `192.168.` (pas `169.254.` qui est APIPA)

**Linux/Mac :**
```bash
ifconfig | grep inet
```

## ⚠️ IPs à Éviter

- ❌ `169.254.x.x` (APIPA) - Non accessible depuis d'autres appareils
- ❌ `192.168.56.x` (VirtualBox) - Non accessible depuis téléphone réel
- ❌ `localhost` ou `127.0.0.1` - Ne fonctionne que sur la machine serveur

## ✅ IPs Valides

- ✅ `192.168.1.x` - Réseau WiFi domestique classique
- ✅ `192.168.0.x` - Autre réseau WiFi domestique
- ✅ `10.0.2.2` - Pour émulateur Android uniquement

## 💡 Astuce : Automatisation

Pour éviter de recompiler à chaque changement de WiFi, vous pouvez :

1. Utiliser un nom de domaine local (si votre routeur le supporte)
2. Utiliser une IP fixe pour votre ordinateur dans les paramètres du routeur
3. Créer un script qui met à jour automatiquement `backend_url.txt`

## 📝 Fichiers à Modifier

- `app/src/main/assets/backend_url.txt` - URL du serveur
- `app/src/main/res/xml/network_security_config.xml` - Ajouter la nouvelle IP si nécessaire

## 🔄 Workflow Recommandé

1. Démarrez le serveur NestJS
2. Notez l'IP affichée (Network: http://...)
3. Mettez à jour `backend_url.txt` avec cette IP
4. Recompilez l'application
5. Réinstallez sur le téléphone
6. Testez la connexion

## ✅ Vérification

Pour vérifier que tout fonctionne :

1. Le serveur affiche : `🌐 Network: http://192.168.1.XXX:3007`
2. `backend_url.txt` contient : `http://192.168.1.XXX:3007/`
3. L'application se connecte sans erreur
























