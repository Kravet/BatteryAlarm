package com.example.batteryalarm.ui

import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HoldToStopButtonTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun releasingBeforeHoldDuration_doesNotCompleteHold() {
        val holdCompleteCount = AtomicInteger(0)
        setHoldButtonContent(holdCompleteCount)

        holdButtonFor(ShortHoldMillis)

        assertEquals(0, holdCompleteCount.get())
    }

    @Test
    fun holdingPastHoldDuration_completesHold() {
        val holdCompleteCount = AtomicInteger(0)
        setHoldButtonContent(holdCompleteCount)

        holdButtonFor(CompletedHoldMillis)
        composeRule.waitUntil(timeoutMillis = AssertionTimeoutMillis) {
            holdCompleteCount.get() == 1
        }

        assertEquals(1, holdCompleteCount.get())
    }

    @Test
    fun afterHoldCompletes_subsequentHoldsDoNotCompleteAgain() {
        val holdCompleteCount = AtomicInteger(0)
        setHoldButtonContent(holdCompleteCount)

        holdButtonFor(CompletedHoldMillis)
        composeRule.waitUntil(timeoutMillis = AssertionTimeoutMillis) {
            holdCompleteCount.get() == 1
        }

        holdButtonFor(CompletedHoldMillis)

        assertEquals(1, holdCompleteCount.get())
    }

    private fun setHoldButtonContent(holdCompleteCount: AtomicInteger) {
        composeRule.setContent {
            BatteryAlarmTheme(dynamicColor = false) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    HoldToStopButton(
                        label = "Stop",
                        contentDescription = HoldButtonContentDescription,
                        onHoldComplete = { holdCompleteCount.incrementAndGet() },
                        modifier = Modifier
                            .width(240.dp)
                            .height(72.dp),
                        holdDurationMillis = HoldDurationMillis,
                    )
                }
            }
        }
    }

    private fun holdButtonFor(durationMillis: Long) {
        composeRule.waitForIdle()
        val button = composeRule.onNodeWithContentDescription(HoldButtonContentDescription)
        if (durationMillis < HoldDurationMillis) {
            button.performTouchInput {
                down(center)
                advanceEventTime(durationMillis)
                up()
            }
        } else {
            button.performTouchInput {
                down(center)
            }
            SystemClock.sleep(durationMillis)
            button.performTouchInput {
                up()
            }
        }
        instrumentation.waitForIdleSync()
        composeRule.waitForIdle()
    }

    private companion object {
        const val HoldButtonContentDescription = "Hold button under test"
        const val HoldDurationMillis = 400
        const val ShortHoldMillis = 100L
        const val CompletedHoldMillis = 900L
        const val AssertionTimeoutMillis = 2_000L
    }
}
