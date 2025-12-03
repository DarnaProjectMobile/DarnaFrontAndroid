# 🔧 Configuration IP du Serveur NestJS

## 📊 Situation Actuelle

D'après votre configuration réseau :

- **IP WiFi (téléphone réel)** : `192.168.1.101` ✅
- **IP VirtualBox** : `192.168.56.1` 
- **Serveur écoute actuellement sur** : `192.168.56.1:3007` ⚠️

## ⚠️ Problème

Le serveur NestJS écoute sur `192.168.56.1` (VirtualBox) au lieu de `192.168.1.101` (WiFi réel).

**Cela signifie que :**
- ✅ Un émulateur Android peut se connecter (via VirtualBox)
- ❌ Un téléphone réel sur WiFi ne peut PAS se connecter

## ✅ Solution : Configurer le Serveur NestJS

### Option 1 : Écouter sur toutes les interfaces (RECOMMANDÉ) ⭐

Modifiez le fichier `main.ts` de votre backend NestJS :

```typescript
// Avant
await app.listen(3007);

// Après
await app.listen(3007, '0.0.0.0');
```

**Avantages :**
- ✅ Fonctionne avec téléphone réel (192.168.1.101)
- ✅ Fonctionne avec émulateur (192.168.56.1)
- ✅ Fonctionne avec localhost
- ✅ Pas besoin de changer l'IP à chaque fois

### Option 2 : Écouter spécifiquement sur l'IP WiFi

```typescript
await app.listen(3007, '192.168.1.101');
```

**Avantages :**
- ✅ Sécurisé (écoute uniquement sur WiFi)
- ⚠️ Nécessite de changer l'IP si le WiFi change

## 📱 Configuration Android

### Fichier `backend_url.txt`

Le fichier `app/src/main/assets/backend_url.txt` contient déjà la bonne IP :

```
http://192.168.1.101:3007/
```

### Fichier `local.properties`

Le fichier `local.properties` a été mis à jour avec :

```
backend.url=http://192.168.1.101:3007/
```

### Fichier `network_security_config.xml`

Les deux IPs sont déjà autorisées :
- ✅ `192.168.1.101` (WiFi réel)
- ✅ `192.168.56.1` (VirtualBox)

## 🚀 Étapes pour Activer

### 1. Modifier le serveur NestJS

Dans votre projet backend NestJS, ouvrez `src/main.ts` et modifiez :

```typescript
async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  
  // ... autres configurations ...
  
  // Écouter sur toutes les interfaces
  await app.listen(3007, '0.0.0.0');
  
  console.log(`🚀 Server running on:`);
  console.log(`   📍 Local:   http://localhost:3007`);
  console.log(`   🌐 Network: http://192.168.1.101:3007`);
}
```

### 2. Redémarrer le serveur

```bash
# Arrêter le serveur actuel (Ctrl+C)
# Puis redémarrer
npm run start
```

Vous devriez maintenant voir :

```
🚀 Server running on:
   📍 Local:   http://localhost:3007
   🌐 Network: http://192.168.1.101:3007  ✅
```

### 3. Recompiler l'application Android

```powershell
cd "C:\Users\Lenovo\Desktop\YOSRA YOSRA\DarnaFrontAndroid-main"
.\gradlew clean build
```

Ou dans Android Studio :
- `Build > Clean Project`
- `Build > Rebuild Project`

### 4. Installer sur le téléphone

1. Connectez votre téléphone au même WiFi (`home`)
2. Installez l'APK généré
3. Testez la connexion

## ✅ Vérifications

### Test 1 : Vérifier que le serveur écoute

Dans PowerShell :

```powershell
netstat -an | findstr 3007
```

Vous devriez voir :
```
TCP    0.0.0.0:3007           0.0.0.0:0              LISTENING
```

### Test 2 : Tester depuis le navigateur

Sur votre téléphone, ouvrez le navigateur et allez sur :
```
http://192.168.1.101:3007/api
```

Vous devriez voir la documentation Swagger.

### Test 3 : Tester depuis l'application

1. Ouvrez l'application
2. Essayez de vous connecter
3. Si ça fonctionne, vous verrez l'écran d'accueil ✅

## 🔍 Dépannage

### Si le téléphone ne peut toujours pas se connecter :

1. **Vérifier le firewall Windows**
   - Ouvrez "Pare-feu Windows Defender"
   - Vérifiez que le port 3007 est autorisé

2. **Vérifier que le téléphone est sur le même WiFi**
   - WiFi du PC : `home`
   - WiFi du téléphone : doit être `home` aussi

3. **Vérifier l'IP du serveur**
   - Redémarrez le serveur NestJS
   - Vérifiez qu'il affiche `192.168.1.101:3007`

4. **Tester avec ping**
   - Sur le téléphone, installez une app de ping
   - Ping `192.168.1.101`
   - Si ça ne fonctionne pas, problème de réseau WiFi

## 📝 Notes

- **Pour émulateur** : Utilisez `http://10.0.2.2:3007/` dans `backend_url.txt`
- **Pour téléphone réel** : Utilisez `http://192.168.1.101:3007/` dans `backend_url.txt`
- **Si le WiFi change** : Mettez à jour `backend_url.txt` avec la nouvelle IP

