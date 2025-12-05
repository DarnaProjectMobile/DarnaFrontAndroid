# Guide de test - Nouvelles fonctionnalités de messagerie

## Prérequis
- Backend NestJS démarré sur le port 3009
- Application Android compilée et installée
- Deux comptes utilisateurs (client et colocataire)
- Une visite confirmée entre les deux utilisateurs

---

## Test 1 : Suppression de message texte

### Étapes
1. **Connexion** : Se connecter en tant que client
2. **Ouvrir le chat** : Accéder à une visite confirmée et ouvrir le chat
3. **Envoyer un message** : Envoyer un message texte (ex: "Bonjour, je suis intéressé")
4. **Appui long** : Faire un appui long sur le message envoyé
5. **Menu contextuel** : Vérifier que le menu s'affiche avec les options "Modifier" et "Supprimer"
6. **Supprimer** : Cliquer sur "Supprimer"

### Résultats attendus
- ✅ Le message devient gris
- ✅ Le contenu affiche "Message supprimé" en italique
- ✅ Les indicateurs de statut disparaissent
- ✅ Le destinataire voit aussi "Message supprimé" en temps réel

### Vérification backend
```bash
# Dans les logs du backend, vous devriez voir :
[ChatService] ✅ Message {messageId} supprimé par {userId}
[ChatGateway] Message {messageId} deleted by user {userId}
```

---

## Test 2 : Suppression de message avec image

### Étapes
1. **Envoyer une image** : Envoyer un message avec une ou plusieurs images
2. **Appui long** : Faire un appui long sur le message
3. **Supprimer** : Cliquer sur "Supprimer"

### Résultats attendus
- ✅ Le message devient gris
- ✅ Les images disparaissent
- ✅ Le contenu affiche "Message supprimé"

---

## Test 3 : Modification de message texte

### Étapes
1. **Envoyer un message** : Envoyer un message texte (ex: "Bonjour")
2. **Appui long** : Faire un appui long sur le message
3. **Modifier** : Cliquer sur "Modifier"
4. **Dialog** : Vérifier que le dialog s'ouvre avec le contenu actuel
5. **Éditer** : Modifier le texte (ex: "Bonjour, comment allez-vous ?")
6. **Enregistrer** : Cliquer sur "Enregistrer"

### Résultats attendus
- ✅ Le message est mis à jour avec le nouveau contenu
- ✅ L'indicateur "(modifié)" apparaît à côté du message
- ✅ Le destinataire voit le message modifié en temps réel avec "(modifié)"

### Vérification backend
```bash
[ChatService] ✅ Message {messageId} modifié par {userId}
[ChatGateway] Message {messageId} updated by user {userId}
```

---

## Test 4 : Impossibilité de modifier un message avec image

### Étapes
1. **Envoyer une image** : Envoyer un message avec une image
2. **Appui long** : Faire un appui long sur le message

### Résultats attendus
- ✅ Le menu contextuel affiche uniquement "Supprimer"
- ✅ L'option "Modifier" n'est pas disponible

---

## Test 5 : Accusés de lecture - Envoyé

### Étapes
1. **Connexion client** : Se connecter en tant que client
2. **Envoyer un message** : Envoyer un message texte
3. **Vérifier le statut** : Observer l'indicateur à côté de l'heure

### Résultats attendus
- ✅ Une simple coche grise (✓) apparaît
- ✅ Cela indique que le message est "envoyé"

---

## Test 6 : Accusés de lecture - Reçu

### Étapes
1. **Connexion colocataire** : Se connecter en tant que colocataire
2. **Ouvrir le chat** : Accéder à la même visite
3. **Retour au client** : Revenir sur le compte client

### Résultats attendus
- ✅ Double coche grise (✓✓) apparaît
- ✅ Cela indique que le message est "reçu" (delivered)

### Vérification backend
```bash
[ChatService] ✅ Statut du message {messageId} mis à jour à "delivered" par {userId}
[ChatGateway] Message {messageId} status updated to "delivered" by user {userId}
```

