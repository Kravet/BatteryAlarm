package com.example.batteryalarm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme

@Composable
fun MainScreen(
    uiState: MainUiState,
    snackbarHostState: SnackbarHostState,
    onAlarmEnabledChange: (Boolean) -> Unit,
    onTestAlarmClick: () -> Unit,
    onStopAlarmClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (uiState.isAlarmScreenVisible) {
                    LowBatteryAlarmScreen(onStopAlarmClick = onStopAlarmClick)
                } else {
                    MainSettingsContent(
                        uiState = uiState,
                        onAlarmEnabledChange = onAlarmEnabledChange,
                        onTestAlarmClick = onTestAlarmClick,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    BatteryAlarmTheme {
        MainScreen(
            uiState = MainUiState(isAlarmEnabled = false),
            snackbarHostState = remember { SnackbarHostState() },
            onAlarmEnabledChange = {},
            onTestAlarmClick = {},
            onStopAlarmClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenAlarmPreview() {
    BatteryAlarmTheme {
        MainScreen(
            uiState = MainUiState(
                isAlarmEnabled = true,
                isAlarmScreenVisible = true,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAlarmEnabledChange = {},
            onTestAlarmClick = {},
            onStopAlarmClick = {},
        )
    }
}
