package com.example.batteryalarm.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.batteryalarm.R
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme

private const val HoldToStopDurationMillis = 1_000
private const val HoldCancelAnimationMillis = 180

private val AlarmRedTop = Color(0xFFFF454F)
private val AlarmRedBottom = Color(0xFFFF363F)
private val AlarmRedDark = Color(0xFFE92D38)
private val AlarmRedText = Color(0xFFFF4A50)

@Composable
fun LowBatteryAlarmScreen(
    batteryPercentage: Int,
    onDismissAlarmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clampedBatteryPercentage = batteryPercentage.coerceIn(0, 100)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AlarmRedTop, AlarmRedBottom),
                ),
            ),
    ) {
        AlarmBackgroundRings(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 36.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.65f))

            AnimatedBell(
                modifier = Modifier.size(92.dp),
            )

            Text(
                text = stringResource(R.string.alarm_screen_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                ),
                color = Color.White.copy(alpha = 0.94f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.alarm_screen_percentage, clampedBatteryPercentage),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 86.sp,
                    lineHeight = 94.sp,
                ),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            BatteryLevelIcon(
                batteryPercentage = clampedBatteryPercentage,
                modifier = Modifier
                    .width(190.dp)
                    .height(86.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.alarm_screen_body),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                ),
                color = Color.White.copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            HoldToStopButton(
                label = stringResource(R.string.stop_alarm),
                contentDescription = stringResource(R.string.hold_to_stop_alarm),
                onHoldComplete = onDismissAlarmClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 300.dp)
                    .height(72.dp),
            )
        }
    }
}

@Composable
private fun AlarmBackgroundRings(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "alarm_background_rings")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "alarm_background_pulse",
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height * 0.30f)
        val baseRadius = size.minDimension * 0.24f

        drawCircle(
            color = Color.White.copy(alpha = 0.045f),
            radius = baseRadius * (1f + pulse * 0.16f),
            center = center,
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.038f),
            radius = baseRadius * 1.55f,
            center = center,
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.026f),
            radius = baseRadius * 2.12f,
            center = center,
        )
    }
}

@Composable
private fun AnimatedBell(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "bell_animation")
    val rotation by transition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bell_rotation",
    )
    val ringProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 980, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "bell_ring_progress",
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.width * 0.045f
            val alpha = 0.42f + (1f - ringProgress) * 0.35f
            val expansion = size.width * 0.06f * ringProgress

            drawArc(
                color = Color.White.copy(alpha = alpha),
                startAngle = 137f,
                sweepAngle = 82f,
                useCenter = false,
                topLeft = Offset(size.width * 0.04f - expansion, size.height * 0.13f - expansion),
                size = Size(
                    width = size.width * 0.48f + expansion * 2f,
                    height = size.height * 0.54f + expansion * 2f,
                ),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
                color = Color.White.copy(alpha = alpha),
                startAngle = -39f,
                sweepAngle = 82f,
                useCenter = false,
                topLeft = Offset(size.width * 0.48f + expansion, size.height * 0.13f - expansion),
                size = Size(
                    width = size.width * 0.48f + expansion * 2f,
                    height = size.height * 0.54f + expansion * 2f,
                ),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        Canvas(
            modifier = Modifier
                .size(58.dp)
                .graphicsLayer(rotationZ = rotation),
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val bellTop = height * 0.22f
            val bellBottom = height * 0.72f
            val bellHalfWidth = width * 0.30f

            val bell = Path().apply {
                moveTo(centerX, bellTop)
                cubicTo(
                    centerX - bellHalfWidth * 0.92f,
                    bellTop + height * 0.03f,
                    centerX - bellHalfWidth,
                    bellTop + height * 0.30f,
                    centerX - bellHalfWidth,
                    bellBottom - height * 0.06f,
                )
                cubicTo(
                    centerX - bellHalfWidth,
                    bellBottom,
                    centerX - bellHalfWidth * 1.26f,
                    bellBottom,
                    centerX - bellHalfWidth * 1.26f,
                    bellBottom + height * 0.09f,
                )
                quadraticTo(
                    centerX,
                    bellBottom + height * 0.20f,
                    centerX + bellHalfWidth * 1.26f,
                    bellBottom + height * 0.09f,
                )
                cubicTo(
                    centerX + bellHalfWidth * 1.26f,
                    bellBottom,
                    centerX + bellHalfWidth,
                    bellBottom,
                    centerX + bellHalfWidth,
                    bellBottom - height * 0.06f,
                )
                cubicTo(
                    centerX + bellHalfWidth,
                    bellTop + height * 0.30f,
                    centerX + bellHalfWidth * 0.92f,
                    bellTop + height * 0.03f,
                    centerX,
                    bellTop,
                )
                close()
            }

            drawCircle(
                color = Color.White,
                radius = width * 0.085f,
                center = Offset(centerX, bellTop - height * 0.025f),
            )
            drawPath(path = bell, color = Color.White)
            drawCircle(
                color = Color.White,
                radius = width * 0.08f,
                center = Offset(centerX, height * 0.90f),
            )
        }
    }
}

