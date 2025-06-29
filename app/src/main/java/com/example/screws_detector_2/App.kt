package com.example.screws_detector_2

import android.app.Application
import android.content.Context

class App : Application() {
    override fun onCreate() { super.onCreate(); context = this }
    companion object { lateinit var context: Context; private set }
}
