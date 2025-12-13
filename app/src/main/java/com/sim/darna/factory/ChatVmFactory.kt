package com.sim.darna.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sim.darna.auth.SessionManager
import com.sim.darna.chat.ChatApi
import com.sim.darna.chat.ChatRepository
import com.sim.darna.chat.ChatViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

class ChatVmFactory(
    private val baseUrl: String,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val token = com.sim.darna.auth.TokenStorage.getToken(context)
        val userId = com.sim.darna.auth.TokenStorage.getUserId(context)

        val chatApi = com.sim.darna.auth.RetrofitClient.chatApi(context)
        val chatRepo = ChatRepository(chatApi)
        
        @Suppress("UNCHECKED_CAST")
        return ChatViewModel(chatRepo, baseUrl, token, userId) as T
    }
}

