package com.example.batteryalarm.ui.feature

import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmSettingsRepository
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmStartResult
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.AlarmStopReason
import com.example.batteryalarm.domain.AlarmStopResult
import com.example.batteryalarm.domain.update.AppUpdate
import com.example.batteryalarm.domain.update.UpdateCheckAttemptStore
import com.example.batteryalarm.update.UpdateOpener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FeatureAlarmController(
    initialState: AlarmState = AlarmState.Idle,
    var startFailure: Exception? = null,
) : AlarmController {
    override var state: AlarmState = initialState
        private set

    val startedReasons = mutableListOf<AlarmStartReason>()

    override suspend fun startAlarm(reason: AlarmStartReason): AlarmStartResult {
        startFailure?.let { throw it }
        startedReasons += reason
        val active = state as? AlarmState.Active
        if (active != null) return AlarmStartResult.AlreadyActive(active)
        val next = AlarmState.Active(reason)
        state = next
        return AlarmStartResult.Started(next)
    }

    override fun stopAlarm(reason: AlarmStopReason): AlarmStopResult {
        val previous = state as? AlarmState.Active ?: return AlarmStopResult.AlreadyIdle
        state = AlarmState.Idle
        return AlarmStopResult.Stopped(previous, reason)
    }
}

internal class FeatureAlarmSettingsRepository(
    enabled: Boolean = false,
    var failure: Exception? = null,
    var storedOverride: Boolean? = null,
) : AlarmSettingsRepository {
    private val enabledState = MutableStateFlow(enabled)
    override val alarmEnabled = enabledState.asStateFlow()

    override suspend fun isAlarmEnabled(): Boolean = enabledState.value

    override suspend fun setAlarmEnabled(enabled: Boolean) {
        failure?.let { throw it }
        enabledState.value = storedOverride ?: enabled
    }

    override suspend fun toggleAlarmEnabled() = setAlarmEnabled(!isAlarmEnabled())
}

internal class FeatureUpdateStore(
    var checkedAt: Long? = null,
    var pending: AppUpdate? = null,
) : UpdateCheckAttemptStore {
    override suspend fun getLastCheckAttemptAtMillis(): Long? = checkedAt
    override suspend fun setLastCheckAttemptAtMillis(epochMillis: Long) { checkedAt = epochMillis }
    override suspend fun getPendingUpdate(): AppUpdate? = pending
    override suspend fun setPendingUpdate(update: AppUpdate) { pending = update }
    override suspend fun clearPendingUpdate() { pending = null }
}

internal class FeatureUpdateOpener : UpdateOpener {
    val opened = mutableListOf<String>()
    override fun openReleasePage(url: String) { opened += url }
}
