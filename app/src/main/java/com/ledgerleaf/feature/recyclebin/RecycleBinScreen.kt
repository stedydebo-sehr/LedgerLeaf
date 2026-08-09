package com.ledgerleaf.feature.recyclebin

import androidx.compose.runtime.Composable
import com.ledgerleaf.core.ui.components.FoundationPlaceholderScreen

@Composable
fun RecycleBinScreen() {
    FoundationPlaceholderScreen(
        title = "Recycle Bin",
        message = "Deleted records will remain recoverable here for six months."
    )
}
