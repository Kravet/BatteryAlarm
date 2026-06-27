package com.example.batteryalarm.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.batteryalarm.R
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { granted ->
                    if (granted) {
                        viewModel.onAlarmToggleClick()
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.notification_permission_denied),
                            )
                        }
                    }
                }

                LaunchedEffect(viewModel) {
                    viewModel.sideEffects.collect { sideEffect ->
                        when (sideEffect) {
                            MainSideEffect.ShowAlarmSettingsChangeFailed -> {
                                snackbarHostState.showSnackbar(
                                    message = "Could not save battery alarm setting. Please try again.",
                                )
                            }

                            MainSideEffect.ShowTestAlarmStartFailed -> {
                                snackbarHostState.showSnackbar(
                                    message = "Could not start test alarm. Please try again.",
                                )
                            }
                        }
                    }
                }

                MainScreen(
                    uiState = uiState,
                    snackbarHostState = snackbarHostState,
                    onAlarmToggleClick = {
                        if (
                            !uiState.isEnabled &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS,
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.onAlarmToggleClick()
                        }
                    },
                    onTestAlarmClick = viewModel::onTestAlarmClick,
                )
            }
        }
    }
}
