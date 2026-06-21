package com.example.batteryalarm.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.batteryalarm.R
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme

@Composable
fun LowBatteryAlarmScreen(
    onDismissAlarmClick: () -> Unit,
) {
    RedWarningTriangle(modifier = Modifier.size(96.dp))
    Text(
        text = stringResource(R.string.alarm_notification_title),
        style = MaterialTheme.typography.headlineSmall,
    )
    Text(
        text = stringResource(R.string.alarm_notification_body),
        style = MaterialTheme.typography.bodyLarge,
    )
    Button(onClick = onDismissAlarmClick) {
        Text(text = stringResource(R.string.dismiss))
    }
}

@Composable
private fun RedWarningTriangle(
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val triangle = Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path = triangle, color = Color(0xFFD32F2F))
        drawLine(
            color = Color.White,
            start = Offset(size.width / 2f, size.height * 0.32f),
            end = Offset(size.width / 2f, size.height * 0.62f),
            strokeWidth = size.width * 0.08f,
        )
        drawCircle(
            color = Color.White,
            radius = size.width * 0.04f,
            center = Offset(size.width / 2f, size.height * 0.78f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LowBatteryAlarmScreenPreview() {
    BatteryAlarmTheme {
        LowBatteryAlarmScreen(onDismissAlarmClick = {})
    }
}
