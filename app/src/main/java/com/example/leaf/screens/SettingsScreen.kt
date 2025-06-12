package com.example.leaf.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.leaf.LanguageManager
import com.example.leaf.MainActivity
import com.example.leaf.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUrl: String,
    onUrlSaved: (String) -> Unit
) {
    var serverUrl by remember { mutableStateOf(currentUrl) }
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showUrlSuccessDialog by remember { mutableStateOf(false) }
    val currentLanguage = LanguageManager.currentLanguage

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.settings_title),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.server_url),
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = { 
                    onUrlSaved(serverUrl)
                    showUrlSuccessDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_url))
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick = { showLanguageDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when (currentLanguage.value) {
                        "ar" -> stringResource(R.string.language_arabic)
                        "fr" -> stringResource(R.string.language_french)
                        else -> stringResource(R.string.language_english)
                    }
                )
            }
        }
    }

    if (showUrlSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showUrlSuccessDialog = false },
            title = { Text(stringResource(R.string.url_saved)) },
            text = { 
                Column {
                    Text(stringResource(R.string.url_saved_message))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = serverUrl,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showUrlSuccessDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column {
                    LanguageOption(
                        languageCode = "en",
                        languageName = "English",
                        selected = currentLanguage.value == "en",
                        onSelect = {
                            LanguageManager.setLanguage(context, "en")
                            showLanguageDialog = false
                            restartActivity(context)
                        }
                    )
                    LanguageOption(
                        languageCode = "ar",
                        languageName = "العربية",
                        selected = currentLanguage.value == "ar",
                        onSelect = {
                            LanguageManager.setLanguage(context, "ar")
                            showLanguageDialog = false
                            restartActivity(context)
                        }
                    )
                    LanguageOption(
                        languageCode = "fr",
                        languageName = "Français",
                        selected = currentLanguage.value == "fr",
                        onSelect = {
                            LanguageManager.setLanguage(context, "fr")
                            showLanguageDialog = false
                            restartActivity(context)
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun LanguageOption(
    languageCode: String,
    languageName: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Text(
            text = languageName,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun restartActivity(context: Context) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    context.startActivity(intent)
    (context as? Activity)?.finish()
} 