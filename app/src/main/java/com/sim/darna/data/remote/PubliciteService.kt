package com.sim.darna.data.remote

import com.sim.darna.data.model.Publicite
import retrofit2.Response
import retrofit2.http.*

interface PubliciteApi {

    @GET("publicites")
    suspend fun getAll(): Response<List<Publicite>>

    @GET("publicites/{id}")
    suspend fun getOne(@Path("id") id: String): Response<Publicite>

    @POST("publicites")
    suspend fun create(
        @Header("Authorization") token: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
        // si besoin upload multipart pour image -> @Multipart + @Part
    ): Response<Publicite>

    @PUT("publicites/{id}")
    suspend fun update(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<Publicite>

    @DELETE("publicites/{id}")
    suspend fun delete(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
