package com.example.batteryalarm.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme

@Composable
fun MainSettingsContent(
    uiState: MainUiState,
    onAlarmEnabledChange: (Boolean) -> Unit,
    onTestAlarmClick: () -> Unit,
) {
    Text(
        text = if (uiState.isAlarmEnabled) {
            "Battery alarm is enabled"
        } else {
            "Battery alarm is disabled"
        },
    )
    Button(
        onClick = {
            onAlarmEnabledChange(!uiState.isAlarmEnabled)
        },
    ) {
        Text(
            text = if (uiState.isAlarmEnabled) {
                "Disable battery alarm"
            } else {
                "Enable battery alarm"
            },
        )
    }
    Button(onClick = onTestAlarmClick) {
        Text(text = "Test")
    }
}

@Preview(showBackground = true)
@Composable
private fun MainSettingsContentDisabledPreview() {
    BatteryAlarmTheme {
        MainSettingsContent(
            uiState = MainUiState(isAlarmEnabled = false),
            onAlarmEnabledChange = {},
            onTestAlarmClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainSettingsContentEnabledPreview() {
    BatteryAlarmTheme {
        MainSettingsContent(
            uiState = MainUiState(isAlarmEnabled = true),
            onAlarmEnabledChange = {},
            onTestAlarmClick = {},
        )
    }
}
