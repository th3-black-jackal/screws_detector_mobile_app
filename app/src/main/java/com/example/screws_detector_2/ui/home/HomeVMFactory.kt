package com.example.screws_detector_2.ui.home


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HomeVMFactory(private val ctx: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when (modelClass) {
            HomeViewModel::class.java -> HomeViewModel(ctx.applicationContext) as T
            else -> throw IllegalArgumentException("Unknown ViewModel $modelClass")
        }
}
