package com.ledgerleaf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ledgerleaf.core.navigation.LedgerLeafNavHost
import com.ledgerleaf.core.ui.theme.LedgerLeafTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LedgerLeafTheme {
                LedgerLeafNavHost()
            }
        }
    }
}