@Composable
private fun BatteryLevelIcon(
    batteryPercentage: Int,
    modifier: Modifier = Modifier,
) {
    val fillFraction = batteryPercentage.coerceIn(0, 100) / 100f
    val fillColor = when {
        batteryPercentage <= 15 -> Color(0xFFE9333E)
        batteryPercentage <= 30 -> Color(0xFFFF725B)
        batteryPercentage <= 60 -> Color(0xFFFFC766)
        else -> Color(0xFF60D984)
    }

    Canvas(modifier = modifier) {
        val strokeWidth = size.height * 0.068f
        val terminalWidth = size.width * 0.105f
        val terminalHeight = size.height * 0.42f
        val bodyWidth = size.width - terminalWidth - strokeWidth * 0.7f
        val bodyHeight = size.height * 0.70f
        val bodyTop = (size.height - bodyHeight) / 2f
        val cornerRadius = bodyHeight * 0.18f
        val bodyRect = Rect(
            left = strokeWidth / 2f,
            top = bodyTop,
            right = bodyWidth,
            bottom = bodyTop + bodyHeight,
        )
        val innerPadding = strokeWidth * 1.25f
        val innerRect = Rect(
            left = bodyRect.left + innerPadding,
            top = bodyRect.top + innerPadding,
            right = bodyRect.right - innerPadding,
            bottom = bodyRect.bottom - innerPadding,
        )
        val fillWidth = innerRect.width * fillFraction
        val fillRect = Rect(
            left = innerRect.left,
            top = innerRect.top,
            right = innerRect.left + fillWidth,
            bottom = innerRect.bottom,
        )

        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.48f),
                ),
                startX = innerRect.left,
                endX = innerRect.right,
            ),
            topLeft = innerRect.topLeft,
            size = innerRect.size,
            cornerRadius = CornerRadius(cornerRadius * 0.68f, cornerRadius * 0.68f),
        )
        if (fillWidth > 0f) {
            drawRoundRect(
                color = fillColor,
                topLeft = fillRect.topLeft,
                size = fillRect.size,
                cornerRadius = CornerRadius(cornerRadius * 0.58f, cornerRadius * 0.58f),
            )
        }
        drawRoundRect(
            color = Color.White,
            topLeft = bodyRect.topLeft,
            size = bodyRect.size,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.88f),
            topLeft = Offset(bodyRect.right - strokeWidth * 0.15f, bodyTop + (bodyHeight - terminalHeight) / 2f),
            size = Size(terminalWidth, terminalHeight),
            cornerRadius = CornerRadius(terminalHeight * 0.22f, terminalHeight * 0.22f),
        )
    }
}

@Composable
internal fun HoldToStopButton(
    label: String,
    contentDescription: String,
    onHoldComplete: () -> Unit,
    modifier: Modifier = Modifier,
    holdDurationMillis: Int = HoldToStopDurationMillis,
) {
    val holdProgress = remember { Animatable(0f) }
    var isPressed by remember { mutableStateOf(false) }
    var hasTriggeredDismiss by remember { mutableStateOf(false) }
    val currentOnHoldComplete by rememberUpdatedState(onHoldComplete)
    val buttonShape = RoundedCornerShape(percent = 50)

    LaunchedEffect(isPressed, hasTriggeredDismiss, holdDurationMillis) {
        if (isPressed && !hasTriggeredDismiss) {
            holdProgress.snapTo(0f)
            holdProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = holdDurationMillis,
                    easing = LinearEasing,
                ),
            )
        } else if (!hasTriggeredDismiss) {
            holdProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = HoldCancelAnimationMillis,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    Box(
        modifier = modifier
            .semantics {
                this.contentDescription = contentDescription
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    if (hasTriggeredDismiss) {
                        return@awaitEachGesture
                    }

                    isPressed = true
                    val completedHold = withTimeoutOrNull(holdDurationMillis.toLong()) {
                        waitForUpOrCancellation()
                        false
                    } ?: true

                    isPressed = false
                    if (completedHold && !hasTriggeredDismiss) {
                        hasTriggeredDismiss = true
                        currentOnHoldComplete()
                    }
                }
            }
            .drawWithCache {
                val strokeWidth = 4.dp.toPx()
                val inset = strokeWidth / 2f
                val cornerRadius = (size.height - strokeWidth) / 2f
                val outlinePath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                left = inset,
                                top = inset,
                                right = size.width - inset,
                                bottom = size.height - inset,
                            ),
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        ),
                    )
                }
                val pathMeasure = PathMeasure().apply {
                    setPath(outlinePath, false)
                }

                onDrawWithContent {
                    drawContent()
                    drawPath(
                        path = outlinePath,
                        color = AlarmRedDark.copy(alpha = 0.16f),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )

                    if (holdProgress.value > 0f) {
                        val progressPath = Path()
                        pathMeasure.getSegment(
                            startDistance = 0f,
                            stopDistance = pathMeasure.length * holdProgress.value,
                            destination = progressPath,
                            startWithMoveTo = true,
                        )
                        drawPath(
                            path = progressPath,
                            color = AlarmRedDark,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )
                    }
                }
            }
            .background(Color.White, buttonShape),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(AlarmRedText, RoundedCornerShape(4.dp)),
            )
            Text(
                text = label,
                color = AlarmRedText,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun LowBatteryAlarmScreenPreview() {
    BatteryAlarmTheme(dynamicColor = false) {
        LowBatteryAlarmScreen(
            batteryPercentage = 10,
            onDismissAlarmClick = {},
        )
    }
}
