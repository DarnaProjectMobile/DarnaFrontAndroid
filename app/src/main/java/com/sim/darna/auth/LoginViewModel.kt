package com.sim.darna.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val message: String? = null
)

class LoginViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginUiState(isLoading = true, error = null)
            
            val request = LoginRequest(email, password)
            
            try {
                // Utilisation de suspend pour mieux gérer les timeouts
                val response = repository.login(request)
                
                // Sauvegarder la session
                try {
                    sessionManager.saveSession(response)
                    _state.value = LoginUiState(
                        success = true,
                        message = "Connexion réussie ✅"
                    )
                } catch (e: Exception) {
                    _state.value = LoginUiState(
                        error = "Erreur lors de la sauvegarde de la session: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                val errorMessage = resolveError(e)
                _state.value = LoginUiState(
                    error = errorMessage // null si erreur 403, sera ignoré
                )
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun resolveError(error: Exception): String? {
        // L'URL est déjà configurée dans le repository via la factory
        // On utilise BASE_URL comme fallback, mais normalement l'URL correcte est déjà utilisée
        val serverUrl = com.sim.darna.network.NetworkConfig.BASE_URL
        
        return when (error) {
            
            is ConnectException -> {
                val isApipa = serverUrl.contains("169.254")
                val isLocalhost = serverUrl.contains("localhost") || serverUrl.contains("127.0.0.1")
                
                when {
                    isApipa -> {
                        "⚠️ Impossible de joindre le serveur\n\n" +
                        "L'IP $serverUrl est une adresse APIPA (169.254.x.x) qui n'est pas accessible depuis le téléphone.\n\n" +
                        "✅ Solution :\n" +
                        "1. Trouvez votre IP locale avec : ipconfig | findstr IPv4 (Windows)\n" +
                        "2. Utilisez une IP 192.168.x.x ou 10.0.2.2 (pour émulateur)\n" +
                        "3. Mettez à jour app/src/main/assets/backend_url.txt avec la nouvelle IP\n" +
                        "4. Recompilez l'application (Build > Rebuild Project)"
                    }
                    isLocalhost -> {
                        "⚠️ Erreur : localhost n'est pas accessible depuis un téléphone réel\n\n" +
                        "Pour un téléphone réel, utilisez l'IP locale de votre PC (192.168.x.x)\n\n" +
                        "✅ Solution :\n" +
                        "1. Trouvez votre IP avec : ipconfig | findstr IPv4\n" +
                        "2. Mettez à jour app/src/main/assets/backend_url.txt : http://VOTRE_IP:3007/\n" +
                        "3. Recompilez et réinstallez l'application"
                    }
                    else -> {
                        val isVirtualBoxIP = serverUrl.contains("192.168.56")
                        if (isVirtualBoxIP) {
                            "⚠️ IP VirtualBox détectée\n\n" +
                            "L'IP 192.168.56.x n'est pas accessible depuis un téléphone réel.\n\n" +
                            "✅ Solution:\n\n" +
                            "1. Trouvez votre IP WiFi réelle:\n" +
                            "   Windows: ipconfig | findstr IPv4\n" +
                            "   (Cherchez 192.168.1.x ou 192.168.0.x)\n\n" +
                            "2. Modifiez backend_url.txt avec cette IP\n\n" +
                            "3. Recompilez l'application\n\n" +
                            "💡 Si le WiFi change, mettez à jour backend_url.txt"
                        } else {
                            "Impossible de joindre le serveur.\n\n" +
                            "Causes possibles:\n" +
                            "• WiFi changé → IP du serveur a changé\n" +
                            "• Serveur non démarré\n" +
                            "• Firewall bloque le port 3007\n\n" +
                            "✅ Solutions:\n\n" +
                            "1. Vérifiez l'IP actuelle du serveur:\n" +
                            "   (Regardez la console: Network: http://...)\n\n" +
                            "2. Si l'IP a changé:\n" +
                            "   Modifiez app/src/main/assets/backend_url.txt\n" +
                            "   avec la nouvelle IP affichée\n\n" +
                            "3. Vérifiez:\n" +
                            "   • Téléphone et PC sur le même WiFi\n" +
                            "   • Firewall autorise le port 3007\n\n" +
                            "4. Recompilez et réinstallez l'app"
                        }
                    }
                }
            }
            
            is SocketTimeoutException -> {
                "Impossible de joindre le serveur.\n\n" +
                "Causes possibles:\n" +
                "• WiFi changé → IP du serveur a changé\n" +
                "• Serveur non démarré\n" +
                "• Firewall bloque le port 3007\n\n" +
                "✅ Solutions:\n\n" +
                "1. Vérifiez l'IP actuelle du serveur:\n" +
                "   (Regardez la console: Network: http://...)\n\n" +
                "2. Si l'IP a changé:\n" +
                "   Modifiez app/src/main/assets/backend_url.txt\n" +
                "   avec la nouvelle IP affichée\n\n" +
                "3. Vérifiez:\n" +
                "   • Téléphone et PC sur le même WiFi\n" +
                "   • Firewall autorise le port 3007\n\n" +
                "4. Recompilez et réinstallez l'app"
            }
            
            is UnknownHostException -> {
                val message = error.message ?: ""
                when {
                    message.contains("169.254", ignoreCase = false) || serverUrl.contains("169.254") -> 
                        "⚠️ Erreur : L'IP 169.254.x.x (APIPA) n'est pas accessible.\n\n" +
                        "✅ Solution : Utilisez l'IP locale de votre PC (192.168.x.x)\n\n" +
                        "1. Trouvez votre IP avec : ipconfig (Windows) ou ifconfig (Linux/Mac)\n" +
                        "2. Mettez à jour local.properties : backend.url=http://VOTRE_IP:3007/\n" +
                        "3. Recompilez l'application\n\n" +
                        "IP actuelle : $serverUrl"
                    else -> 
                        "Impossible de résoudre l'adresse du serveur.\n\n" +
                        "Serveur: $serverUrl\n\n" +
                        "Vérifiez:\n" +
                        "1. Que l'IP est correcte (utilisez: ipconfig)\n" +
                        "2. Que vous êtes sur le même réseau WiFi\n" +
                        "3. Modifiez backend.url dans local.properties si nécessaire"
                }
            }
            
            is IOException -> {
                val message = error.message ?: ""
                val errorLower = message.lowercase()
                when {
                    errorLower.contains("failed to connect", ignoreCase = true) || 
                    errorLower.contains("unable to resolve host", ignoreCase = true) -> 
                        "Échec de connexion au serveur.\n\n" +
                        "Serveur: $serverUrl\n\n" +
                        "Vérifiez:\n" +
                        "• Que l'IP est correcte (ipconfig | findstr IPv4)\n" +
                        "• Que le serveur est démarré (npm run start)\n" +
                        "• Que vous êtes sur le même WiFi\n" +
                        "• Que le firewall autorise le port 3007\n\n" +
                        "💡 Pour changer l'URL:\n" +
                        "Modifiez app/src/main/assets/backend_url.txt puis recompilez"
                    errorLower.contains("connection refused", ignoreCase = true) -> 
                        "Connexion refusée. Le serveur n'est pas accessible.\n\n" +
                        "Serveur: $serverUrl\n\n" +
                        "Vérifiez:\n" +
                        "• Que le serveur est démarré (npm run start)\n" +
                        "• Que le firewall autorise le port 3007\n" +
                        "• Que l'IP est correcte (ipconfig | findstr IPv4)"
                    errorLower.contains("network is unreachable", ignoreCase = true) || 
                    errorLower.contains("no route to host", ignoreCase = true) -> 
                        "Réseau inaccessible.\n\n" +
                        "Vérifiez:\n" +
                        "1. Votre connexion WiFi est active\n" +
                        "2. Que vous êtes connecté au même réseau que le serveur\n" +
                        "3. Que l'IP du serveur est correcte"
                    errorLower.contains("timeout", ignoreCase = true) -> 
                        "Timeout de connexion.\n\n" +
                        "Serveur: $serverUrl\n\n" +
                        "Vérifiez:\n" +
                        "• Que le serveur est démarré\n" +
                        "• Que l'IP est correcte\n" +
                        "• Que vous êtes sur le même WiFi"
                    else -> 
                        "Erreur de connexion réseau.\n\n" +
                        "Serveur: $serverUrl\n\n" +
                        "Vérifiez:\n" +
                        "• Que le serveur est démarré (npm run start)\n" +
                        "• Que l'IP est correcte (ipconfig | findstr IPv4)\n" +
                        "• Que vous êtes sur le même WiFi\n" +
                        "• Que le firewall autorise le port 3007\n\n" +
                        "💡 Pour changer l'URL:\n" +
                        "Modifiez app/src/main/assets/backend_url.txt puis recompilez"
                }
            }
            
            is HttpException -> {
                when (error.code()) {
                    401 -> "Email ou mot de passe incorrect"
                    403 -> null // Ne jamais afficher les erreurs 403
                    404 -> "Endpoint non trouvé. Vérifiez que le serveur est correctement configuré"
                    500 -> "Erreur serveur interne. Veuillez réessayer plus tard"
                    else -> "Erreur serveur (${error.code()})"
                }
            }
            
            else -> {
                val message = error.localizedMessage ?: error.message ?: "Erreur inconnue"
                "Erreur : $message\n\nServeur: $serverUrl"
            }
        }
    }
}
