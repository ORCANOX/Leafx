package com.example.leaf

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.View
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.*

object LanguageManager {
    private const val LANGUAGE_PREFS = "language_prefs"
    private const val LANGUAGE_KEY = "language"
    
    private val _currentLanguage = mutableStateOf("en")
    val currentLanguage: State<String> = _currentLanguage

    fun setLanguage(context: Context, languageCode: String) {
        val locale = when (languageCode) {
            "ar" -> Locale("ar")
            "fr" -> Locale("fr")
            else -> Locale("en")
        }

        Locale.setDefault(locale)
        
        val newConfig = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        
        context.createConfigurationContext(newConfig)
        context.resources.updateConfiguration(newConfig, context.resources.displayMetrics)

        context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_KEY, languageCode)
            .apply()
            
        _currentLanguage.value = languageCode
    }

    fun getCurrentLanguage(context: Context): String {
        return context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
            .getString(LANGUAGE_KEY, "en") ?: "en"
    }

    fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "ar" -> "العربية"
            "fr" -> "Français"
            else -> "English"
        }
    }

    fun updateContext(context: Context): Context {
        val languageCode = getCurrentLanguage(context)
        val locale = when (languageCode) {
            "ar" -> Locale("ar")
            "fr" -> Locale("fr")
            else -> Locale("en")
        }

        Locale.setDefault(locale)
        
        val newConfig = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        
        return context.createConfigurationContext(newConfig)
    }

    fun isRtl(context: Context): Boolean {
        return getCurrentLanguage(context) == "ar"
    }

    @Composable
    fun LocalizedContent(content: @Composable () -> Unit) {
        val configuration = LocalConfiguration.current
        val languageCode = currentLanguage.value
        val locale = when (languageCode) {
            "ar" -> Locale("ar")
            "fr" -> Locale("fr")
            else -> Locale("en")
        }

        val newConfiguration = Configuration(configuration).apply {
            setLocale(locale)
        }

        CompositionLocalProvider(
            LocalConfiguration provides newConfiguration
        ) {
            content()
        }
    }
} 