# Plan d'amélioration de la messagerie Darna

## Objectif
Améliorer la messagerie instantanée entre client et colocataire en ajoutant :
1. Suppression de messages et photos
2. Modification de messages texte
3. Système d'accusés de lecture (envoyé, reçu, vu)

## Architecture des modifications

### 1. Modèle de données (MessageResponse)
**Fichier**: `ChatApi.kt`

Ajouts nécessaires :
- `isDeleted: Boolean?` - Indique si le message est supprimé
- `isEdited: Boolean?` - Indique si le message a été modifié
- `editedAt: String?` - Date de dernière modification
- `status: String?` - Statut du message ("sent", "delivered", "read")
- `deliveredAt: String?` - Date de réception
- `readAt: String?` - Date de lecture (déjà existant)

### 2. API REST (ChatApi.kt)
**Nouveaux endpoints** :
- `DELETE chat/message/{messageId}` - Supprimer un message
- `PATCH chat/message/{messageId}` - Modifier un message
- `PATCH chat/message/{messageId}/status` - Mettre à jour le statut

### 3. Repository (ChatRepository.kt)
**Nouvelles méthodes** :
- `deleteMessage(messageId: String)`
- `updateMessage(messageId: String, newContent: String)`
- `updateMessageStatus(messageId: String, status: String)`

### 4. ViewModel (ChatViewModel.kt)
**Nouvelles fonctions** :
- `deleteMessage(messageId: String)` - Supprimer un message
- `editMessage(messageId: String, newContent: String)` - Modifier un message
- `updateMessageStatus(messageId: String, status: String)` - Mettre à jour le statut

**Événements WebSocket à gérer** :
- `message_deleted` - Quand un message est supprimé
- `message_updated` - Quand un message est modifié
- `message_status_changed` - Quand le statut change

### 5. UI (ChatScreen.kt)
**Modifications de MessageBubble** :
- Ajouter un menu contextuel (appui long) avec options :
  - "Modifier" (uniquement pour messages texte de l'utilisateur)
  - "Supprimer" (pour tous les messages de l'utilisateur)
- Afficher "(modifié)" pour les messages édités
- Afficher "Message supprimé" pour les messages supprimés
- Afficher les indicateurs de statut :
  - ✓ (envoyé)
  - ✓✓ (reçu)
  - ✓✓ en bleu (vu)

**Nouveau composant** :
- `MessageEditDialog` - Dialog pour modifier un message

## Étapes d'implémentation

### Étape 1 : Mise à jour du modèle de données
1. Modifier `MessageResponse` dans `ChatApi.kt`
2. Ajouter les nouveaux champs

### Étape 2 : Ajout des endpoints API
1. Ajouter les nouveaux endpoints dans `ChatApi.kt`
2. Créer les data classes de requête/réponse

### Étape 3 : Mise à jour du Repository
1. Implémenter les nouvelles méthodes dans `ChatRepository.kt`

### Étape 4 : Mise à jour du ViewModel
1. Ajouter les nouvelles fonctions dans `ChatViewModel.kt`
2. Gérer les nouveaux événements WebSocket
3. Mettre à jour l'état UI en conséquence

### Étape 5 : Mise à jour de l'UI
1. Ajouter le menu contextuel dans `MessageBubble`
2. Créer le dialog de modification
3. Afficher les indicateurs de statut
4. Gérer l'affichage des messages supprimés/modifiés

### Étape 6 : Tests et validation
1. Tester la suppression de messages
2. Tester la modification de messages
3. Tester les accusés de lecture
4. Vérifier la synchronisation temps réel via WebSocket

## Notes importantes
- Les messages supprimés ne sont pas effacés de la base de données, juste marqués comme supprimés
- Seuls les messages texte peuvent être modifiés (pas les images)
- Les statuts sont mis à jour automatiquement via WebSocket
- L'utilisateur ne peut modifier/supprimer que ses propres messages
