package com.sim.darna.data.repository

import com.sim.darna.data.model.Categorie
import com.sim.darna.data.model.Publicite
import com.sim.darna.data.remote.PubliciteApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PubliciteRepository @Inject constructor(
    private val api: PubliciteApi
) {
    suspend fun getAllPublicites(
        categorie: Categorie? = null,
        searchQuery: String? = null
    ): Result<List<Publicite>> {
        return try {
            val response = api.getAllPublicites(
                categorie = categorie?.name,
                search = searchQuery
            )
            when {
                response.isSuccessful && response.body() != null -> {
                    Result.success(response.body()!!)
                }
                response.code() == 404 -> {
                    Result.failure(Exception("Endpoint non trouvé. Vérifiez que le backend NestJS est démarré et que l'endpoint /api/publicites existe."))
                }
                else -> {
                    Result.failure(Exception("Erreur ${response.code()}: ${response.message()}"))
                }
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Impossible de se connecter au serveur. Vérifiez que le backend est démarré sur http://10.0.2.2:3000"))
        } catch (e: java.net.ConnectException) {
            Result.failure(Exception("Connexion refusée. Vérifiez que le backend NestJS est démarré et écoute sur le port 3000."))
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau: ${e.message}"))
        }
    }

    suspend fun getPubliciteById(id: String): Result<Publicite> {
        return try {
            val response = api.getPubliciteById(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Réponse vide de l'API. Code: ${response.code()}"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Erreur: ${response.code()} - ${response.message()}${if (errorBody != null) "\n$errorBody" else ""}"))
            }
        } catch (e: Exception) {
            // Si c'est une erreur de sérialisation indiquant qu'un tableau est retourné au lieu d'un objet
            if (e is kotlinx.serialization.SerializationException ||
                e.message?.contains("Expected start of the object", ignoreCase = true) == true ||
                e.message?.contains("Expected '{'", ignoreCase = true) == true ||
                e.message?.contains("had '[' instead", ignoreCase = true) == true) {
                // L'API retourne un tableau au lieu d'un objet
                // Solution: Récupérons toutes les publicités et filtrons par ID
                try {
                    val allPublicites = getAllPublicites()
                    allPublicites.fold(
                        onSuccess = { list ->
                            val found = list.find { it.id == id }
                            if (found != null) {
                                Result.success(found)
                            } else {
                                Result.failure(Exception("Publicité avec l'ID $id non trouvée"))
                            }
                        },
                        onFailure = { error ->
                            Result.failure(Exception("Impossible de récupérer la publicité: ${error.message}"))
                        }
                    )
                } catch (e2: Exception) {
                    Result.failure(Exception("Erreur lors de la récupération: ${e2.message}"))
                }
            } else {
                Result.failure(Exception("Erreur réseau: ${e.message}"))
            }
        }
    }

    suspend fun createPublicite(publicite: com.sim.darna.data.model.CreatePubliciteRequest): Result<Publicite> {
        return try {
            val response = api.createPublicite(publicite)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePublicite(
        id: String,
        publicite: com.sim.darna.data.model.UpdatePubliciteRequest
    ): Result<Publicite> {
        return try {
            val response = api.updatePublicite(id, publicite)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePublicite(id: String): Result<Unit> {
        return try {
            val response = api.deletePublicite(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erreur: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

