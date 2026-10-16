package com.example.batteryalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.batteryalarm.privacy.PrivacyPolicyLinks
import com.example.batteryalarm.ui.privacy.PrivacyBottomSheet
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme

/** Fraction of viewport height to shift the hero anchor above geometric center. */
private const val MainContentUpwardOffsetFraction = 0.10f

private val MainScreenContentPadding = 24.dp
private val HeroToSecondarySpacing = 24.dp

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
    onPrivacyDiagnosticsClick: () -> Unit = {},
    onPrivacySheetDismiss: () -> Unit = {},
    onAllowBothPrivacy: () -> Unit = {},
    onDeclineBothPrivacy: () -> Unit = {},
    onSaveCustomPrivacyChoices: (Boolean, Boolean) -> Unit = { _, _ -> },
    onAnalyticsConsentChange: (Boolean) -> Unit = {},
    onCrashlyticsConsentChange: (Boolean) -> Unit = {},
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
            val density = LocalDensity.current
            val viewportHeight = maxHeight
            val contentViewportHeight = (viewportHeight - MainScreenContentPadding * 2)
                .coerceAtLeast(0.dp)
            val scrollState = rememberScrollState()
            var heroHeightPx by remember { mutableIntStateOf(0) }

            val topSpacer = with(density) {
                val contentViewportPx = contentViewportHeight.toPx()
                val targetHeroCenterY =
                    contentViewportPx * (0.5f - MainContentUpwardOffsetFraction)
                val topSpacerPx = (targetHeroCenterY - heroHeightPx / 2f).coerceAtLeast(0f)
                topSpacerPx.toDp()
            }

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
                        .padding(MainScreenContentPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(topSpacer))

                    MainSettingsHeroContent(
                        modifier = Modifier.onSizeChanged { heroHeightPx = it.height },
                        uiState = uiState,
                        onAlarmToggleClick = onAlarmToggleClick,
                    )

                    Spacer(modifier = Modifier.height(HeroToSecondarySpacing))

                    MainSettingsSecondaryContent(
                        uiState = uiState,
                        onTestAlarmClick = onTestAlarmClick,
                        onUpdateClick = onUpdateClick,
                        onPrivacyDiagnosticsClick = onPrivacyDiagnosticsClick,
                    )
                }
            }
        }
    }

    val privacySheetMode = uiState.privacySheetMode
    if (privacySheetMode != null) {
        PrivacyBottomSheet(
            mode = privacySheetMode,
            analyticsEnabled = uiState.analyticsEnabled,
            crashlyticsEnabled = uiState.crashlyticsEnabled,
            onDismiss = onPrivacySheetDismiss,
            onAllowBoth = onAllowBothPrivacy,
            onDeclineBoth = onDeclineBothPrivacy,
            onSaveCustomChoices = onSaveCustomPrivacyChoices,
            onAnalyticsChange = onAnalyticsConsentChange,
            onCrashlyticsChange = onCrashlyticsConsentChange,
            onPrivacyPolicyClick = openPrivacyPolicy,
        )
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
