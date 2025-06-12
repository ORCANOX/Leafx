package com.example.leaf

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.leaf.screens.*
import com.example.leaf.ui.theme.LeafTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val SERVER_URL_PREFS = "server_url_prefs"
        private const val SERVER_URL_KEY = "server_url"
        private const val DEFAULT_SERVER_URL = "http://10.0.2.2:5000"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        LanguageManager.setLanguage(this, LanguageManager.getCurrentLanguage(this))
        
        setContent {
            LeafTheme {
                LanguageManager.LocalizedContent {
                    var showWelcome by remember { mutableStateOf(true) }
                    var currentScreen by remember { mutableStateOf(Screen.Home) }
                    
                    // Get saved server URL or use default
                    val context = LocalContext.current
                    val sharedPrefs = context.getSharedPreferences(SERVER_URL_PREFS, Context.MODE_PRIVATE)
                    var serverUrl by remember { 
                        mutableStateOf(
                            sharedPrefs.getString(SERVER_URL_KEY, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
                        ) 
                    }
                    
                    val currentLanguage = LanguageManager.currentLanguage
                    val isRtl = currentLanguage.value == "ar"

                    if (showWelcome) {
                        WelcomeScreen(
                            onGetStarted = { showWelcome = false }
                        )
                    } else {
                        MainScreen(
                            currentScreen = currentScreen,
                            onScreenChange = { currentScreen = it },
                            serverUrl = serverUrl,
                            onUrlSaved = { newUrl ->
                                serverUrl = newUrl
                                // Save the new URL to SharedPreferences
                                sharedPrefs.edit().putString(SERVER_URL_KEY, newUrl).apply()
                            },
                            isRtl = isRtl
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit,
    serverUrl: String,
    onUrlSaved: (String) -> Unit,
    isRtl: Boolean
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(72.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                val navItems = listOf(
                    Screen.Home to Icons.Default.Home,
                    Screen.Detection to Icons.Default.Camera,
                    Screen.History to Icons.Default.History,
                    Screen.Settings to Icons.Default.Settings
                )

                val orderedItems = if (isRtl) navItems.reversed() else navItems

                orderedItems.forEach { (screen, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = null) },
                        label = { 
                            Text(
                                when (screen) {
                                    Screen.Home -> stringResource(R.string.nav_home)
                                    Screen.Detection -> stringResource(R.string.nav_detection)
                                    Screen.History -> stringResource(R.string.nav_history)
                                    Screen.Settings -> stringResource(R.string.nav_settings)
                                }
                            )
                        },
                        selected = currentScreen == screen,
                        onClick = { onScreenChange(screen) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                Screen.Home -> HomeScreen()
                Screen.Detection -> DetectionScreen(serverUrl = serverUrl)
                Screen.History -> HistoryScreen()
                Screen.Settings -> SettingsScreen(
                    currentUrl = serverUrl,
                    onUrlSaved = onUrlSaved
                )
            }
        }
    }
}

enum class Screen {
    Home, Detection, History, Settings
}