// CODE À AJOUTER DANS ChatScreen.kt - Composant pour afficher les réactions

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
                    Text(
                        text = emoji,
                        fontSize = 14.sp
                    )
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
        title = {
            Text(
                text = "Ajouter une réaction",
                fontWeight = FontWeight.Bold
            )
        },
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
                        modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 24.sp
                            )
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
