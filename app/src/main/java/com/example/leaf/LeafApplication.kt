package com.example.leaf

import android.app.Application
import android.content.Context

class LeafApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val savedLanguage = LanguageManager.getCurrentLanguage(this)
        LanguageManager.setLanguage(this, savedLanguage)
    }

    override fun attachBaseContext(base: Context) {
        val context = LanguageManager.updateContext(base)
        super.attachBaseContext(context)
    }
} 