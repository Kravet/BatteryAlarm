package com.example.batteryalarm.ui.feature

import com.example.batteryalarm.analytics.AppAnalytics
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.update.CheckForAppUpdate
import com.example.batteryalarm.domain.update.UpdateCheckAttemptStore
import com.example.batteryalarm.domain.update.UpdateCheckResult
import com.example.batteryalarm.ui.AppUpdateUiModel
import com.example.batteryalarm.update.UpdateCheckLog
import com.example.batteryalarm.update.UpdateOpener
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Presentation orchestration around the existing update-check use case. */
class UpdateFeature @Inject constructor(
    private val checkForAppUpdate: CheckForAppUpdate,
    private val attemptStore: UpdateCheckAttemptStore,
    private val updateOpener: UpdateOpener,
    private val analytics: AppAnalytics,
    private val alarmController: AlarmController,
) {
    private val _state = MutableStateFlow(UpdateFeatureState())
    val state: StateFlow<UpdateFeatureState> = _state.asStateFlow()

    fun checkOnStart(scope: CoroutineScope) {
        scope.launch { runUpdateCheck() }
    }

    fun openPending(scope: CoroutineScope) {
        val pending = _state.value.pendingUpdate ?: return
        analytics.logUpdateClicked(version = pending.versionName)
        updateOpener.openReleasePage(pending.releasePageUrl)
        _state.update { it.copy(pendingUpdate = null) }
        scope.launch { attemptStore.clearPendingUpdate() }
    }

    private suspend fun runUpdateCheck() {
        UpdateCheckLog.d("runUpdateCheck start")
        try {
            when (val result = checkForAppUpdate()) {
                is UpdateCheckResult.Available -> {
                    val alarmActive = alarmController.state is AlarmState.Active
                    UpdateCheckLog.d(
                        "runUpdateCheck Available version=${result.update.versionName} " +
                            "url=${result.update.releasePageUrl} alarmActive=$alarmActive",
                    )
                    if (!alarmActive) {
                        val update = AppUpdateUiModel.from(result.update)
                        analytics.logUpdateAvailableShown(version = update.versionName)
                        _state.update { it.copy(pendingUpdate = update) }
                    }
                }
                UpdateCheckResult.Disabled -> UpdateCheckLog.d("runUpdateCheck Disabled")
                UpdateCheckResult.SkippedWithinInterval ->
                    UpdateCheckLog.d("runUpdateCheck SkippedWithinInterval")
                UpdateCheckResult.UpToDate -> UpdateCheckLog.d("runUpdateCheck UpToDate")
                UpdateCheckResult.NoUsableRelease ->
                    UpdateCheckLog.d("runUpdateCheck NoUsableRelease")
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            UpdateCheckLog.w("runUpdateCheck failed: ${exception.message}")
        }
    }
}

data class UpdateFeatureState(
    val pendingUpdate: AppUpdateUiModel? = null,
)
