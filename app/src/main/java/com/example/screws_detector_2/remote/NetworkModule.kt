// com/example/screws_detector/data/remote/NetworkModule.kt
package com.example.screws_detector.data.remote

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.screws_detector_2.remote.ServiceAPI
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory   // ← NEW
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    // 👉 point to your backend (10.0.2.2 == host-machine on Android emulator)
    //private const val BASE_URL = "http://10.0.2.2:8000/"

    private fun buildBaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences("network_prefs", Context.MODE_PRIVATE)
        val ip    = prefs.getString("ip",   "10.0.2.2") ?: "10.0.2.2"
        val port  = prefs.getString("port", "8000")     ?: "8000"
        Log.d("HOST_IP", ip)
        Log.d("HOST_PORT", port)
        return "http://$ip:$port/"
    }


    /** Retrofit entry point (singleton per app-process). */
    fun apiService(context: Context): ServiceAPI {

        /* ───── Authorization header interceptor ───── */
        val prefs = tokenPrefs(context)
        val authInterceptor = Interceptor { chain ->
            val token = prefs.getString("access", null)
            val req   = if (token != null)
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            else chain.request()

            chain.proceed(req)
        }

        /* ───── OkHttp client with logging + timeouts ───── */
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(authInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15,  TimeUnit.SECONDS)
            .build()

        /* ───── Moshi with KotlinJsonAdapterFactory (code-gen aware) ───── */
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())       // ← IMPORTANT
            .build()

        /* ───── Build Retrofit instance ───── */
        return Retrofit.Builder()
            .baseUrl(buildBaseUrl(context))
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ServiceAPI::class.java)
    }

    /** Encrypted SharedPreferences for storing **access / refresh** tokens. */
    fun tokenPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            "auth_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
}
