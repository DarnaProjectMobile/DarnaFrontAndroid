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





