package com.example.batteryalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

/**
 * Nudge the anchored block from the geometric center of the full screen.
 * Negative = higher, positive = lower. Use dp, not percent.
 */
private val HeroCenterOffsetY = 0.dp

private val MainScreenHorizontalPadding = 24.dp
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
            val screenHeightPx = with(density) { maxHeight.toPx() }
            val offsetPx = with(density) { HeroCenterOffsetY.toPx() }
            val scrollState = rememberScrollState()
            var contentHeightPx by remember { mutableIntStateOf(0) }

            // verticalScroll forces max-height viewport, so Alignment.Center cannot work.
            // Equal spacers center the measured content against the real screen height.
            val extraSpacePx = (screenHeightPx - contentHeightPx).coerceAtLeast(0f)
            val topSpacerPx = (extraSpacePx / 2f + offsetPx).coerceAtLeast(0f)
            val bottomSpacerPx = (extraSpacePx / 2f - offsetPx).coerceAtLeast(0f)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .testTag(MainScreenTestTags.SCROLL_CONTAINER),
            ) {
                Spacer(modifier = Modifier.height(with(density) { topSpacerPx.toDp() }))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MainScreenHorizontalPadding)
                        .onSizeChanged { contentHeightPx = it.height },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(HeroToSecondarySpacing),
                ) {
                    MainSettingsHeroContent(
                        uiState = uiState,
                        onAlarmToggleClick = onAlarmToggleClick,
                    )
                    MainSettingsSecondaryContent(
                        uiState = uiState,
                        onTestAlarmClick = onTestAlarmClick,
                        onUpdateClick = onUpdateClick,
                        onPrivacyDiagnosticsClick = onPrivacyDiagnosticsClick,
                    )
                }

                Spacer(modifier = Modifier.height(with(density) { bottomSpacerPx.toDp() }))
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
