package com.example.batteryalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.batteryalarm.privacy.PrivacyPolicyLinks
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme

/** Fraction of viewport height to shift the main block above center. */
private const val MainContentUpwardOffsetFraction = 0.10f

object MainScreenTestTags {
    const val SCROLL_CONTAINER = "main_screen_scroll_container"
}

@Composable
fun MainScreen(
    uiState: MainUiState,
    snackbarHostState: SnackbarHostState,
    onAlarmToggleClick: () -> Unit,
    onTestAlarmClick: () -> Unit,
    onUpdateClick: () -> Unit = {},
    onPrivacyPolicyClick: (() -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current
    val openPrivacyPolicy = onPrivacyPolicyClick
        ?: { uriHandler.openUri(PrivacyPolicyLinks.PUBLIC_URL) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = Color.White,
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White),
        ) {
            // Bottom padding of 2×offset moves the visual block up by [fraction] of height
            // when Arrangement.Center is applied.
            val viewportHeight = maxHeight
            val upwardShiftPadding = viewportHeight * MainContentUpwardOffsetFraction * 2
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .testTag(MainScreenTestTags.SCROLL_CONTAINER),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = viewportHeight)
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    MainSettingsContent(
                        modifier = Modifier.padding(bottom = upwardShiftPadding),
                        uiState = uiState,
                        onAlarmToggleClick = onAlarmToggleClick,
                        onTestAlarmClick = onTestAlarmClick,
                        onUpdateClick = onUpdateClick,
                        onPrivacyPolicyClick = openPrivacyPolicy,
                    )
                }
            }
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
