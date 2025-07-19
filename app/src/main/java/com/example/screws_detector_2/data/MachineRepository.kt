package com.example.screws_detector_2.data

import android.content.Context
import com.example.screws_detector_2.remote.NetworkModule



object MachineRepository {
    suspend fun getAll(context: Context) =
        NetworkModule.apiService(context).getMachines()
}
