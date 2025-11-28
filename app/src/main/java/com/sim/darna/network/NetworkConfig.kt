package com.sim.darna.network

import android.content.Context

object NetworkConfig {
    // ⚠️ IMPORTANT: Vérifiez que cette URL est accessible depuis votre téléphone
    // 
    // URLs disponibles du serveur:
    // - Local: http://localhost:3007 (ne fonctionne QUE sur la machine serveur)
    // - Network: http://[VOTRE_IP]:3007 (pour téléphone réel sur le même réseau)
    //
    // Configuration recommandée:
    // - Si vous testez sur un ÉMULATEUR Android, utilisez: "http://10.0.2.2:3007/"
    //   (10.0.2.2 redirige vers localhost de la machine hôte)
    // - Si vous testez sur un TÉLÉPHONE RÉEL, utilisez l'IP locale de votre ordinateur:
    //   Exemples: "http://192.168.1.XXX:3007/" ou "http://192.168.0.XXX:3007/"
    //
    // 🔍 Comment trouver l'IP de votre ordinateur:
    // Windows: Ouvrez PowerShell et tapez: ipconfig | findstr IPv4
    // Linux/Mac: Ouvrez Terminal et tapez: ifconfig | grep inet
    // Ou vérifiez dans votre backend NestJS quand vous démarrez le serveur (npm run start)
    //
    // Assurez-vous que:
    // 1. Le téléphone et l'ordinateur sont sur le même réseau WiFi
    // 2. Le serveur NestJS est démarré et accessible
    // 3. Le firewall n'bloque pas le port 3007
    // 4. L'IP dans network_security_config.xml correspond à cette IP
    
    // 🔧 Pour modifier l'URL sans Android Studio:
    // 1. Créez un fichier "backend_url.txt" dans le dossier assets de l'app
    // 2. Écrivez simplement l'URL: http://192.168.1.XXX:3007/
    // 3. L'app utilisera automatiquement cette URL au démarrage
    
    // Valeur par défaut
    private const val DEFAULT_URL = "http://10.0.2.2:3007/"
    
    // Cache pour éviter de lire le fichier à chaque fois
    @Volatile
    private var cachedUrl: String? = null
    
    /**
     * Récupère l'URL du backend avec priorité:
     * 1. Fichier backend_url.txt dans assets (modifiable sans recompiler)
     * 2. BuildConfig.SERVER_URL (depuis local.properties ou variable d'environnement)
     * 3. URL par défaut (10.0.2.2 pour émulateur)
     */
    fun getBaseUrl(context: Context? = null, forceRefresh: Boolean = false): String {
        // Si on force le rafraîchissement, on vide le cache
        if (forceRefresh) {
            cachedUrl = null
        }
        
        // Si on a déjà une URL en cache et qu'on ne force pas le rafraîchissement, on l'utilise
        if (!forceRefresh) {
            cachedUrl?.let { return ensureTrailingSlash(it) }
        }
        
        // Essayer de lire depuis le fichier assets (priorité 1)
        context?.let {
            try {
                val urlFromFile = readUrlFromAssets(it)
                if (urlFromFile.isNotBlank()) {
                    cachedUrl = urlFromFile
                    return ensureTrailingSlash(urlFromFile)
                }
            } catch (e: Exception) {
                // Fichier n'existe pas ou erreur de lecture, continuer
            }
        }
        
        // Essayer BuildConfig (priorité 2)
        try {
            val buildConfigUrl = try {
                val buildConfigClass = Class.forName("com.sim.darna.BuildConfig")
                val serverUrlField = buildConfigClass.getField("SERVER_URL")
                serverUrlField.get(null) as? String
            } catch (e: Exception) {
                null
            }
            
            if (!buildConfigUrl.isNullOrBlank()) {
                cachedUrl = buildConfigUrl
                return ensureTrailingSlash(buildConfigUrl)
            }
        } catch (e: Exception) {
            // BuildConfig non disponible, continuer
        }
        
        // Utiliser l'URL par défaut (priorité 3)
        cachedUrl = DEFAULT_URL
        return ensureTrailingSlash(DEFAULT_URL)
    }
    
    /**
     * Version simplifiée qui utilise BuildConfig directement (pour compatibilité)
     * Cette propriété est utilisée dans les factories qui n'ont pas accès au Context
     */
    val BASE_URL: String
        get() {
            cachedUrl?.let { return ensureTrailingSlash(it) }
            
            val url = try {
                // Essayer d'accéder à BuildConfig via reflection
                val buildConfigClass = Class.forName("com.sim.darna.BuildConfig")
                val serverUrlField = buildConfigClass.getField("SERVER_URL")
                val serverUrl = serverUrlField.get(null) as? String
                if (serverUrl.isNullOrBlank()) {
                    DEFAULT_URL
                } else {
                    serverUrl
                }
            } catch (e: ClassNotFoundException) {
                // BuildConfig n'existe pas encore (pendant la compilation)
                DEFAULT_URL
            } catch (e: NoSuchFieldException) {
                // Le champ SERVER_URL n'existe pas
                DEFAULT_URL
            } catch (e: Exception) {
                // Autre erreur
                DEFAULT_URL
            }
            
            cachedUrl = url
            return ensureTrailingSlash(url)
        }
    
    /**
     * Lit l'URL depuis le fichier backend_url.txt dans assets
     */
    private fun readUrlFromAssets(context: Context): String {
        return try {
            val url = context.assets.open("backend_url.txt").bufferedReader().use { 
                it.readLine()?.trim() ?: "" 
            }
            // Vérifier que l'URL est valide
            if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                url
            } else {
                ""
            }
        } catch (e: Exception) {
            // Fichier n'existe pas ou erreur de lecture
            ""
        }
    }
    
    /**
     * Réinitialise le cache (utile pour forcer une relecture)
     */
    fun clearCache() {
        cachedUrl = null
    }
    
    private fun ensureTrailingSlash(url: String): String =
        if (url.endsWith("/")) url else "$url/"
}



