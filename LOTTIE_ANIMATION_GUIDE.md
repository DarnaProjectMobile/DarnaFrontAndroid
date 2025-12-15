# Guide d'ajout d'animation Lottie pour la victoire

## Étapes pour ajouter une animation Lottie depuis LottieFiles

### 1. Télécharger une animation depuis LottieFiles

1. Allez sur [LottieFiles.com](https://lottiefiles.com/)
2. Recherchez une animation de victoire/succès (ex: "celebration", "success", "win", "confetti")
3. Cliquez sur l'animation que vous aimez
4. Cliquez sur le bouton **"Download"**
5. Sélectionnez le format **"Lottie JSON"**
6. Téléchargez le fichier JSON

### 2. Ajouter le fichier dans votre projet

1. Créez le dossier `app/src/main/assets/` s'il n'existe pas
2. Renommez le fichier téléchargé en `win_animation.json`
3. Placez-le dans le dossier `app/src/main/assets/`

**Structure finale :**
```
app/src/main/assets/
  ├── empty.json (déjà présent)
  └── win_animation.json (nouveau fichier)
```

### 3. Vérifier le code

Le code est déjà configuré pour charger `win_animation.json` depuis le dossier `assets/`. Si vous voulez utiliser un autre nom de fichier, modifiez la ligne dans `PubliciteDetailScreen.kt` :

```kotlin
WinAnimationLottie(
    assetFileName = "votre_fichier.json", // Changez ici
    ...
)
```

### 4. Recommandations pour choisir une animation

**Caractéristiques idéales :**
- Durée : 2-5 secondes (pas trop long)
- Taille : Optimisée pour mobile (pas trop complexe)
- Thème : Célébration, succès, confetti, étoiles, trophée
- Couleurs : Vives et joyeuses (vert, or, bleu)

**Exemples de recherches sur LottieFiles :**
- "celebration"
- "success animation"
- "win celebration"
- "confetti"
- "trophy"
- "victory"

### 5. Tester l'animation

Une fois le fichier ajouté :
1. Compilez et lancez l'application
2. Jouez à la roue de la fortune
3. Gagnez un prix (autre que "rien")
4. L'animation devrait s'afficher automatiquement dans un dialog

### 6. Personnalisation (optionnel)

Si vous voulez modifier le comportement de l'animation, vous pouvez éditer `WinAnimationLottie.kt` :

- **Taille** : Modifiez `Modifier.size(300.dp)`
- **Nombre de répétitions** : Modifiez `iterations = 1` (actuellement joue une seule fois)
- **Position** : Modifiez l'alignement dans le `Box`

### 7. Ressources gratuites

**Sites recommandés :**
- [LottieFiles.com](https://lottiefiles.com/) - La plus grande collection
- [Lottie Animations](https://lottieanimations.com/) - Animations gratuites
- [Icons8 Lottie](https://icons8.com/lottie-animations) - Animations premium et gratuites

**Recherches populaires pour "victoire" :**
- "Success Checkmark"
- "Celebration Confetti"
- "Trophy Win"
- "Star Celebration"
- "Fireworks"

### Notes importantes

- Le fichier doit être au format JSON (pas GIF, pas MP4)
- Le fichier doit être dans `assets/` (pas dans `res/raw/`)
- L'animation se ferme automatiquement après avoir joué une fois
- L'utilisateur peut aussi fermer l'animation en cliquant en dehors ou en appuyant sur retour

