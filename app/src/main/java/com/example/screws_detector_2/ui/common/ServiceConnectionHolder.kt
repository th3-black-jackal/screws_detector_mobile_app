package com.example.screws_detector_2.ui.common

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.screws_detector_2.service.MachineService

class MachineServiceConnection(private val ctx: Context) : ServiceConnection{
    var service: MachineService? = null
        private set
    val isConnected: Boolean
        get() = service != null
    override fun onServiceConnected(name: ComponentName, binder: IBinder){
        service = (binder as MachineService.LocalBinder).service()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
    }

    fun bind(){
        Intent(ctx, MachineService::class.java).also { intent -> ctx.bindService(intent, this, Context.BIND_AUTO_CREATE)}
    }
    fun unbind() = runCatching {
        ctx.unbindService(this)
        service = null
    }
}