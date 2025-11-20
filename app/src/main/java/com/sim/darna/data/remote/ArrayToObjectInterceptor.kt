package com.sim.darna.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * Intercepteur qui transforme les réponses JSON en tableau en objet unique
 * pour les endpoints qui devraient retourner un objet mais retournent un tableau
 */
class ArrayToObjectInterceptor : Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        // Vérifier si c'est une requête GET pour obtenir une publicité par ID
        if (request.method == "GET" && request.url.encodedPath.contains("/publicites/") 
            && !request.url.encodedPath.endsWith("/publicites")) {
            
            val responseBody = response.body
            if (responseBody != null && response.isSuccessful) {
                val contentType = responseBody.contentType()
                if (contentType != null && contentType.type == "application" && contentType.subtype == "json") {
                    val jsonString = responseBody.string()
                    
                    // Si la réponse commence par '[', c'est un tableau
                    if (jsonString.trimStart().startsWith("[")) {
                        // Prendre le premier élément du tableau
                        val json = jsonString.trimStart()
                        if (json.length > 2 && json.startsWith("[") && json.endsWith("]")) {
                            val arrayContent = json.substring(1, json.length - 1).trim()
                            // Trouver le premier objet complet dans le tableau
                            if (arrayContent.startsWith("{")) {
                                var braceCount = 0
                                var endIndex = -1
                                for (i in arrayContent.indices) {
                                    when (arrayContent[i]) {
                                        '{' -> braceCount++
                                        '}' -> {
                                            braceCount--
                                            if (braceCount == 0) {
                                                endIndex = i + 1
                                                break
                                            }
                                        }
                                    }
                                }
                                if (endIndex > 0) {
                                    val firstObject = arrayContent.substring(0, endIndex)
                                    val newResponseBody = firstObject.toResponseBody(contentType)
                                    
                                    return response.newBuilder()
                                        .body(newResponseBody)
                                        .build()
                                }
                            }
                        }
                    } else {
                        // Si ce n'est pas un tableau, recréer le body avec le contenu original
                        val newResponseBody = jsonString.toResponseBody(contentType)
                        return response.newBuilder()
                            .body(newResponseBody)
                            .build()
                    }
                }
            }
        }
        
        return response
    }
}

