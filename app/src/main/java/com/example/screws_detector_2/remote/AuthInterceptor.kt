package com.example.screws_detector_2.remote

import com.example.screws_detector_2.data.model.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val store: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val access = store.access
        val newReq = if (access != null)
            req.newBuilder()
                .addHeader("Authorization", "Bearer $access")
                .build()
        else req
        return chain.proceed(newReq)
    }
}