package com.example.batteryalarm.ui

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.text.HtmlCompat
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.batteryalarm.R
import com.example.batteryalarm.ui.theme.AlarmDisableRed
import com.example.batteryalarm.ui.theme.AlarmEnableGreen
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme
import kotlin.math.min
import kotlin.math.sqrt

private val AlarmToggleButtonSize = 200.dp

private val AlarmDisableTriangleShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    val cornerRadius = min(width, height) * 0.1f
    val triangleHeight = width * sqrt(3f) / 2f
    val verticalInset = (height - triangleHeight) / 2f
    val vertices = listOf(
        Offset(width / 2f, verticalInset),
        Offset(width, verticalInset + triangleHeight),
        Offset(0f, verticalInset + triangleHeight),
    )

    vertices.forEachIndexed { index, vertex ->
        val previous = vertices[(index + vertices.size - 1) % vertices.size]
        val next = vertices[(index + 1) % vertices.size]

        val toPrevious = previous - vertex
        val toNext = next - vertex
        val previousLength = toPrevious.getDistance()
        val nextLength = toNext.getDistance()

        val radiusOnPrevious = min(cornerRadius, previousLength / 2f)
        val radiusOnNext = min(cornerRadius, nextLength / 2f)

        val cornerStart = vertex + toPrevious / previousLength * radiusOnPrevious
        val cornerEnd = vertex + toNext / nextLength * radiusOnNext

        if (index == 0) {
            moveTo(cornerStart.x, cornerStart.y)
        } else {
            lineTo(cornerStart.x, cornerStart.y)
        }
        quadraticTo(vertex.x, vertex.y, cornerEnd.x, cornerEnd.y)
    }
    close()
}

@Composable
fun MainSettingsContent(
    uiState: MainUiState,
    onAlarmEnabledChange: (Boolean) -> Unit,
    onTestAlarmClick: () -> Unit,
) {
    val isAlarmEnabled = uiState.isAlarmEnabled
    val toggleContentDescription = if (isAlarmEnabled) {
        stringResource(R.string.disable_battery_alarm)
    } else {
        stringResource(R.string.enable_battery_alarm)
    }

    AlarmStatusText(isAlarmEnabled = isAlarmEnabled)
    Button(
        onClick = {
            onAlarmEnabledChange(!isAlarmEnabled)
        },
        modifier = Modifier
            .size(AlarmToggleButtonSize)
            .semantics {
                contentDescription = toggleContentDescription
            },
        shape = if (isAlarmEnabled) AlarmDisableTriangleShape else CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isAlarmEnabled) AlarmDisableRed else AlarmEnableGreen,
            contentColor = Color.White,
        ),
    ) {
        Text(
            modifier = Modifier.offset(
                y = if (isAlarmEnabled) {
                    AlarmToggleButtonSize * (sqrt(3f) / 12f)
                } else {
                    0.dp
                },
            ),
            text = if (isAlarmEnabled) {
                stringResource(R.string.alarm_toggle_off)
            } else {
                stringResource(R.string.alarm_toggle_on)
            },
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )
    }
    Button(
        onClick = onTestAlarmClick,
        enabled = !uiState.isTestAlarmPending,
    ) {
        Text(text = stringResource(R.string.test_the_sound))
    }
}

@Composable
private fun AlarmStatusText(isAlarmEnabled: Boolean) {
    val statusHtml = stringResource(
        if (isAlarmEnabled) {
            R.string.alarm_status_enabled
        } else {
            R.string.alarm_status_disabled
        },
    )
    val statusText = remember(statusHtml) {
        buildAnnotatedString {
            append(HtmlCompat.fromHtml(statusHtml, HtmlCompat.FROM_HTML_MODE_LEGACY))
        }
    }

    Text(text = statusText)
}

@Preview(showBackground = true)
@Composable
private fun MainSettingsContentDisabledPreview() {
    BatteryAlarmTheme {
        MainSettingsContent(
            uiState = MainUiState(isAlarmEnabled = false),
            onAlarmEnabledChange = {},
            onTestAlarmClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainSettingsContentEnabledPreview() {
    BatteryAlarmTheme {
        MainSettingsContent(
            uiState = MainUiState(isAlarmEnabled = true),
            onAlarmEnabledChange = {},
            onTestAlarmClick = {},
        )
    }
}
