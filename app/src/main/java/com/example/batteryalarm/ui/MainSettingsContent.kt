package com.example.batteryalarm.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.batteryalarm.R
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme
import com.example.batteryalarm.ui.theme.BrandGreen
import com.example.batteryalarm.ui.theme.BrandGreenLight
import com.example.batteryalarm.ui.theme.ProtectionOffAccent
import com.example.batteryalarm.ui.theme.ProtectionOffBackground
import com.example.batteryalarm.ui.theme.ProtectionOffBorder
import com.example.batteryalarm.ui.theme.ProtectionOffDropShadow
import com.example.batteryalarm.ui.theme.ProtectionOffIconBackground
import com.example.batteryalarm.ui.theme.StatusCardDropShadow
import com.example.batteryalarm.ui.theme.TextDark
import com.example.batteryalarm.ui.theme.TextMuted

private const val CardTransitionDurationMillis = 400
private const val ContentFadeDurationMillis = 300
private const val ShieldGlyphAnimationDurationMillis = 600

private const val ShieldPathData =
    "M12,1L3,5v6c0,5.55 3.84,10.74 9,12 5.16,-1.26 9,-6.45 9,-12L21,5z"
private const val ShieldViewportSize = 24f
private const val ShieldSizeFraction = 36f / 64f
private const val ShieldGlyphStrokeWidth = 2.4f
private const val SlashDiagonalFraction = 0.70f

private const val MainTitleFontSizeSp = 28f
private const val MainTitleLineHeightSp = 35f
private const val MainSubtitleFontSizeSp = 10f
private const val MainSubtitleLineHeightSp = 15f
private val HeaderTitleSubtitleSpacing = 5.dp

private val StatusCardColorAnimationSpec =
    tween<Color>(durationMillis = CardTransitionDurationMillis, easing = FastOutSlowInEasing)
private val StatusCardBorderEnabled = Color(0xFFE0E0E0)

@Composable
fun MainSettingsContent(
    uiState: MainUiState,
    onAlarmToggleClick: () -> Unit,
    onTestAlarmClick: () -> Unit,
) {
    val isEnabled = uiState.isEnabled
    val toggleContentDescription = stringResource(uiState.toggleContentDescriptionRes)
    val testAlarmButtonText = uiState.testAlarmSecondsRemaining?.let { secondsRemaining ->
        stringResource(R.string.test_alarm_countdown, secondsRemaining)
    } ?: stringResource(R.string.test_the_sound)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        HeaderSection()

        ProtectionToggleCard(
            isEnabled = isEnabled,
            contentDescription = toggleContentDescription,
            onClick = onAlarmToggleClick,
        )

        TestAlarmButtonSection(
            uiState = uiState,
            buttonText = testAlarmButtonText,
            onClick = onTestAlarmClick,
        )
    }
}