---

## 🚨 PROBLÈME DE CONNEXION ACTUEL

### Diagnostic du problème

D'après les logs de l'application :
```
failed to connect to /192.168.56.1 (port 3007) from /192.168.137.57 (port 58708)
```

**Problème identifié :**
- ❌ Le serveur écoute sur `192.168.56.1:3007` (VirtualBox)
- ❌ Le téléphone est sur l'IP `192.168.137.57` (réseau différent)
- ❌ Les deux appareils ne sont pas sur le même réseau → **connexion impossible**

### ✅ SOLUTION : Configurer le serveur NestJS pour écouter sur toutes les interfaces

**Cette solution permettra au téléphone de se connecter quel que soit le réseau.**

#### Étape 1 : Modifier le fichier `main.ts` du backend NestJS

Dans votre projet backend NestJS, ouvrez le fichier `src/main.ts` et modifiez :

```typescript
// ❌ AVANT (écoute uniquement sur VirtualBox)
await app.listen(3007);
// ou
await app.listen(3007, '192.168.56.1');

// ✅ APRÈS (écoute sur toutes les interfaces)
await app.listen(3007, '0.0.0.0');
```

**Exemple complet :**
```typescript
async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  
  // ... autres configurations ...
  
  // Écouter sur toutes les interfaces (0.0.0.0)
  await app.listen(3007, '0.0.0.0');
  
  console.log(`🚀 Server running on:`);
  console.log(`   📍 Local:   http://localhost:3007`);
  console.log(`   🌐 Network: Accessible sur toutes les interfaces`);
}
```

#### Étape 2 : Redémarrer le serveur NestJS

1. Arrêtez le serveur actuel (Ctrl+C dans le terminal)
2. Redémarrez-le :
   ```bash
   npm run start
   ```

Vous devriez voir un message indiquant que le serveur écoute sur toutes les interfaces.

#### Étape 3 : Trouver la bonne IP pour le téléphone

Après avoir configuré le serveur pour écouter sur `0.0.0.0`, vous devez identifier l'IP à utiliser depuis votre téléphone.

**Méthode 1 : Vérifier dans les logs du serveur**
Lors du démarrage, le serveur affichera les IPs disponibles. Cherchez celle qui correspond à votre réseau WiFi.

**Méthode 2 : Tester depuis le téléphone**
1. Sur votre téléphone, ouvrez un navigateur
2. Testez différentes URLs jusqu'à trouver celle qui fonctionne :
   - `http://172.16.11.55:3007/api` (votre IP WiFi actuelle)
   - `http://192.168.56.1:3007/api` (VirtualBox)
   - `http://192.168.137.1:3007/api` (si c'est le réseau du téléphone)

**Méthode 3 : Utiliser l'IP du réseau partagé**
Si votre téléphone est sur `192.168.137.x`, il y a peut-être un partage de connexion. Dans ce cas :
- Trouvez l'IP de la carte réseau qui gère le partage
- Utilisez cette IP dans `backend_url.txt`

#### Étape 4 : Mettre à jour backend_url.txt

Une fois que vous avez trouvé l'IP qui fonctionne depuis le téléphone, mettez à jour :

```
app/src/main/assets/backend_url.txt
```

Par exemple, si votre téléphone peut accéder à `172.16.11.55:3007` :
```
http://172.16.11.55:3007/
```

#### Étape 5 : Vérifier network_security_config.xml

Assurez-vous que l'IP est autorisée dans le fichier de sécurité réseau :

```xml
<domain includeSubdomains="true">172.16.11.55</domain>
```

#### Étape 6 : Recompiler et réinstaller l'application

```powershell
cd "C:\Users\Lenovo\Desktop\YOSRA YOSRA\DarnaFrontAndroid-main"
.\gradlew clean build
```

Puis réinstallez l'APK sur votre téléphone.

---

## 🔍 Vérifications supplémentaires

### Vérifier que le serveur écoute bien sur toutes les interfaces

Dans PowerShell :
```powershell
netstat -an | findstr 3007
```

Vous devriez voir :
```
TCP    0.0.0.0:3007           0.0.0.0:0              LISTENING
```

### Tester depuis le téléphone

Sur votre téléphone, ouvrez un navigateur et essayez :
```
http://[VOTRE_IP]:3007/api
```

Si vous voyez la documentation Swagger, c'est que ça fonctionne ! ✅

### Vérifier le firewall Windows

Le pare-feu Windows doit autoriser le port 3007 :

1. Ouvrez "Pare-feu Windows Defender"
2. Cliquez sur "Paramètres avancés"
3. Créez une règle entrante pour le port 3007 (TCP)

---

## 📝 Notes importantes

- **IP 192.168.137.x** : C'est souvent l'IP d'un partage de connexion ou hotspot
- **IP 192.168.56.1** : C'est l'IP VirtualBox (pour émulateurs)
- **IP 172.16.11.55** : C'est votre IP WiFi réelle
- **0.0.0.0** : Permet au serveur d'écouter sur toutes les interfaces réseau



