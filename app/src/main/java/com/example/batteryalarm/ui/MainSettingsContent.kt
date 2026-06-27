package com.example.batteryalarm.ui

import androidx.annotation.StringRes
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
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme
import kotlin.math.min
import kotlin.math.sqrt

private val AlarmToggleButtonSize = 200.dp
private val EnabledToggleTextOffsetRatio = sqrt(3f) / 12f

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
    onAlarmToggleClick: () -> Unit,
    onTestAlarmClick: () -> Unit,
) {
    val toggleContentDescription = stringResource(uiState.toggleContentDescriptionRes)

    AlarmStatusText(statusTextRes = uiState.statusTextRes)
    Button(
        onClick = onAlarmToggleClick,
        modifier = Modifier
            .size(AlarmToggleButtonSize)
            .semantics {
                contentDescription = toggleContentDescription
            },
        shape = if (uiState.isEnabled) AlarmDisableTriangleShape else CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = uiState.toggleButtonColor,
            contentColor = Color.White,
        ),
    ) {
        Text(
            modifier = Modifier.offset(
                y = if (uiState.isEnabled) {
                    AlarmToggleButtonSize * EnabledToggleTextOffsetRatio
                } else {
                    0.dp
                },
            ),
            text = stringResource(uiState.toggleButtonTextRes),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )
    }
    Button(
        onClick = onTestAlarmClick,
        enabled = !uiState.isTestAlarmPending,
    ) {
        Text(text = stringResource(uiState.testAlarmButtonTextRes))
    }
}

@Composable
private fun AlarmStatusText(@StringRes statusTextRes: Int) {
    val statusHtml = stringResource(statusTextRes)
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
