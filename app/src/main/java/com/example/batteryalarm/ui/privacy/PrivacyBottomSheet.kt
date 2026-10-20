package com.example.batteryalarm.ui.privacy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.batteryalarm.R
import com.example.batteryalarm.privacy.PrivacyPolicyLinks
import com.example.batteryalarm.ui.theme.BatteryAlarmThemeTokens

object PrivacyBottomSheetTestTags {
    const val SHEET = "privacy_bottom_sheet"
    const val TITLE = "privacy_sheet_title"
    const val ALLOW_BOTH = "privacy_allow_both"
    const val DECLINE_BOTH = "privacy_decline_both"
    const val CUSTOMIZE = "privacy_customize_choices"
    const val SAVE_CHOICES = "privacy_save_choices"
    const val ANALYTICS_SWITCH = "privacy_analytics_switch"
    const val CRASHLYTICS_SWITCH = "privacy_crashlytics_switch"
    const val PRIVACY_POLICY_LINK = "privacy_policy_link"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyBottomSheet(
    mode: PrivacySheetMode,
    analyticsEnabled: Boolean,
    crashlyticsEnabled: Boolean,
    onDismiss: () -> Unit,
    onAllowBoth: () -> Unit,
    onDeclineBoth: () -> Unit,
    onSaveCustomChoices: (analyticsEnabled: Boolean, crashlyticsEnabled: Boolean) -> Unit,
    onAnalyticsChange: (Boolean) -> Unit,
    onCrashlyticsChange: (Boolean) -> Unit,
    onPrivacyPolicyClick: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uriHandler = LocalUriHandler.current
    val openPrivacyPolicy = onPrivacyPolicyClick
        ?: { uriHandler.openUri(PrivacyPolicyLinks.PUBLIC_URL) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag(PrivacyBottomSheetTestTags.SHEET),
    ) {
        when (mode) {
            PrivacySheetMode.ONBOARDING -> OnboardingPrivacyContent(
                onAllowBoth = onAllowBoth,
                onDeclineBoth = onDeclineBoth,
                onSaveCustomChoices = onSaveCustomChoices,
                onPrivacyPolicyClick = openPrivacyPolicy,
            )

            PrivacySheetMode.SETTINGS -> SettingsPrivacyContent(
                analyticsEnabled = analyticsEnabled,
                crashlyticsEnabled = crashlyticsEnabled,
                onAnalyticsChange = onAnalyticsChange,
                onCrashlyticsChange = onCrashlyticsChange,
                onPrivacyPolicyClick = openPrivacyPolicy,
            )
        }
    }
}

@Composable
private fun OnboardingPrivacyContent(
    onAllowBoth: () -> Unit,
    onDeclineBoth: () -> Unit,
    onSaveCustomChoices: (Boolean, Boolean) -> Unit,
    onPrivacyPolicyClick: () -> Unit,
) {
    var customizeExpanded by remember { mutableStateOf(false) }
    var customAnalytics by remember { mutableStateOf(false) }
    var customCrashlytics by remember { mutableStateOf(false) }
    val brandColors = BatteryAlarmThemeTokens.colors
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "\uD83D\uDD12 ${stringResource(R.string.privacy_onboarding_title)}",
            modifier = Modifier.testTag(PrivacyBottomSheetTestTags.TITLE),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = onSurface,
        )

