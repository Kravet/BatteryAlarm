package com.example.batteryalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.batteryalarm.R
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme
import com.example.batteryalarm.ui.theme.BrandGreen
import com.example.batteryalarm.ui.theme.BrandGreenLight
import com.example.batteryalarm.ui.theme.TextDark
import com.example.batteryalarm.ui.theme.TextMuted

@Composable
fun UpdateAvailableButton(
    update: AppUpdateUiModel,
    onUpdateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val title = stringResource(R.string.update_available_title, update.versionName)
    val body = stringResource(R.string.update_available_body)
    val action = stringResource(R.string.update_action_update)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Button
            }
            .clip(shape)
            .background(color = BrandGreenLight, shape = shape)
            .border(width = 1.dp, color = BrandGreen.copy(alpha = 0.25f), shape = shape)
            .clickable(onClick = onUpdateClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
            )
        }
        Text(
            text = action,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = BrandGreen,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UpdateAvailableButtonPreview() {
    BatteryAlarmTheme {
        UpdateAvailableButton(
            update = AppUpdateUiModel(
                versionName = "1.1.0",
                releaseNotes = null,
                releasePageUrl = "https://github.com/example/repo/releases/tag/v1.1.0",
            ),
            onUpdateClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
