# Guide d'ajout des effets sonores

## Structure des fichiers

Pour ajouter des effets sonores à la roue de la fortune, suivez ces étapes :

### 1. Créer le dossier raw

Créez le dossier suivant s'il n'existe pas :
```
app/src/main/res/raw/
```

### 2. Ajouter vos fichiers audio

Ajoutez vos fichiers audio dans le dossier `raw/` avec les noms suivants :
- `spin_sound.mp3` (ou .ogg, .wav) - Son de rotation de la roue
- `win_sound.mp3` - Son de victoire
- `lose_sound.mp3` - Son de défaite

**Formats supportés :** MP3, OGG, WAV

**Recommandations :**
- Durée : 1-3 secondes pour les effets sonores
- Format : OGG Vorbis recommandé pour Android (meilleure compression)
- Volume : Normalisé pour éviter les différences de volume

### 3. Activer les sons dans le code

Une fois les fichiers ajoutés, décommentez les lignes dans `SoundManager.kt` :

```kotlin
sounds[SoundType.SPIN] = soundPool.load(context, R.raw.spin_sound, 1)
sounds[SoundType.WIN] = soundPool.load(context, R.raw.win_sound, 1)
sounds[SoundType.LOSE] = soundPool.load(context, R.raw.lose_sound, 1)
```

### 4. Tester

Les sons seront automatiquement joués :
- **Spin** : Quand l'utilisateur clique sur "Jouer" et que la roue commence à tourner
- **Win** : Quand l'utilisateur gagne (gain autre que "rien")
- **Lose** : Quand l'utilisateur perd (gain = "rien")

## Ressources gratuites

Vous pouvez trouver des effets sonores gratuits sur :
- [Freesound.org](https://freesound.org/)
- [Zapsplat](https://www.zapsplat.com/)
- [OpenGameArt](https://opengameart.org/)

Recherchez des termes comme :
- "roulette spin", "wheel spin" pour le son de rotation
- "success", "victory", "win" pour le son de victoire
- "fail", "lose", "error" pour le son de défaite

## Notes

- Les sons sont gérés par `SoundManager` qui utilise `SoundPool` pour une lecture rapide
- Les sons sont chargés une seule fois au démarrage de l'application
- Si aucun fichier audio n'est trouvé, l'application fonctionnera normalement sans son

