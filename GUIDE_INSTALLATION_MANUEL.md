# 🛠️ Guide d'installation COMPLET - Étape par étape

## ⚠️ IMPORTANT
Les indicateurs de statut et les réactions ne fonctionneront PAS tant que vous n'aurez pas ajouté le code manuellement.

---

## 📱 PARTIE 1 : Frontend (Android) - À FAIRE MAINTENANT

### Étape 1 : Ouvrir ChatViewModel.kt

**Fichier** : `app/src/main/java/com/sim/darna/chat/ChatViewModel.kt`

#### Action 1.1 : Ajouter la fonction toggleReaction

1. **Allez à la fin du fichier** (avant la dernière `}`)
2. **Copiez ce code** :

```kotlin
    /**
     * Ajouter ou retirer une réaction à un message
     */
    fun toggleReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            try {
                val updatedMessage = repository.toggleReaction(messageId, emoji)
                
                _state.update { 
                    it.copy(
                        messages = it.messages.map { msg ->
                            if (msg.id == messageId) {
                                updatedMessage
                            } else msg
                        }
                    )
                }
                
                socket?.emit("toggle_reaction", JSONObject().apply {
                    put("messageId", messageId)
                    put("emoji", emoji)
                })
                
            } catch (error: Exception) {
                Log.e("ChatViewModel", "Erreur réaction", error)
            }
        }
    }
```

3. **Collez-le** juste avant la dernière `}` de la classe

#### Action 1.2 : Ajouter le gestionnaire WebSocket pour les réactions

1. **Dans la même fonction `setupSocket()`**, trouvez la ligne avec `socket?.on("error")`
2. **Juste AVANT cette ligne**, ajoutez :

```kotlin
            socket?.on("reaction_updated") { args ->
                try {
                    if (args.isNotEmpty() && args[0] is JSONObject) {
                        val data = args[0] as JSONObject
                        val messageId = data.getString("messageId")
                        val reactions = data.optJSONObject("reactions")
                        
                        val reactionsMap = mutableMapOf<String, List<String>>()
                        reactions?.keys()?.forEach { emoji ->
                            val userIds = reactions.getJSONArray(emoji)
                            val userIdsList = mutableListOf<String>()
                            for (i in 0 until userIds.length()) {
                                userIdsList.add(userIds.getString(i))
                            }
                            reactionsMap[emoji] = userIdsList
                        }
                        
                        viewModelScope.launch {
                            _state.update { 
                                it.copy(
                                    messages = it.messages.map { msg ->
                                        if (msg.id == messageId) {
                                            msg.copy(reactions = reactionsMap)
                                        } else msg
                                    }
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Erreur reaction_updated", e)
                }
            }
```

---

### Étape 2 : Ouvrir ChatScreen.kt

**Fichier** : `app/src/main/java/com/sim/darna/screens/ChatScreen.kt`

#### Action 2.1 : Ajouter les imports

En haut du fichier, après les autres imports, ajoutez :

```kotlin
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
```

#### Action 2.2 : Ajouter les composants de réactions

À la fin du fichier (après `MessageEditDialog`), ajoutez :

```kotlin
@Composable
private fun MessageReactions(
    reactions: Map<String, List<String>>?,
    currentUserId: String,
    onReactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (reactions.isNullOrEmpty()) return
    
    FlowRow(
        modifier = modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        reactions.forEach { (emoji, userIds) ->
            val hasReacted = userIds.contains(currentUserId)
            val count = userIds.size
            
            Surface(
                onClick = { onReactionClick(emoji) },
                shape = RoundedCornerShape(12.dp),
                color = if (hasReacted) AppColors.primary.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
                border = if (hasReacted) BorderStroke(1.dp, AppColors.primary) else null,
                modifier = Modifier.height(28.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = emoji, fontSize = 14.sp)
                    if (count > 1) {
                        Text(
                            text = count.toString(),
                            fontSize = 11.sp,
                            color = if (hasReacted) AppColors.primary else Color.Gray,
                            fontWeight = if (hasReacted) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReactionPicker(
    onReactionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val commonEmojis = listOf(
        "👍", "❤️", "😂", "😮", "😢", "🙏",
        "🎉", "🔥", "👏", "✨", "💯", "🚀"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une réaction", fontWeight = FontWeight.Bold) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(commonEmojis) { emoji ->
                    Surface(
                        onClick = {
                            onReactionSelected(emoji)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Gray.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp).padding(4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
```

#### Action 2.3 : Modifier MessageBubble pour afficher les réactions

Dans le composant `MessageBubble`, trouvez la section où l'heure est affichée (cherchez `formatMessageTime`).

**Juste APRÈS** la Row qui contient l'heure et les indicateurs de statut, ajoutez :

```kotlin
                // Afficher les réactions
                MessageReactions(
                    reactions = message.reactions,
                    currentUserId = currentUserId,
                    onReactionClick = { emoji ->
                        onReactionClick?.invoke(message.id ?: "", emoji)
                    }
                )
```

#### Action 2.4 : Ajouter le paramètre onReactionClick à MessageBubble

Dans la signature de `MessageBubble`, ajoutez :

```kotlin
private fun MessageBubble(
    message: MessageResponse,
    isCurrentUser: Boolean,
    baseUrl: String,
    modifier: Modifier = Modifier,
    onEditMessage: ((String, String) -> Unit)? = null,
    onDeleteMessage: ((String) -> Unit)? = null,
    onReactionClick: ((String, String) -> Unit)? = null  // ← AJOUTER CETTE LIGNE
)
```

#### Action 2.5 : Modifier l'appel de MessageBubble dans ChatScreen

