// com/example/screws_detector/data/remote/NetworkModule.kt
package com.example.screws_detector_2.remote

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    /* ------------------------------------------------------------ *
     * 1)  BASE-URL: read IP + port from plain prefs (default localhost)
     * ------------------------------------------------------------ */
    private fun buildBaseUrl(ctx: Context): String {
        val prefs = ctx.getSharedPreferences("network_prefs", Context.MODE_PRIVATE)
        val ip    = prefs.getString("ip",   "10.0.2.2") ?: "10.0.2.2"
        val port  = prefs.getString("port", "8000")     ?: "8000"
        Log.d("HOST_IP",   ip)
        Log.d("HOST_PORT", port)
        return "http://$ip:$port/"
    }

    /* ------------------------------------------------------------ *
     * 2)  Encrypted prefs – store access / refresh tokens securely
     * ------------------------------------------------------------ */
    fun tokenPrefs(ctx: Context) =
        EncryptedSharedPreferences.create(
            "auth_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            ctx,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    /* ------------------------------------------------------------ *
     * 3)  Public entry-point
     * ------------------------------------------------------------ */
    fun apiService(ctx: Context): ServiceAPI = retrofit(ctx).create(ServiceAPI::class.java)

    /* ------------------------------------------------------------ *
     * -------- Below here everything is internal plumbing -------- *
     * ------------------------------------------------------------ */

    @Volatile private var memoised: Retrofit? = null
    @Volatile private var memoisedBaseUrl: String? = null
    private val lock = Any()

    /** Lazily (re)build Retrofit if BASE_URL has changed. */
    private fun retrofit(ctx: Context): Retrofit {
        val url = buildBaseUrl(ctx)
        memoised?.let { if (url == memoisedBaseUrl) return it }

        synchronized(lock) {
            memoised?.let { if (url == memoisedBaseUrl) return it } // re-check
            memoisedBaseUrl = url
            memoised = newRetrofit(ctx, url)
            return memoised!!
        }
    }

    /* ------------------------------------------------------------ *
     * 4)  Fresh Retrofit instance
     * ------------------------------------------------------------ */
    private fun newRetrofit(ctx: Context, baseUrl: String): Retrofit {
        val prefs = tokenPrefs(ctx)

        /* ---- A)  Interceptor:  add Bearer token ---- */
        val authInterceptor = Interceptor { chain ->
            val token = prefs.getString("access", null)
            val req: Request = if (token != null)
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            else chain.request()

            chain.proceed(req)
        }

        /* ---- B)  Interceptor:  refresh token on 401 (single retry) ---- */
        val refreshInterceptor = Interceptor { chain ->
            val origResponse = chain.proceed(chain.request())
            if (origResponse.code != 401) return@Interceptor origResponse

            // 401 hit – try refreshing once
            origResponse.close()
            val refresh = prefs.getString("refresh", null) ?: return@Interceptor origResponse
            val newAccess = runBlocking {
                runCatching {
                    // synchronous refresh call (small & fast)
                    val api = Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(MoshiConverterFactory.create())
                        .client(OkHttpClient()) // bare client
                        .build()
                        .create(ServiceAPI::class.java)
                        .refreshToken(mapOf("refresh" to refresh))
                        .access
                }.getOrNull()
            } ?: return@Interceptor origResponse

            // save & retry original
            prefs.edit().putString("access", newAccess.toString()).apply()
            val newReq = chain.request().newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer $newAccess")
                .build()
            chain.proceed(newReq)
        }

        /* ---- C)  OkHttp ---- */
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15,  TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(authInterceptor)
            .addInterceptor(refreshInterceptor)
            .build()

        /* ---- D)  Moshi ---- */
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
