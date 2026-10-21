package com.example.batteryalarm.ui

import android.Manifest
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.res.stringResource
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
        if (redirectToAlarmIfActive()) {
            return
        }
        enableEdgeToEdge()
        setContent {
            BatteryAlarmTheme {
                val uiState by viewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val notificationPermissionDeniedMessage = stringResource(R.string.notification_permission_denied)
                val alarmSettingsSaveFailedMessage = stringResource(R.string.alarm_settings_save_failed)
                val testAlarmStartFailedMessage = stringResource(R.string.test_alarm_start_failed)

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { granted ->
                    if (granted) {
                        viewModel.onAlarmToggleClick()
                    } else {
                        viewModel.onNotificationPermissionDenied()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = notificationPermissionDeniedMessage,
                            )
                        }
                    }
                }

                LaunchedEffect(viewModel, alarmSettingsSaveFailedMessage, testAlarmStartFailedMessage) {
                    viewModel.sideEffects.collect { sideEffect ->
                        when (sideEffect) {
                            MainSideEffect.ShowAlarmSettingsChangeFailed -> {
                                snackbarHostState.showSnackbar(
                                    message = alarmSettingsSaveFailedMessage,
                                )
                            }

                            MainSideEffect.ShowTestAlarmStartFailed -> {
                                snackbarHostState.showSnackbar(
                                    message = testAlarmStartFailedMessage,
                                )
                            }
                        }
                    }
                }

                MainScreen(
                    uiState = uiState,
                    snackbarHostState = snackbarHostState,
                    onAlarmToggleClick = {
                        if (!uiState.isPrivacySheetVisible) {
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
                        }
                    },
                    onTestAlarmClick = viewModel::onTestAlarmClick,
                    onUpdateClick = viewModel::onUpdateClick,
                    onPrivacyDiagnosticsClick = viewModel::onPrivacyDiagnosticsClick,
                    onPrivacySheetDismiss = viewModel::onPrivacySheetDismiss,
                    onAllowBothPrivacy = viewModel::onAllowBothPrivacy,
                    onDeclineBothPrivacy = viewModel::onDeclineBothPrivacy,
                    onSaveCustomPrivacyChoices = viewModel::onSaveCustomPrivacyChoices,
                    onAnalyticsConsentChange = viewModel::onAnalyticsConsentChange,
                    onCrashlyticsConsentChange = viewModel::onCrashlyticsConsentChange,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        redirectToAlarmIfActive()
    }

    private fun redirectToAlarmIfActive(): Boolean {
        if (!viewModel.isAlarmActive()) {
            return false
        }
        startActivity(AlarmActivity.createAppLaunchAlarmIntent(this))
        finish()
        return true
    }

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
            )
    }
}
