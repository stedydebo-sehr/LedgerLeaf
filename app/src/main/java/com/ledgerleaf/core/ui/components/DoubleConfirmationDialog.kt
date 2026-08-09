package com.ledgerleaf.core.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun DoubleConfirmationDialog(
    visible: Boolean,
    itemDescription: String,
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit
) {
    if (!visible) return

    var confirmationStep by remember(visible) {
        mutableIntStateOf(1)
    }

    val isFinalStep = confirmationStep == 2

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isFinalStep) "Confirm permanent action" else "Are you sure?"
            )
        },
        text = {
            Text(
                if (isFinalStep) {
                    "This will affect $itemDescription. Confirm again to continue."
                } else {
                    "You are about to change $itemDescription. Continue?"
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isFinalStep) {
                        onConfirmed()
                    } else {
                        confirmationStep = 2
                    }
                }
            ) {
                Text(if (isFinalStep) "Confirm" else "Continue")
            }
        }
    )
}
