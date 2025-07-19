package com.example.screws_detector_2.service

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.example.screws_detector_2.data.model.Machine
import com.example.screws_detector_2.remote.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MachineService : LifecycleService() {
    inner class LocalBinder : Binder() {
        fun service(): MachineService = this@MachineService
    }

    private val binder = LocalBinder()
    private val api by lazy { NetworkModule.apiService(this) }
    private val ioScope = CoroutineScope(Job() + Dispatchers.IO)
    @SuppressLint("MissingSuperCall")
    override fun onBind(intent: Intent): IBinder = binder

    fun getMachines(
        onSuccess: (List<Machine>) -> Unit,
        onError:   (Throwable) -> Unit = {}
    ) = ioScope.launch {
        runCatching { api.getMachines() }
            .onSuccess { onSuccess(it) }
            .onFailure { onError(it) }
    }

    fun openMachine(
        id: Int,
        onComplete: (Boolean) -> Unit
    ) = ioScope.launch {
        runCatching { api.openMachine(id) }
            .onSuccess { onComplete(true) }
            .onFailure { onComplete(false) }
    }

    fun closeMachine(
        id: Int,
        onComplete: (Boolean) -> Unit
    ) = ioScope.launch {
        runCatching { api.closeMachine(id) }
            .onSuccess { onComplete(true) }
            .onFailure { onComplete(false) }
    }
}