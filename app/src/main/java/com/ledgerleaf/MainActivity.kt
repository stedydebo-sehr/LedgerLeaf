package com.ledgerleaf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerleaf.core.datastore.ThemeMode
import com.ledgerleaf.core.navigation.LedgerLeafNavHost
import com.ledgerleaf.core.ui.theme.LedgerLeafTheme
import com.ledgerleaf.feature.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val preferences by settingsViewModel.preferences.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val dark = when (preferences.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            LedgerLeafTheme(darkTheme = dark) { LedgerLeafNavHost() }
        }
    }
}
