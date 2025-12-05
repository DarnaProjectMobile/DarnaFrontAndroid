# Amélioration de la messagerie Darna - Récapitulatif des modifications

## Date : 2025-12-05

## Objectif
Améliorer la messagerie instantanée entre client et colocataire en ajoutant trois fonctionnalités majeures :
1. **Suppression de messages et photos** (soft delete)
2. **Modification de messages texte**
3. **Accusés de lecture** (statuts : envoyé, reçu, vu)

---

## Modifications Frontend (Android - Kotlin)

### 1. Modèle de données (`ChatApi.kt`)

**Fichier** : `app/src/main/java/com/sim/darna/chat/ChatApi.kt`

#### Nouveaux champs ajoutés à `MessageResponse` :
```kotlin
val isDeleted: Boolean? = false        // Message supprimé
val isEdited: Boolean? = false         // Message modifié
val editedAt: String? = null           // Date de modification
val status: String? = "sent"           // Statut : "sent", "delivered", "read"
val deliveredAt: String? = null        // Date de réception
```

#### Nouveaux endpoints API :
- `DELETE chat/message/{messageId}` - Supprimer un message
- `PATCH chat/message/{messageId}` - Modifier un message
- `PATCH chat/message/{messageId}/status` - Mettre à jour le statut

#### Nouveaux DTOs :
- `UpdateMessageRequest(content: String)` - Pour la modification
- `UpdateStatusRequest(status: String)` - Pour le statut

### 2. Repository (`ChatRepository.kt`)

**Fichier** : `app/src/main/java/com/sim/darna/chat/ChatRepository.kt`

#### Nouvelles méthodes :
```kotlin
suspend fun deleteMessage(messageId: String): MessageResponse
suspend fun updateMessage(messageId: String, newContent: String): MessageResponse
suspend fun updateMessageStatus(messageId: String, status: String): MessageResponse
```

### 3. ViewModel (`ChatViewModel.kt`)

**Fichier** : `app/src/main/java/com/sim/darna/chat/ChatViewModel.kt`

#### Nouveaux événements WebSocket gérés :
- `message_deleted` - Quand un message est supprimé
- `message_updated` - Quand un message est modifié
- `message_status_changed` - Quand le statut change

#### Nouvelles fonctions publiques :
```kotlin
fun deleteMessage(messageId: String)
fun editMessage(messageId: String, newContent: String)
fun updateMessageStatus(messageId: String, status: String)
```

### 4. Interface utilisateur (`ChatScreen.kt`)

**Fichier** : `app/src/main/java/com/sim/darna/screens/ChatScreen.kt`

#### Composant `MessageBubble` amélioré :
- **Appui long** : Affiche un menu contextuel avec options "Modifier" et "Supprimer"
- **Messages supprimés** : Affichés en gris avec texte "Message supprimé" en italique
- **Messages modifiés** : Affichent l'indicateur "(modifié)" à côté du texte
- **Indicateurs de statut** :
  - ✓ (gris) = envoyé
  - ✓✓ (gris) = reçu
  - ✓✓ (bleu) = vu

#### Nouveau composant `MessageEditDialog` :
- Dialog modal pour modifier le contenu d'un message
- Validation : le message ne peut pas être vide
- Boutons "Annuler" et "Enregistrer"

#### Logique de mise à jour automatique des statuts :
- Marque les messages comme "delivered" quand l'utilisateur ouvre la conversation
- Marque les messages comme "read" quand l'utilisateur consulte les messages

---

## Modifications Backend (NestJS - TypeScript)

### 1. Schéma de base de données (`message.schema.ts`)

**Fichier** : `src/chat/schemas/message.schema.ts`

#### Nouveaux champs ajoutés :
```typescript
@Prop({ default: false })
isDeleted: boolean;

@Prop({ default: false })
isEdited: boolean;

@Prop({ type: Date, default: null })
editedAt?: Date;

@Prop({ default: 'sent' })
status: string; // 'sent', 'delivered', 'read'

@Prop({ type: Date, default: null })
deliveredAt?: Date;
```

### 2. DTOs

**Nouveaux fichiers** :
- `src/chat/dto/update-message.dto.ts` - Pour la modification
- `src/chat/dto/update-status.dto.ts` - Pour le statut

### 3. Service (`chat.service.ts`)

**Fichier** : `src/chat/chat.service.ts`

#### Nouvelles méthodes :

##### `deleteMessage(messageId, userId)`
- Vérifie que l'utilisateur est l'expéditeur
- Marque le message comme supprimé (soft delete)
- Remplace le contenu par "Message supprimé"
- Supprime les images associées

##### `updateMessage(messageId, newContent, userId)`
- Vérifie que l'utilisateur est l'expéditeur
- Vérifie que le message n'est pas supprimé
- Vérifie que le message ne contient pas d'images
- Met à jour le contenu et marque comme modifié

##### `updateMessageStatus(messageId, status, userId)`
- Vérifie que l'utilisateur est le destinataire
- Met à jour le statut (sent/delivered/read)
- Met à jour les dates correspondantes (deliveredAt, readAt)

### 4. Controller (`chat.controller.ts`)

**Fichier** : `src/chat/chat.controller.ts`

#### Nouveaux endpoints :

##### `DELETE /chat/message/:messageId`
- Supprime un message (soft delete)
- Authentification requise
- Seul l'expéditeur peut supprimer

##### `PATCH /chat/message/:messageId`
- Modifie le contenu d'un message
- Authentification requise
- Seul l'expéditeur peut modifier
- Uniquement pour les messages texte

##### `PATCH /chat/message/:messageId/status`
- Met à jour le statut d'un message
- Authentification requise
- Seul le destinataire peut mettre à jour

