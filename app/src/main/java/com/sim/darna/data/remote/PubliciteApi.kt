package com.sim.darna.data.remote

import com.sim.darna.data.model.CreatePubliciteRequest
import com.sim.darna.data.model.Publicite
import com.sim.darna.data.model.UpdatePubliciteRequest
import retrofit2.Response
import retrofit2.http.*

interface PubliciteApi {
    @GET("publicites")
    suspend fun getAllPublicites(
        @Query("categorie") categorie: String? = null,
        @Query("search") search: String? = null
    ): Response<List<Publicite>>

    @GET("publicites/{id}")
    suspend fun getPubliciteById(@Path("id") id: String): Response<Publicite>

    @POST("publicites")
    suspend fun createPublicite(
        @Body request: CreatePubliciteRequest
    ): Response<Publicite>

    @PUT("publicites/{id}")
    suspend fun updatePublicite(
        @Path("id") id: String,
        @Body request: UpdatePubliciteRequest
    ): Response<Publicite>

    @DELETE("publicites/{id}")
    suspend fun deletePublicite(@Path("id") id: String): Response<Unit>
}