---

## Test 7 : Accusés de lecture - Vu

### Étapes
1. **Connexion colocataire** : Se connecter en tant que colocataire
2. **Ouvrir le chat** : Accéder à la visite et consulter les messages
3. **Retour au client** : Revenir sur le compte client

### Résultats attendus
- ✅ Double coche bleue (✓✓) apparaît
- ✅ Cela indique que le message est "vu" (read)

### Vérification backend
```bash
[ChatService] ✅ Statut du message {messageId} mis à jour à "read" par {userId}
[ChatGateway] Message {messageId} status updated to "read" by user {userId}
```

---

## Test 8 : Synchronisation en temps réel

### Étapes
1. **Deux appareils** : Avoir deux appareils ou émulateurs
2. **Connexion** : Client sur appareil 1, colocataire sur appareil 2
3. **Ouvrir le chat** : Les deux utilisateurs ouvrent le même chat
4. **Actions simultanées** :
   - Client envoie un message
   - Client modifie un message
   - Client supprime un message

### Résultats attendus
- ✅ Le colocataire voit tous les changements en temps réel
- ✅ Pas besoin de rafraîchir l'écran
- ✅ Les indicateurs de statut se mettent à jour automatiquement

---

## Test 9 : Permissions - Suppression

### Étapes
1. **Client envoie un message** : Le client envoie un message
2. **Connexion colocataire** : Se connecter en tant que colocataire
3. **Appui long** : Faire un appui long sur le message du client

### Résultats attendus
- ✅ Le menu contextuel ne s'affiche PAS
- ✅ Le colocataire ne peut pas supprimer le message du client

---

## Test 10 : Permissions - Modification

### Étapes
1. **Client envoie un message** : Le client envoie un message
2. **Connexion colocataire** : Se connecter en tant que colocataire
3. **Appui long** : Faire un appui long sur le message du client

### Résultats attendus
- ✅ Le menu contextuel ne s'affiche PAS
- ✅ Le colocataire ne peut pas modifier le message du client

---

## Test 11 : Validation - Message vide

### Étapes
1. **Envoyer un message** : Envoyer un message texte
2. **Modifier** : Ouvrir le dialog de modification
3. **Effacer le texte** : Supprimer tout le contenu
4. **Enregistrer** : Essayer de cliquer sur "Enregistrer"

### Résultats attendus
- ✅ Le bouton "Enregistrer" est désactivé
- ✅ Impossible d'enregistrer un message vide

---

## Test 12 : Annulation de modification

### Étapes
1. **Envoyer un message** : Envoyer un message texte
2. **Modifier** : Ouvrir le dialog de modification
3. **Éditer** : Modifier le texte
4. **Annuler** : Cliquer sur "Annuler"

### Résultats attendus
- ✅ Le dialog se ferme
- ✅ Le message reste inchangé
- ✅ Pas d'indicateur "(modifié)"

---

## Test 13 : Messages supprimés ne peuvent pas être modifiés

### Étapes
1. **Envoyer un message** : Envoyer un message texte
2. **Supprimer** : Supprimer le message
3. **Appui long** : Faire un appui long sur le message supprimé

### Résultats attendus
- ✅ Le menu contextuel ne s'affiche PAS
- ✅ Les messages supprimés ne peuvent plus être modifiés

---

## Test 14 : Test API REST avec Swagger

### Accès Swagger
```
http://localhost:3009/api
```

### Test DELETE /chat/message/{messageId}
1. **Authentification** : Cliquer sur "Authorize" et entrer le token JWT
2. **Expand** : Ouvrir l'endpoint DELETE /chat/message/{messageId}
3. **Try it out** : Cliquer sur "Try it out"
4. **messageId** : Entrer un ID de message valide
5. **Execute** : Cliquer sur "Execute"

**Résultat attendu** :
```json
{
  "_id": "...",
  "isDeleted": true,
  "content": "Message supprimé",
  "images": [],
  ...
}
```

