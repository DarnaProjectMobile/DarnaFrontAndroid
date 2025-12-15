package com.sim.darna.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.sim.darna.R

class SoundManager private constructor(context: Context) {
    
    private val soundPool: SoundPool
    private val sounds = mutableMapOf<SoundType, Int>()
    
    enum class SoundType {
        SPIN,      // Son de rotation de la roue
        WIN,       // Son de victoire
        LOSE       // Son de défaite
    }
    
    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()
        
        // Charger les sons
        // Note: Pour utiliser vos propres fichiers audio:
        // 1. Créez le dossier app/src/main/res/raw/ s'il n'existe pas
        // 2. Ajoutez vos fichiers audio (spin_sound.mp3, win_sound.mp3, lose_sound.mp3)
        // 3. Décommentez les lignes ci-dessous et remplacez par vos fichiers
        try {
            // Exemple pour charger depuis res/raw (décommentez quand vous avez les fichiers):
            sounds[SoundType.SPIN] = soundPool.load(context, R.raw.spin_sound, 1)
            sounds[SoundType.WIN] = soundPool.load(context, R.raw.win_sound, 1)
            sounds[SoundType.LOSE] = soundPool.load(context, R.raw.lose_sound, 1)
            
            // Pour l'instant, on utilise des valeurs par défaut (0 = pas de son)
            // Les sons seront joués silencieusement jusqu'à ce que vous ajoutiez vos fichiers
            Log.d("SoundManager", "SoundManager initialisé")
            Log.d("SoundManager", "Pour ajouter des sons: créez app/src/main/res/raw/ et ajoutez vos fichiers audio")
        } catch (e: Exception) {
            Log.e("SoundManager", "Erreur lors du chargement des sons: ${e.message}")
        }
    }
    
    fun playSound(soundType: SoundType) {
        try {
            val soundId = sounds[soundType]
            if (soundId != null && soundId > 0) {
                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                Log.d("SoundManager", "Son joué: $soundType")
            } else {
                // Si aucun son n'est chargé, on peut utiliser des sons système
                // ou simplement ne rien faire
                Log.d("SoundManager", "Aucun son chargé pour: $soundType")
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Erreur lors de la lecture du son: ${e.message}")
        }
    }
    
    fun release() {
        soundPool.release()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: SoundManager? = null
        
        fun getInstance(context: Context): SoundManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SoundManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

