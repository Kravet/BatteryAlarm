package com.example.batteryalarm.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.batteryalarm.R

@Composable
fun UpdateAvailableDialog(
    update: AppUpdateUiModel,
    onUpdateClick: () -> Unit,
    onLaterClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onLaterClick,
        title = {
            Text(text = stringResource(R.string.update_available_title, update.versionName))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(text = stringResource(R.string.update_available_body))
                val notes = update.releaseNotes
                if (!notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = stringResource(R.string.update_whats_new))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = notes)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onUpdateClick) {
                Text(text = stringResource(R.string.update_action_update))
            }
        },
        dismissButton = {
            TextButton(onClick = onLaterClick) {
                Text(text = stringResource(R.string.update_action_later))
            }
        },
    )
}
