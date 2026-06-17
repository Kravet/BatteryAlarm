package com.example.batteryalarm.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

                            MainSideEffect.ShowTestAlarmStartFailed -> {
                                snackbarHostState.showSnackbar(
                                    message = "Could not start test alarm. Please try again.",
                                )
                            }

                            MainSideEffect.LaunchAlarmScreen -> {
                                startActivity(createAlarmIntent(this@MainActivity))
                            }
                        }
                    }
                }

                MainScreen(
                    uiState = uiState,
                    snackbarHostState = snackbarHostState,
                    onAlarmEnabledChange = viewModel::onAlarmEnabledChange,
                    onTestAlarmClick = viewModel::onTestAlarmClick,
                    onStopAlarmClick = viewModel::onStopAlarmClick,
                )
            }
        }
        viewModel.onAlarmIntentReceived(isAlarmIntent(intent))
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncAlarmScreenVisibility()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.onAlarmIntentReceived(isAlarmIntent(intent))
    }

    companion object {
        private const val ACTION_SHOW_ALARM = "com.example.batteryalarm.action.SHOW_ALARM"

        fun createAlarmIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
            .setAction(ACTION_SHOW_ALARM)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
            )

        fun isAlarmIntent(intent: Intent?): Boolean = intent?.action == ACTION_SHOW_ALARM
    }
}
