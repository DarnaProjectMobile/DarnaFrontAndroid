// CODE À AJOUTER DANS ChatViewModel.kt - Dans la fonction setupSocket(), après les autres listeners

            // Gérer les réactions aux messages en temps réel
            socket?.on("reaction_updated") { args ->
                try {
                    if (args.isNotEmpty() && args[0] is JSONObject) {
                        val data = args[0] as JSONObject
                        val messageId = data.getString("messageId")
                        val reactions = data.optJSONObject("reactions")
                        
                        // Convertir les réactions en Map
                        val reactionsMap = mutableMapOf<String, List<String>>()
                        reactions?.keys()?.forEach { emoji ->
                            val userIds = reactions.getJSONArray(emoji)
                            val userIdsList = mutableListOf<String>()
                            for (i in 0 until userIds.length()) {
                                userIdsList.add(userIds.getString(i))
                            }
                            reactionsMap[emoji] = userIdsList
                        }
                        
                        // Mettre à jour le message dans l'état
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
                        Log.d("ChatViewModel", "Réaction mise à jour pour le message $messageId")
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Erreur lors de la réception de reaction_updated", e)
                }
            }
