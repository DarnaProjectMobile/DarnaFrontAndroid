# Configuration de l'URL du Backend

## Méthode 1 : Modifier via fichier (SANS Android Studio) ⭐ RECOMMANDÉ

Cette méthode permet de modifier l'URL sans recompiler l'application.

### Étapes :

1. **Ouvrez le fichier** : `app/src/main/assets/backend_url.txt`

2. **Modifiez l'URL** dans ce fichier :
   ```
   http://192.168.1.XXX:3007/
   ```
   Remplacez `XXX` par votre IP locale.

3. **Recompilez l'application** :
   ```bash
   ./gradlew clean build
   ```
   Ou dans Android Studio : `Build > Rebuild Project`

4. **Réinstallez l'application** sur votre téléphone/émulateur.

### Avantages :
- ✅ Pas besoin d'Android Studio pour modifier l'URL
- ✅ Simple : éditez juste un fichier texte
- ✅ L'URL est lue au démarrage de l'app

---

## Méthode 2 : Modifier via local.properties

### Étapes :

1. **Ouvrez le fichier** : `local.properties` (à la racine du projet)

2. **Ajoutez ou modifiez** la ligne :
   ```
   backend.url=http://192.168.1.XXX:3007/
   ```

3. **Recompilez l'application** :
   ```bash
   ./gradlew clean build
   ```

---

## Méthode 3 : Variable d'environnement

### Windows (PowerShell) :
```powershell
$env:DARNA_BACKEND_URL="http://192.168.1.XXX:3007/"
./gradlew clean build
```

### Linux/Mac :
```bash
export DARNA_BACKEND_URL="http://192.168.1.XXX:3007/"
./gradlew clean build
```

---

## Comment trouver votre IP locale ?

### Windows :
```powershell
ipconfig | findstr IPv4
```

### Linux/Mac :
```bash
ifconfig | grep inet
```

Cherchez une adresse de type `192.168.x.x` ou `192.168.0.x`.

⚠️ **IMPORTANT** : N'utilisez PAS les adresses `169.254.x.x` (APIPA) car elles ne sont pas accessibles depuis d'autres appareils.

---

## Priorité de configuration

L'application utilise les URLs dans cet ordre de priorité :

1. **Fichier assets/backend_url.txt** (si disponible)
2. **BuildConfig.SERVER_URL** (depuis local.properties ou variable d'environnement)
3. **URL par défaut** : `http://10.0.2.2:3007/` (pour émulateur)

---

## Vérification

Pour vérifier quelle URL est utilisée, consultez les logs Logcat dans Android Studio lors du démarrage de l'application.






