package com.example.batteryalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme

@Composable
fun MainScreen(
    uiState: MainUiState,
    snackbarHostState: SnackbarHostState,
    onAlarmToggleClick: () -> Unit,
    onTestAlarmClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MainSettingsContent(
                uiState = uiState,
                onAlarmToggleClick = onAlarmToggleClick,
                onTestAlarmClick = onTestAlarmClick,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenDisabledPreview() {
    BatteryAlarmTheme {
        MainScreen(
            uiState = MainUiState.from(alarmEnabled = false, isTestAlarmPending = false),
            snackbarHostState = remember { SnackbarHostState() },
            onAlarmToggleClick = {},
            onTestAlarmClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenEnabledPreview() {
    BatteryAlarmTheme {
        MainScreen(
            uiState = MainUiState.from(alarmEnabled = true, isTestAlarmPending = false),
            snackbarHostState = remember { SnackbarHostState() },
            onAlarmToggleClick = {},
            onTestAlarmClick = {},
        )
    }
}
