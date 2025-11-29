package com.sim.darna.network

import com.google.gson.annotations.SerializedName
import okhttp3.Dns
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class NominatimResult(
    @SerializedName("display_name") val displayName: String,
    val lat: String,
    val lon: String
)

data class NominatimReverseResult(
    @SerializedName("display_name") val displayName: String?
)

private interface NominatimApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @Query("addressdetails") addressDetails: Int = 0
    ): List<NominatimResult>

    @GET("reverse")
    suspend fun reverse(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json"
    ): NominatimReverseResult
}

object NominatimService {

    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"

    private val api: NominatimApi

    init {
        val client = OkHttpClient.Builder()
            .dns(Dns.SYSTEM)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "DarnaApp/1.0 (contact@darnaapp.com)")
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(NOMINATIM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        api = retrofit.create(NominatimApi::class.java)
    }

    suspend fun search(query: String): List<NominatimResult> = api.search(query)

    suspend fun reverse(latitude: Double, longitude: Double): NominatimReverseResult =
        api.reverse(latitude, longitude)
}

