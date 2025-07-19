package com.example.screws_detector_2.remote

import com.example.screws_detector_2.data.AccessResponse
import com.example.screws_detector_2.data.model.Machine
import com.example.screws_detector_2.data.model.LoginRequest
import com.example.screws_detector_2.data.model.TokenStore
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ServiceAPI {
    @POST("api/v1/auth/token/")
    suspend fun login(@Body body: LoginRequest): TokenStore

    @GET("api/v1/machines/")              // ← NEW
    suspend fun getMachines(): List<Machine>

    @POST("api/v1/machines/{id}/open/")
    suspend fun openMachine(@Path("id") id: Int): Unit   // 204/200 no-body expected

    @POST("api/v1/machines/{id}/close/")
    suspend fun closeMachine(@Path("id") id: Int): Unit

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body body: Map<String, String>): AccessResponse

}