package com.ledgerleaf.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FoundationPlaceholderScreen(
    title: String,
    message: String
) {
    Scaffold(
        topBar = {
            LedgerLeafTopBar(title = title)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LedgerLeafEmptyState(
                title = title,
                message = message
            )
        }
    }
}
