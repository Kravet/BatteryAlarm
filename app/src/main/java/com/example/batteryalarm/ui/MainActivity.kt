package com.example.batteryalarm.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BatteryAlarmTheme {
                val uiState by viewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(viewModel) {
                    viewModel.sideEffects.collect { sideEffect ->
                        when (sideEffect) {
                            MainSideEffect.ShowAlarmSettingsChangeFailed -> {
                                snackbarHostState.showSnackbar(
                                    message = "Could not save battery alarm setting. Please try again.",
                                )
                            }
                        }
                    }
                }

                MainScreen(
                    uiState = uiState,
                    snackbarHostState = snackbarHostState,
                    onAlarmEnabledChange = viewModel::onAlarmEnabledChange,
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    uiState: MainUiState,
    snackbarHostState: SnackbarHostState,
    onAlarmEnabledChange: (Boolean) -> Unit,
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
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenEnabledPreview() {
    BatteryAlarmTheme {
        MainScreen(
            uiState = MainUiState(isAlarmEnabled = true),
            snackbarHostState = remember { SnackbarHostState() },
            onAlarmEnabledChange = {},
        )
    }
}