        if (!customizeExpanded) {
            FeatureSummary(
                title = stringResource(R.string.privacy_analytics_title),
                body = stringResource(R.string.privacy_analytics_body),
                emoji = "\uD83D\uDCCA",
            )
            FeatureSummary(
                title = stringResource(R.string.privacy_crashlytics_title),
                body = stringResource(R.string.privacy_crashlytics_body),
                emoji = "\uD83D\uDC1B",
            )
            Text(
                text = stringResource(R.string.privacy_disclaimer),
                color = onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
            PrivacyPolicyLink(onClick = onPrivacyPolicyClick)
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onAllowBoth,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(PrivacyBottomSheetTestTags.ALLOW_BOTH),
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandColors.brandGreenFill,
                    contentColor = brandColors.onBrandGreenFill,
                ),
            ) {
                Text(text = stringResource(R.string.privacy_allow_both))
            }
            OutlinedButton(
                onClick = onDeclineBoth,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(PrivacyBottomSheetTestTags.DECLINE_BOTH),
            ) {
                Text(text = stringResource(R.string.privacy_decline_both), color = onSurface)
            }
            TextButton(
                onClick = { customizeExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(PrivacyBottomSheetTestTags.CUSTOMIZE),
            ) {
                Text(
                    text = stringResource(R.string.privacy_customize_choices),
                    color = brandColors.brandGreen,
                    fontWeight = FontWeight.Medium,
                )
            }
        } else {
            Text(
                text = stringResource(R.string.privacy_disclaimer),
                color = onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
            ConsentSwitchRow(
                title = stringResource(R.string.privacy_analytics_title),
                body = stringResource(R.string.privacy_analytics_body),
                checked = customAnalytics,
                onCheckedChange = { customAnalytics = it },
                switchTestTag = PrivacyBottomSheetTestTags.ANALYTICS_SWITCH,
            )
            ConsentSwitchRow(
                title = stringResource(R.string.privacy_crashlytics_title),
                body = stringResource(R.string.privacy_crashlytics_body),
                checked = customCrashlytics,
                onCheckedChange = { customCrashlytics = it },
                switchTestTag = PrivacyBottomSheetTestTags.CRASHLYTICS_SWITCH,
            )
            PrivacyPolicyLink(onClick = onPrivacyPolicyClick)
            Button(
                onClick = { onSaveCustomChoices(customAnalytics, customCrashlytics) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(PrivacyBottomSheetTestTags.SAVE_CHOICES),
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandColors.brandGreenFill,
                    contentColor = brandColors.onBrandGreenFill,
                ),
            ) {
                Text(text = stringResource(R.string.privacy_save_choices))
            }
        }
    }
}

@Composable
private fun SettingsPrivacyContent(
    analyticsEnabled: Boolean,
    crashlyticsEnabled: Boolean,
    onAnalyticsChange: (Boolean) -> Unit,
    onCrashlyticsChange: (Boolean) -> Unit,
    onPrivacyPolicyClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "\uD83D\uDD12 ${stringResource(R.string.privacy_diagnostics)}",
            modifier = Modifier.testTag(PrivacyBottomSheetTestTags.TITLE),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        ConsentSwitchRow(
            title = stringResource(R.string.privacy_analytics_title),
            body = stringResource(R.string.privacy_analytics_settings_body),
            checked = analyticsEnabled,
            onCheckedChange = onAnalyticsChange,
            switchTestTag = PrivacyBottomSheetTestTags.ANALYTICS_SWITCH,
        )
        ConsentSwitchRow(
            title = stringResource(R.string.privacy_crashlytics_title),
            body = stringResource(R.string.privacy_crashlytics_settings_body),
            checked = crashlyticsEnabled,
            onCheckedChange = onCrashlyticsChange,
            switchTestTag = PrivacyBottomSheetTestTags.CRASHLYTICS_SWITCH,
        )
        PrivacyPolicyLink(onClick = onPrivacyPolicyClick)
    }
}

@Composable
private fun FeatureSummary(
    title: String,
    body: String,
    emoji: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$emoji $title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    }
}

@Composable
private fun ConsentSwitchRow(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchTestTag: String,
) {
    val brandColors = BatteryAlarmThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = brandColors.onBrandGreenFill,
                checkedTrackColor = brandColors.brandGreenFill,
                checkedBorderColor = brandColors.brandGreenFill,
            ),
            modifier = Modifier.testTag(switchTestTag),
        )
    }
}

@Composable
private fun PrivacyPolicyLink(onClick: () -> Unit) {
    Text(
        text = "${stringResource(R.string.privacy_policy)} \u2197",
        modifier = Modifier
            .testTag(PrivacyBottomSheetTestTags.PRIVACY_POLICY_LINK)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp),
        color = BatteryAlarmThemeTokens.colors.brandGreen,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        textDecoration = TextDecoration.Underline,
    )
}
