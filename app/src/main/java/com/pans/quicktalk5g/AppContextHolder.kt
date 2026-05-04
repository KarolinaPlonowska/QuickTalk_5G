package com.pans.quicktalk5g
import android.app.Application
import android.content.Context
class AppContextHolder : Application() {
    companion object {
        lateinit var context: Context
            private set
    }
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}
