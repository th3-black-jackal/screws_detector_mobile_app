package com.example.screws_detector_2.remote

import com.example.screws_detector_2.data.model.Machine
//import com.example.screws_detector.data.model.MachineDetail
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

    /*@GET("api/v1/machines/{id}/")
    suspend fun getMachine(@Path("id") id: Int): MachineDetail*/

}