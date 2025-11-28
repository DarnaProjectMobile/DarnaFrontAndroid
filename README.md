# Darna Android App (Frontend)

## 1. Présentation du projet

### 1.1. Contexte
De nombreux étudiants rencontrent des difficultés à trouver une colocation adaptée à leurs besoins, que ce soit en termes de budget, de localisation, ou de profil des colocataires. Les plateformes actuelles manquent souvent de fiabilité ou de fonctionnalités adaptées à la vie étudiante.  

Dans ce contexte, nous proposons la conception d’une application mobile et web dédiée à la recherche de colocation pour étudiants, alliant simplicité, rapidité et fiabilité.

## 2. Problématique

- Comment optimiser le temps et les efforts nécessaires à la recherche d’une colocation ?  
- Comment garantir la fiabilité des annonces et éviter les arnaques immobilières ?  
- Comment attirer des partenaires (agences, résidences universitaires, etc.) pour enrichir les offres de colocation ?

## 3. Objectifs du projet

L’objectif principal est de concevoir une application intuitive et sécurisée permettant aux étudiants :

- De rechercher facilement une colocation selon leurs critères.  
- De poster leurs propres annonces avec des informations complètes et visuelles.  
- De bénéficier d’un environnement fiable, grâce à un système de vérification et d’évaluation des annonces.

## 4. Public cible

L’application s’adresse principalement aux :  

- Étudiants universitaires à la recherche d’un logement en colocation.  
- Nouveaux bacheliers intégrant une université loin de leur domicile.  
- Étudiants étrangers ou en mobilité souhaitant trouver un logement temporaire.

## 5. Description générale de l’application

L’application offrira :

- Un système d’annonces de colocation avec informations détaillées (prix, localisation, photos, description, contact).  
- Une expérience utilisateur fluide et moderne, adaptée aux habitudes numériques des étudiants.  
- Une sécurité renforcée pour la gestion des comptes et la vérification des annonces.

## 6. Besoins fonctionnels

### 6.1. Gestion des annonces

- Création d’une annonce de colocation (titre, description, adresse, prix, nombre de colocataires, etc.).  
- Ajout de photos et de visites virtuelles à 360°.  
- Modification et suppression d’une annonce.  
- Consultation et gestion de ses propres annonces.

### 6.2. Publicité

- Les utilisateurs ou les propriétaires peuvent promouvoir leur logement à travers des publicités payantes ou mises en avant.

### 6.3. Request et communication

- Les étudiants peuvent soumettre une demande s’ils recherchent une colocation existante.  
- Possibilité de contacter directement le propriétaire ou locataire via messagerie interne.  
- Système de notation et d’avis sur les annonces ou utilisateurs.

### 6.4. Gestion du profil utilisateur

- Création d’un compte personnel (étudiant, propriétaire, agence).  
- Modification du profil et des préférences.  
- Consultation de l’historique des recherches et annonces sauvegardées.

## 7. Besoins non fonctionnels

| Catégorie  | Exigence                                                                 |
|------------|--------------------------------------------------------------------------|
| Fiabilité  | Vérification manuelle ou automatisée des annonces pour éviter les arnaques |
| Performance| Les pages et recherches doivent se charger rapidement                    |
| Ergonomie  | Interface simple, intuitive et attractive, adaptée à l’usage mobile     |

## 8. Technologies envisagées

- **Front-end** : Kotlin / Jetpack Compose (Android)  
- **Back-end** : NestJS  
- **Base de données** : MongoDB  

## 9. Configuration du backend (développement)

Pour éviter l'erreur *« Impossible de joindre le serveur »* visible sur l'écran de connexion :

1. Démarrez l'API NestJS (`npm run start`).
2. Relevez l'IP que votre appareil peut joindre :
   - Émulateur Android Studio → `http://10.0.2.2:3007/`
   - Téléphone réel sur le même WiFi → utilisez l'IPv4 locale de votre PC (`ipconfig`) telle que `http://192.168.1.42:3007/`
3. Ouvrez `local.properties` (non versionné) et ajoutez ou modifiez la ligne :  
   `backend.url=http://VOTRE_IP:3007/`
4. Recompilez l'app (`Build > Rebuild Project`). Vous pouvez aussi définir la variable d'environnement `DARNA_BACKEND_URL` avant d'exécuter Gradle.

Sans configuration, l'application utilise automatiquement `http://10.0.2.2:3007/`.

### 9.1. Dépannage des erreurs de connexion

Si vous rencontrez l'erreur *« Impossible de joindre le serveur »*, suivez ces étapes :

#### Étape 1 : Vérifier que le serveur est démarré
```bash
# Dans le dossier du backend NestJS
npm run start
```
Assurez-vous que le serveur écoute sur le port 3007 et affiche un message de démarrage.

#### Étape 2 : Trouver votre IP locale
- **Windows** : Ouvrez PowerShell et exécutez :
  ```powershell
  ipconfig | findstr IPv4
  ```
  Cherchez une adresse de type `192.168.x.x` ou `192.168.0.x` (évitez les adresses `169.254.x.x` qui sont des adresses APIPA non accessibles).

- **Linux/Mac** : Ouvrez Terminal et exécutez :
  ```bash
  ifconfig | grep inet
  ```
  Cherchez une adresse de type `192.168.x.x`.

#### Étape 3 : Vérifier la configuration réseau
1. Ouvrez `local.properties` et vérifiez que `backend.url` pointe vers la bonne IP :
   ```
   backend.url=http://192.168.1.XXX:3007/
   ```
   ⚠️ **Important** : N'utilisez PAS d'adresse `169.254.x.x` (APIPA) car elle n'est pas accessible depuis d'autres appareils.

2. Vérifiez que l'IP est autorisée dans `app/src/main/res/xml/network_security_config.xml` (elle devrait déjà y être si vous utilisez une IP standard).

#### Étape 4 : Vérifier la connexion réseau
- Assurez-vous que votre téléphone et votre ordinateur sont sur le **même réseau WiFi**.
- Désactivez temporairement le VPN si vous en utilisez un.
- Vérifiez que le firewall Windows/Mac ne bloque pas le port 3007.

#### Étape 5 : Tester la connexion
1. Sur votre téléphone, ouvrez un navigateur et essayez d'accéder à : `http://VOTRE_IP:3007/`
2. Si cela fonctionne dans le navigateur mais pas dans l'app, recompilez l'application :
   - Android Studio : `Build > Rebuild Project`
   - Ou via terminal : `./gradlew clean build`

#### Étape 6 : Vérifier les logs
- Ouvrez Logcat dans Android Studio pour voir les erreurs détaillées.
- Les messages d'erreur améliorés dans l'application vous donneront des indications précises sur le problème.

#### Solutions courantes :
- **IP a changé** : Les IP locales peuvent changer si vous vous reconnectez au WiFi. Vérifiez régulièrement avec `ipconfig`.
- **Serveur non démarré** : Vérifiez que le serveur NestJS est bien en cours d'exécution.
- **Firewall** : Ajoutez une exception pour le port 3007 dans votre firewall.
- **Réseau différent** : Assurez-vous que le téléphone et l'ordinateur sont sur le même réseau WiFi (pas de réseau invité isolé).

## 10. Conclusion

Ce projet vise à répondre aux besoins réels des étudiants en matière de logement partagé, en leur offrant un outil fiable et convivial. L’application ambitionne de devenir une référence pour la colocation étudiante grâce à son ergonomie, sa rapidité et sa sécurité.