### Test PATCH /chat/message/{messageId}
1. **Try it out** : Cliquer sur "Try it out"
2. **messageId** : Entrer un ID de message valide
3. **Body** :
```json
{
  "content": "Nouveau contenu du message"
}
```
4. **Execute** : Cliquer sur "Execute"

**Résultat attendu** :
```json
{
  "_id": "...",
  "content": "Nouveau contenu du message",
  "isEdited": true,
  "editedAt": "2025-12-05T...",
  ...
}
```

### Test PATCH /chat/message/{messageId}/status
1. **Try it out** : Cliquer sur "Try it out"
2. **messageId** : Entrer un ID de message valide
3. **Body** :
```json
{
  "status": "read"
}
```
4. **Execute** : Cliquer sur "Execute"

**Résultat attendu** :
```json
{
  "_id": "...",
  "status": "read",
  "read": true,
  "readAt": "2025-12-05T...",
  "deliveredAt": "2025-12-05T...",
  ...
}
```

---

## Test 15 : Vérification MongoDB

### Connexion à MongoDB
```bash
# Si MongoDB est local
mongosh

# Sélectionner la base de données
use darna

# Voir un message
db.messages.findOne()
```

### Vérifier les nouveaux champs
```javascript
{
  _id: ObjectId("..."),
  visiteId: "...",
  senderId: "...",
  receiverId: "...",
  content: "...",
  images: [],
  type: "text",
  read: false,
  readAt: null,
  // Nouveaux champs
  isDeleted: false,
  isEdited: false,
  editedAt: null,
  status: "sent",
  deliveredAt: null,
  createdAt: ISODate("..."),
  updatedAt: ISODate("...")
}
```

---

## Checklist finale

### Frontend
- [ ] Appui long fonctionne sur les messages
- [ ] Menu contextuel s'affiche correctement
- [ ] Suppression de message texte
- [ ] Suppression de message avec image
- [ ] Modification de message texte
- [ ] Dialog de modification fonctionne
- [ ] Indicateur "(modifié)" s'affiche
- [ ] Messages supprimés affichent "Message supprimé"
- [ ] Indicateurs de statut (✓, ✓✓, ✓✓ bleu)
- [ ] Synchronisation temps réel fonctionne
- [ ] Permissions respectées (seul l'expéditeur peut modifier/supprimer)

### Backend
- [ ] Endpoint DELETE /chat/message/{messageId} fonctionne
- [ ] Endpoint PATCH /chat/message/{messageId} fonctionne
- [ ] Endpoint PATCH /chat/message/{messageId}/status fonctionne
- [ ] Événement WebSocket delete_message fonctionne
- [ ] Événement WebSocket update_message fonctionne
- [ ] Événement WebSocket update_message_status fonctionne
- [ ] Permissions vérifiées côté serveur
- [ ] Logs affichent les actions correctement
- [ ] Données persistées dans MongoDB

---

## Dépannage

### Le menu contextuel ne s'affiche pas
- Vérifier que l'import `ExperimentalFoundationApi` est présent
- Vérifier que `combinedClickable` est importé
- Vérifier que le message appartient bien à l'utilisateur actuel

### Les événements WebSocket ne fonctionnent pas
- Vérifier que le backend est démarré
- Vérifier les logs du backend pour les erreurs de connexion
- Vérifier que le token JWT est valide
- Vérifier que l'utilisateur a bien rejoint la room de la visite

### Les statuts ne se mettent pas à jour
- Vérifier que `updateMessageStatus` est appelé
- Vérifier les logs du backend
- Vérifier que le destinataire est bien l'utilisateur actuel

### Erreur 403 Forbidden
- Vérifier que l'utilisateur est bien l'expéditeur (pour suppression/modification)
- Vérifier que l'utilisateur est bien le destinataire (pour mise à jour de statut)
- Vérifier le token JWT

---

## Conclusion

Si tous les tests passent, les fonctionnalités sont correctement implémentées ! 🎉

Pour toute question ou problème, consultez les logs du backend et du frontend pour identifier la source du problème.
