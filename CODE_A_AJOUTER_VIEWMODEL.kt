// CODE À AJOUTER À LA FIN DE ChatViewModel.kt (avant la dernière accolade)

    /**
     * Ajouter ou retirer une réaction à un message
     * Si l'utilisateur a déjà réagi avec cet emoji, la réaction est retirée
     */
    fun toggleReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            try {
                // Appeler l'API pour toggle la réaction
                val updatedMessage = repository.toggleReaction(messageId, emoji)
                
                // Mettre à jour l'état local
                _state.update { 
                    it.copy(
                        messages = it.messages.map { msg ->
                            if (msg.id == messageId) {
                                updatedMessage
                            } else msg
                        }
                    )
                }
                
                // Émettre l'événement via Socket.IO pour notifier les autres utilisateurs
                socket?.emit("toggle_reaction", JSONObject().apply {
                    put("messageId", messageId)
                    put("emoji", emoji)
                })
                
            } catch (error: Exception) {
                Log.e("ChatViewModel", "Erreur lors de l'ajout de la réaction", error)
                _state.update { 
                    it.copy(error = error.message ?: "Erreur lors de l'ajout de la réaction")
                }
            }
        }
    }