@Composable
private fun TestAlarmButtonSection(
    uiState: MainUiState,
    buttonText: String,
    onClick: () -> Unit,
) {
    val hintModifier = if (uiState.isTestAlarmPending) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {}
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TextButton(
            onClick = onClick,
            enabled = !uiState.isTestAlarmPending,
        ) {
            Text(
                text = buttonText,
                color = if (uiState.isTestAlarmPending) TextMuted else BrandGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Text(
            modifier = hintModifier,
            text = stringResource(R.string.test_alarm_lock_screen_hint),
            color = if (uiState.isTestAlarmPending) TextMuted else Color.Transparent,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HeaderTitleSubtitleSpacing),
    ) {
        Text(
            text = stringResource(R.string.main_title),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = MainTitleFontSizeSp.sp,
                lineHeight = MainTitleLineHeightSp.sp,
            ),
            fontWeight = FontWeight.Bold,
            color = TextDark,
        )
        Text(
            text = stringResource(R.string.main_subtitle),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = MainSubtitleFontSizeSp.sp,
                lineHeight = MainSubtitleLineHeightSp.sp,
            ),
            color = TextMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProtectionToggleCard(
    isEnabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val cardShape = RoundedCornerShape(16.dp)
    val cardBackground by animateColorAsState(
        targetValue = if (isEnabled) Color.White else ProtectionOffBackground,
        animationSpec = StatusCardColorAnimationSpec,
        label = "card_background",
    )
    val cardBorder by animateColorAsState(
        targetValue = if (isEnabled) StatusCardBorderEnabled else ProtectionOffBorder,
        animationSpec = StatusCardColorAnimationSpec,
        label = "card_border",
    )
    val shadowColor by animateColorAsState(
        targetValue = if (isEnabled) StatusCardDropShadow else ProtectionOffDropShadow,
        animationSpec = StatusCardColorAnimationSpec,
        label = "card_shadow",
    )
    val shadowRadius by animateDpAsState(
        targetValue = if (isEnabled) 4.dp else 6.dp,
        animationSpec = tween(durationMillis = CardTransitionDurationMillis, easing = FastOutSlowInEasing),
        label = "card_shadow_radius",
    )
    val shadowOffsetY by animateDpAsState(
        targetValue = if (isEnabled) 1.dp else 2.dp,
        animationSpec = tween(durationMillis = CardTransitionDurationMillis, easing = FastOutSlowInEasing),
        label = "card_shadow_offset",
    )
    val cardShadow = Shadow(
        radius = shadowRadius,
        color = shadowColor,
        offset = DpOffset(x = 0.dp, y = shadowOffsetY),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
                this.role = Role.Switch
            }
            .padding(vertical = 3.dp, horizontal = 1.dp)
            .dropShadow(
                shape = cardShape,
                shadow = cardShadow,
            )
            .background(
                color = cardBackground,
                shape = cardShape,
            )
            .border(
                width = 1.dp,
                color = cardBorder,
                shape = cardShape,
            )
            .clip(cardShape)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProtectionShieldIcon(
                isEnabled = isEnabled,
                modifier = Modifier.size(64.dp),
            )

            AnimatedContent(
                targetState = isEnabled,
                transitionSpec = {
                    fadeIn(tween(ContentFadeDurationMillis)) togetherWith
                        fadeOut(tween(ContentFadeDurationMillis))
                },
                label = "text_transition",
                modifier = Modifier.weight(1f),
            ) { enabled ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(
                            if (enabled) R.string.protection_on else R.string.protection_off,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                    )
                    Text(
                        text = stringResource(
                            if (enabled) R.string.alarm_is_active else R.string.protection_off_subtitle,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        minLines = 2,
                        maxLines = 2,
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { onClick() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BrandGreen,
                    checkedBorderColor = BrandGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = ProtectionOffIconBackground,
                    uncheckedBorderColor = ProtectionOffBorder,
                ),
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
    }
}

private fun lerpOffset(a: Offset, b: Offset, t: Float): Offset =
    Offset(lerp(a.x, b.x, t), lerp(a.y, b.y, t))

@Composable
private fun ProtectionShieldIcon(
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val iconBackground by animateColorAsState(
        targetValue = if (isEnabled) BrandGreenLight else ProtectionOffIconBackground,
        animationSpec = StatusCardColorAnimationSpec,
        label = "icon_background",
    )
    val shieldColor by animateColorAsState(
        targetValue = if (isEnabled) BrandGreen else ProtectionOffAccent,
        animationSpec = StatusCardColorAnimationSpec,
        label = "shield_color",
    )
    val glyphProgress by animateFloatAsState(
        targetValue = if (isEnabled) 0f else 1f,
        animationSpec = if (isEnabled) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            )
        } else {
            tween(durationMillis = ShieldGlyphAnimationDurationMillis, easing = FastOutSlowInEasing)
        },
        label = "shield_glyph_progress",
    )

    val shieldPath = remember {
        PathParser().parsePathString(ShieldPathData).toPath()
    }

    Box(
        modifier = modifier
            .background(
                color = iconBackground,
                shape = CircleShape,
            )
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = size.minDimension
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = diameter / 2f
            val shieldScale = diameter * ShieldSizeFraction / ShieldViewportSize
            val strokeWidth = ShieldGlyphStrokeWidth * shieldScale

            withTransform({
                translate(cx - 12f * shieldScale, cy - 12f * shieldScale)
                scale(shieldScale, shieldScale, pivot = Offset.Zero)
            }) {
                drawPath(shieldPath, color = shieldColor)
            }

            val shieldOff = cx - 12f * shieldScale
            fun shieldPt(x: Float, y: Float) =
                Offset(shieldOff + x * shieldScale, shieldOff + y * shieldScale)

            val checkP1 = shieldPt(7.5f, 12.5f)
            val checkP2 = shieldPt(10.8f, 15.8f)
            val checkP3 = shieldPt(16.5f, 9.2f)

            val d = r * SlashDiagonalFraction
            val slashP1 = Offset(cx - d, cy - d)
            val slashP2 = Offset(cx, cy)
            val slashP3 = Offset(cx + d, cy + d)

            val t = glyphProgress
            val p1 = lerpOffset(checkP1, slashP1, t)
            val p2 = lerpOffset(checkP2, slashP2, t)
            val p3 = lerpOffset(checkP3, slashP3, t)

            val morphPath = Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
            }

            drawPath(
                path = morphPath,
                color = Color.White,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainSettingsContentDisabledPreview() {
    BatteryAlarmTheme {
        MainSettingsContent(
            uiState = MainUiState.from(alarmEnabled = false, isTestAlarmPending = false),
            onAlarmToggleClick = {},
            onTestAlarmClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainSettingsContentEnabledPreview() {
    BatteryAlarmTheme {
        MainSettingsContent(
            uiState = MainUiState.from(alarmEnabled = true, isTestAlarmPending = false),
            onAlarmToggleClick = {},
            onTestAlarmClick = {},
        )
    }
}
