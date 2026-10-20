package com.example.batteryalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
 * Nudge where the hero bottom sits relative to the screen midline.
 * Negative = higher, positive = lower. Use dp, not percent.
 */
private val HeroBottomOffsetY = 0.dp

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

    val backgroundColor = MaterialTheme.colorScheme.background
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = backgroundColor,
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor),
        ) {
            val density = LocalDensity.current
            val screenHeightPx = with(density) { maxHeight.toPx() }
            val offsetPx = with(density) { HeroBottomOffsetY.toPx() }
            val scrollState = rememberScrollState()
            var heroHeightPx by remember { mutableIntStateOf(0) }

            // Pin the bottom of the hero (title + card) to the vertical midline.
            // Secondary actions hang below that anchor.
            val topSpacerPx = (screenHeightPx / 2f - heroHeightPx + offsetPx).coerceAtLeast(0f)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = MainScreenHorizontalPadding)
                    .testTag(MainScreenTestTags.SCROLL_CONTAINER),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(with(density) { topSpacerPx.toDp() }))

                MainSettingsHeroContent(
                    uiState = uiState,
                    onAlarmToggleClick = onAlarmToggleClick,
                    modifier = Modifier.onSizeChanged { heroHeightPx = it.height },
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

@Preview(showBackground = true, name = "Main disabled light")
@Composable
private fun MainScreenDisabledPreview() {
    BatteryAlarmTheme(dynamicColor = false) {
        MainScreen(
            uiState = MainUiState.from(alarmEnabled = false, isTestAlarmPending = false),
            snackbarHostState = remember { SnackbarHostState() },
            onAlarmToggleClick = {},
            onTestAlarmClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Main enabled light")
@Composable
private fun MainScreenEnabledPreview() {
    BatteryAlarmTheme(dynamicColor = false) {
        MainScreen(
            uiState = MainUiState.from(alarmEnabled = true, isTestAlarmPending = false),
            snackbarHostState = remember { SnackbarHostState() },
            onAlarmToggleClick = {},
            onTestAlarmClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Main enabled dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainScreenEnabledDarkPreview() {
    BatteryAlarmTheme(darkTheme = true, dynamicColor = false) {
        MainScreen(
            uiState = MainUiState.from(alarmEnabled = true, isTestAlarmPending = false),
            snackbarHostState = remember { SnackbarHostState() },
            onAlarmToggleClick = {},
            onTestAlarmClick = {},
        )
    }
}