Dans la fonction `ChatScreen`, trouvez où `MessageBubble` est appelé dans la `LazyColumn`.

Modifiez l'appel pour ajouter :

```kotlin
MessageBubble(
    message = message,
    isCurrentUser = message.senderId == currentUserId,
    baseUrl = viewModel.baseUrl,
    modifier = Modifier.fillMaxWidth(),
    onEditMessage = { messageId, content ->
        messageToEdit = Pair(messageId, content)
        editDialogText = content
    },
    onDeleteMessage = { messageId ->
        viewModel.deleteMessage(messageId)
    },
    onReactionClick = { messageId, emoji ->  // ← AJOUTER CETTE LIGNE
        viewModel.toggleReaction(messageId, emoji)
    }
)
```

---

## 🔧 PARTIE 2 : Backend (NestJS) - À FAIRE MAINTENANT

### Étape 1 : Ouvrir chat.service.ts

**Fichier** : `src/chat/chat.service.ts`

À la fin de la classe (avant la dernière `}`), ajoutez :

```typescript
  async toggleReaction(messageId: string, emoji: string, userId: string): Promise<any> {
    const message = await this.messageModel.findById(messageId).exec();
    
    if (!message) {
      throw new NotFoundException('Message non trouvé');
    }

    if (!message.reactions) {
      message.reactions = {};
    }

    const currentReactions = message.reactions[emoji] || [];
    const userIndex = currentReactions.indexOf(userId);

    if (userIndex > -1) {
      currentReactions.splice(userIndex, 1);
      if (currentReactions.length === 0) {
        delete message.reactions[emoji];
      } else {
        message.reactions[emoji] = currentReactions;
      }
    } else {
      message.reactions[emoji] = [...currentReactions, userId];
    }

    message.markModified('reactions');
    await message.save();

    console.log(`[ChatService] ✅ Réaction ${emoji} toggleée pour ${messageId} par ${userId}`);
    return this.enrichMessage(message);
  }
```

---

### Étape 2 : Ouvrir chat.controller.ts

**Fichier** : `src/chat/chat.controller.ts`

Avant la dernière `}` de la classe, ajoutez :

```typescript
  @Post('message/:messageId/reaction')
  @ApiBearerAuth('access-token')
  @ApiOperation({ summary: 'Ajouter ou retirer une réaction' })
  @ApiParam({ name: 'messageId', description: 'ID du message' })
  async toggleReaction(
    @Param('messageId') messageId: string,
    @Body() toggleReactionDto: { emoji: string },
    @CurrentUser() user: any,
  ) {
    return this.chatService.toggleReaction(messageId, toggleReactionDto.emoji, user.userId);
  }
```

---

### Étape 3 : Ouvrir chat.gateway.ts

**Fichier** : `src/chat/chat.gateway.ts`

Avant la dernière `}` de la classe, ajoutez :

```typescript
  @SubscribeMessage('toggle_reaction')
  async handleToggleReaction(
    @MessageBody() data: { messageId: string; emoji: string },
    @ConnectedSocket() client: Socket,
  ) {
    try {
      const userId = this.connectedUsers.get(client.id);
      if (!userId) {
        client.emit('error', { message: 'Non autorisé' });
        return;
      }

      const updatedMessage = await this.chatService.toggleReaction(data.messageId, data.emoji, userId);

      this.server.to(`visite:${updatedMessage.visiteId}`).emit('reaction_updated', {
        messageId: data.messageId,
        reactions: updatedMessage.reactions,
      });

      this.server.to(`user:${updatedMessage.receiverId}`).emit('reaction_updated', {
        messageId: data.messageId,
        reactions: updatedMessage.reactions,
      });

      this.server.to(`user:${updatedMessage.senderId}`).emit('reaction_updated', {
        messageId: data.messageId,
        reactions: updatedMessage.reactions,
      });

      console.log(`[ChatGateway] Reaction ${data.emoji} toggled for ${data.messageId}`);
      return updatedMessage;
    } catch (error: any) {
      console.error('[ChatGateway] Error toggling reaction:', error);
      client.emit('error', { message: error.message });
    }
  }
```

---

## 🚀 PARTIE 3 : Compiler et tester

### Étape 1 : Redémarrer le backend

```bash
cd "C:\Users\Lenovo\Desktop\YOSRA YOSRA\DarnaBackendNest"
# Arrêtez le serveur (Ctrl+C)
npm run start:dev
```

### Étape 2 : Compiler l'application Android

```bash
cd "c:\Users\Lenovo\Desktop\YOSRA YOSRA\DarnaFrontAndroid-main"
.\gradlew clean
.\gradlew build
```

### Étape 3 : Tester

1. **Ouvrez l'application**
2. **Envoyez un message**
3. **Vous devriez voir** :
   - ✓ (gris) à côté de l'heure
   - Puis ✓✓ (gris) après quelques secondes
   - Puis ✓✓ (bleu) quand l'autre utilisateur ouvre le chat

4. **Pour les réactions** :
   - Appuyez longuement sur un message
   - Un menu devrait apparaître
   - Ajoutez une réaction 👍

---

## ✅ Checklist

- [ ] Code ajouté dans `ChatViewModel.kt`
- [ ] Code ajouté dans `ChatScreen.kt`
- [ ] Code ajouté dans `chat.service.ts`
- [ ] Code ajouté dans `chat.controller.ts`
- [ ] Code ajouté dans `chat.gateway.ts`
- [ ] Backend redémarré
- [ ] Application recompilée
- [ ] Test effectué

---

**Une fois tout ce code ajouté, TOUT fonctionnera !** 🎉