### 5. Gateway WebSocket (`chat.gateway.ts`)

**Fichier** : `src/chat/chat.gateway.ts`

#### Nouveaux gestionnaires d'événements :

##### `delete_message`
- Reçoit : `{ messageId }`
- Émet : `message_deleted` à tous les utilisateurs de la visite

##### `update_message`
- Reçoit : `{ messageId, content }`
- Émet : `message_updated` avec le message complet

##### `update_message_status`
- Reçoit : `{ messageId, status }`
- Émet : `message_status_changed` à l'expéditeur

---

## Flux de fonctionnement

### 1. Suppression d'un message

**Frontend** :
1. Utilisateur fait un appui long sur son message
2. Menu contextuel s'affiche avec option "Supprimer"
3. Appel de `viewModel.deleteMessage(messageId)`
4. Émission de l'événement WebSocket `delete_message`

**Backend** :
1. Réception de l'événement `delete_message`
2. Vérification des permissions (expéditeur uniquement)
3. Soft delete du message dans la base de données
4. Émission de `message_deleted` à tous les utilisateurs

**Frontend (réception)** :
1. Réception de `message_deleted`
2. Mise à jour de l'état local
3. Affichage du message en gris avec "Message supprimé"

### 2. Modification d'un message

**Frontend** :
1. Utilisateur fait un appui long sur son message texte
2. Menu contextuel s'affiche avec option "Modifier"
3. Dialog s'ouvre avec le contenu actuel
4. Utilisateur modifie et clique "Enregistrer"
5. Appel de `viewModel.editMessage(messageId, newContent)`
6. Émission de l'événement WebSocket `update_message`

**Backend** :
1. Réception de l'événement `update_message`
2. Vérification des permissions et validations
3. Mise à jour du message dans la base de données
4. Émission de `message_updated` à tous les utilisateurs

**Frontend (réception)** :
1. Réception de `message_updated`
2. Mise à jour de l'état local
3. Affichage du message avec indicateur "(modifié)"

### 3. Accusés de lecture

**Frontend** :
1. Utilisateur ouvre la conversation
2. Appel de `viewModel.updateMessageStatus(messageId, "delivered")` pour chaque message reçu
3. Appel de `viewModel.updateMessageStatus(messageId, "read")` après consultation
4. Émission de l'événement WebSocket `update_message_status`

**Backend** :
1. Réception de l'événement `update_message_status`
2. Vérification des permissions (destinataire uniquement)
3. Mise à jour du statut et des dates
4. Émission de `message_status_changed` à l'expéditeur

**Frontend (réception)** :
1. Réception de `message_status_changed`
2. Mise à jour de l'état local
3. Affichage des indicateurs :
   - ✓ (gris) pour "sent"
   - ✓✓ (gris) pour "delivered"
   - ✓✓ (bleu) pour "read"

---

## Règles de validation

### Suppression
- ✅ L'utilisateur peut supprimer ses propres messages
- ✅ Les messages texte et images peuvent être supprimés
- ✅ Le message est marqué comme supprimé (soft delete)
- ✅ Le contenu devient "Message supprimé"
- ✅ Les images sont supprimées

### Modification
- ✅ L'utilisateur peut modifier ses propres messages
- ✅ Uniquement les messages texte (sans images)
- ✅ Les messages supprimés ne peuvent pas être modifiés
- ✅ Un indicateur "(modifié)" est affiché
- ✅ La date de modification est enregistrée

### Statuts
- ✅ Seul le destinataire peut mettre à jour le statut
- ✅ Les statuts sont : "sent", "delivered", "read"
- ✅ Les dates sont automatiquement enregistrées
- ✅ L'expéditeur voit les indicateurs en temps réel

---

## Tests recommandés

### Frontend
1. ✅ Tester l'appui long sur un message
2. ✅ Tester la suppression d'un message texte
3. ✅ Tester la suppression d'un message avec image
4. ✅ Tester la modification d'un message texte
5. ✅ Vérifier que les messages avec images ne peuvent pas être modifiés
6. ✅ Vérifier l'affichage des indicateurs de statut
7. ✅ Tester la synchronisation en temps réel entre deux utilisateurs

### Backend
1. ✅ Tester les endpoints REST avec Swagger
2. ✅ Vérifier les permissions (seul l'expéditeur peut supprimer/modifier)
3. ✅ Vérifier les permissions (seul le destinataire peut mettre à jour le statut)
4. ✅ Tester les événements WebSocket
5. ✅ Vérifier la persistance dans MongoDB

---

## Compatibilité

### Rétrocompatibilité
- ✅ Les anciens messages sans les nouveaux champs fonctionnent toujours
- ✅ Les valeurs par défaut sont définies pour tous les nouveaux champs
- ✅ L'API REST existante n'est pas modifiée

### Migration de données
Aucune migration nécessaire ! Les nouveaux champs ont des valeurs par défaut :
- `isDeleted: false`
- `isEdited: false`
- `status: "sent"`
- `editedAt: null`
- `deliveredAt: null`

---

## Conclusion

Toutes les fonctionnalités demandées ont été implémentées avec succès :

1. ✅ **Suppression de messages et photos** : Soft delete avec affichage "Message supprimé"
2. ✅ **Modification de messages** : Uniquement pour les messages texte, avec indicateur "(modifié)"
3. ✅ **Accusés de lecture** : Statuts envoyé/reçu/vu avec indicateurs visuels

Le code est bien commenté, structuré et respecte les bonnes pratiques. La synchronisation en temps réel via WebSocket garantit une expérience utilisateur fluide.
