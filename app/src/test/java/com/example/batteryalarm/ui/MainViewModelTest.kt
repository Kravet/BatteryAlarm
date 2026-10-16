package com.example.batteryalarm.ui

import com.example.batteryalarm.alarm.BatteryLowAlarmHandler
import com.example.batteryalarm.analytics.FakeAppAnalytics
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmStartResult
import com.example.batteryalarm.domain.AlarmSettingsRepository
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.AlarmStopReason
import com.example.batteryalarm.domain.AlarmStopResult
import com.example.batteryalarm.domain.privacy.PrivacyConsent
import com.example.batteryalarm.domain.update.AppUpdate
import com.example.batteryalarm.domain.update.CheckForAppUpdate
import com.example.batteryalarm.domain.update.InstalledAppVersionName
import com.example.batteryalarm.domain.update.LatestStableRelease
import com.example.batteryalarm.domain.update.LatestStableReleaseSource
import com.example.batteryalarm.domain.update.UpdateCheckAttemptStore
import com.example.batteryalarm.domain.update.UpdateCheckerEnabled
import com.example.batteryalarm.privacy.FakeAnalyticsCollectionController
import com.example.batteryalarm.privacy.FakeCrashlyticsCollectionController
import com.example.batteryalarm.privacy.FirebaseConsentApplier
import com.example.batteryalarm.privacy.InMemoryPrivacyConsentRepository
import com.example.batteryalarm.ui.privacy.PrivacySheetMode
import com.example.batteryalarm.update.UpdateOpener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun TestScope.createViewModel(
        alarmSettingsRepository: FakeAlarmSettingsRepository = FakeAlarmSettingsRepository(enabled = false),
        alarmController: FakeAlarmController = FakeAlarmController(),
        checkForAppUpdate: CheckForAppUpdate = disabledUpdateChecker(),
        updateCheckAttemptStore: UpdateCheckAttemptStore = InMemoryUpdateCheckAttemptStore(),
        updateOpener: FakeUpdateOpener = FakeUpdateOpener(),
        analytics: FakeAppAnalytics = FakeAppAnalytics(),
        privacyConsentRepository: InMemoryPrivacyConsentRepository = InMemoryPrivacyConsentRepository(),
        firebaseConsentApplier: FirebaseConsentApplier = FirebaseConsentApplier(
            analyticsCollection = FakeAnalyticsCollectionController(),
            crashlyticsCollection = FakeCrashlyticsCollectionController(),
        ),
    ): MainViewModel {
        val handler = BatteryLowAlarmHandler(
            alarmController = alarmController,
            scope = this,
        )
        return MainViewModel(
            alarmSettingsRepository = alarmSettingsRepository,
            batteryLowAlarmHandler = handler,
            alarmController = alarmController,
            checkForAppUpdate = checkForAppUpdate,
            updateCheckAttemptStore = updateCheckAttemptStore,
            updateOpener = updateOpener,
            analytics = analytics,
            privacyConsentRepository = privacyConsentRepository,
            firebaseConsentApplier = firebaseConsentApplier,
        )
    }

    private fun disabledUpdateChecker(
        store: UpdateCheckAttemptStore = InMemoryUpdateCheckAttemptStore(),
    ): CheckForAppUpdate = CheckForAppUpdate(
        isEnabled = UpdateCheckerEnabled { false },
        attemptStore = store,
        releaseSource = object : LatestStableReleaseSource {
            override suspend fun fetchLatestStableRelease(): LatestStableRelease? = null
        },
        installedVersionName = InstalledAppVersionName { "1.0.0" },
        clockMillis = { 0L },
    )

    private fun availableUpdateChecker(
        store: UpdateCheckAttemptStore = InMemoryUpdateCheckAttemptStore(),
        update: AppUpdate = AppUpdate(
            versionName = "1.1.0",
            releaseNotes = "Notes",
            releasePageUrl = "https://github.com/example/repo/releases/tag/v1.1.0",
        ),
    ): CheckForAppUpdate = CheckForAppUpdate(
        isEnabled = UpdateCheckerEnabled { true },
        attemptStore = store,
        releaseSource = object : LatestStableReleaseSource {
            override suspend fun fetchLatestStableRelease(): LatestStableRelease = LatestStableRelease(
                tagName = "v${update.versionName}",
                body = update.releaseNotes,
                htmlUrl = update.releasePageUrl,
            )
        },
        installedVersionName = InstalledAppVersionName { "1.0.0" },
        clockMillis = { 1L },
    )

    private fun pendingWithinIntervalChecker(
        store: InMemoryUpdateCheckAttemptStore,
    ): CheckForAppUpdate = CheckForAppUpdate(
        isEnabled = UpdateCheckerEnabled { true },
        attemptStore = store,
        releaseSource = object : LatestStableReleaseSource {
            override suspend fun fetchLatestStableRelease(): LatestStableRelease? =
                error("Remote should not be called within interval")
        },
        installedVersionName = InstalledAppVersionName { "1.0.0" },
        clockMillis = { 1_000L },
    )

    @Test
    fun `initial state uses stored alarm enabled flag`() = runTest {
        val viewModel = createViewModel(
            alarmSettingsRepository = FakeAlarmSettingsRepository(enabled = true),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEnabled)
    }

    @Test
    fun `shows onboarding privacy sheet when consent decision is missing`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(PrivacySheetMode.ONBOARDING, viewModel.uiState.value.privacySheetMode)
        assertFalse(viewModel.uiState.value.decisionRecorded)
    }

    @Test
    fun `does not show onboarding when consent decision already recorded`() = runTest {
        val privacyRepo = InMemoryPrivacyConsentRepository(
            PrivacyConsent(
                analyticsEnabled = true,
                crashlyticsEnabled = false,
                decisionRecorded = true,
                consentTextVersion = PrivacyConsent.CONSENT_TEXT_VERSION,
            ),
        )
        val viewModel = createViewModel(privacyConsentRepository = privacyRepo)

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.privacySheetMode)
        assertTrue(viewModel.uiState.value.decisionRecorded)
        assertTrue(viewModel.uiState.value.analyticsEnabled)
        assertFalse(viewModel.uiState.value.crashlyticsEnabled)
    }

    @Test
    fun `stale consent text version shows onboarding and keeps collection off`() = runTest {
        val privacyRepo = InMemoryPrivacyConsentRepository(
            PrivacyConsent(
                analyticsEnabled = true,
                crashlyticsEnabled = true,
                decisionRecorded = true,
                consentTextVersion = "1",
            ),
        )
        val analyticsCollection = FakeAnalyticsCollectionController()
        val crashlyticsCollection = FakeCrashlyticsCollectionController()
        val viewModel = createViewModel(
            privacyConsentRepository = privacyRepo,
            firebaseConsentApplier = FirebaseConsentApplier(analyticsCollection, crashlyticsCollection),
        )

        advanceUntilIdle()

        assertEquals(PrivacySheetMode.ONBOARDING, viewModel.uiState.value.privacySheetMode)
        assertFalse(viewModel.uiState.value.decisionRecorded)
        assertEquals(false, analyticsCollection.enabledHistory.last())
        assertEquals(false, crashlyticsCollection.enabledHistory.last())
    }

    @Test
    fun `allow both persists consent enables both and closes onboarding`() = runTest {
        val privacyRepo = InMemoryPrivacyConsentRepository()
        val analyticsCollection = FakeAnalyticsCollectionController()
        val crashlyticsCollection = FakeCrashlyticsCollectionController()
        val viewModel = createViewModel(
            privacyConsentRepository = privacyRepo,
            firebaseConsentApplier = FirebaseConsentApplier(analyticsCollection, crashlyticsCollection),
        )

        advanceUntilIdle()
        viewModel.onAllowBothPrivacy()
        advanceUntilIdle()

        assertTrue(privacyRepo.current.decisionRecorded)
        assertTrue(privacyRepo.current.analyticsEnabled)
        assertTrue(privacyRepo.current.crashlyticsEnabled)
        assertNull(viewModel.uiState.value.privacySheetMode)
        assertEquals(true, analyticsCollection.enabledHistory.last())
        assertEquals(true, crashlyticsCollection.enabledHistory.last())
    }

    @Test
    fun `decline both persists disabled consent and closes onboarding`() = runTest {
        val privacyRepo = InMemoryPrivacyConsentRepository()
        val viewModel = createViewModel(privacyConsentRepository = privacyRepo)

        advanceUntilIdle()
        viewModel.onDeclineBothPrivacy()
        advanceUntilIdle()

        assertTrue(privacyRepo.current.decisionRecorded)
        assertFalse(privacyRepo.current.analyticsEnabled)
        assertFalse(privacyRepo.current.crashlyticsEnabled)
        assertNull(viewModel.uiState.value.privacySheetMode)
    }

    @Test
    fun `dismiss without decision keeps collection off and hides sheet for session`() = runTest {
        val privacyRepo = InMemoryPrivacyConsentRepository()
        val analyticsCollection = FakeAnalyticsCollectionController()
        val crashlyticsCollection = FakeCrashlyticsCollectionController()
        val viewModel = createViewModel(
            privacyConsentRepository = privacyRepo,
            firebaseConsentApplier = FirebaseConsentApplier(analyticsCollection, crashlyticsCollection),
        )

        advanceUntilIdle()
        viewModel.onPrivacySheetDismiss()
        advanceUntilIdle()

        assertFalse(privacyRepo.current.decisionRecorded)
        assertNull(viewModel.uiState.value.privacySheetMode)
        assertFalse(privacyRepo.current.analyticsEnabled)
        assertFalse(privacyRepo.current.crashlyticsEnabled)
        assertEquals(false, analyticsCollection.enabledHistory.last())
        assertEquals(false, crashlyticsCollection.enabledHistory.last())
    }

    @Test
    fun `settings toggles persist immediately and keep settings sheet open`() = runTest {
        val privacyRepo = InMemoryPrivacyConsentRepository(
            PrivacyConsent(
                decisionRecorded = true,
                consentTextVersion = PrivacyConsent.CONSENT_TEXT_VERSION,
            ),
        )
        val viewModel = createViewModel(privacyConsentRepository = privacyRepo)

        advanceUntilIdle()
        viewModel.onPrivacyDiagnosticsClick()
        advanceUntilIdle()
        viewModel.onAnalyticsConsentChange(true)
        advanceUntilIdle()

        assertEquals(PrivacySheetMode.SETTINGS, viewModel.uiState.value.privacySheetMode)
        assertTrue(privacyRepo.current.analyticsEnabled)
        assertTrue(viewModel.uiState.value.analyticsEnabled)
    }

    @Test
    fun `when alarm is disabled, then toggle stores enabled flag and updates state`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = false)
        val viewModel = createViewModel(alarmSettingsRepository = repository)

        advanceUntilIdle()
        viewModel.onAlarmToggleClick()
        advanceUntilIdle()

        assertTrue(repository.isAlarmEnabled())
        assertTrue(viewModel.uiState.value.isEnabled)
    }

    @Test
    fun `when alarm is enabled, then toggle stores disabled flag and updates state`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = true)
        val viewModel = createViewModel(alarmSettingsRepository = repository)

        advanceUntilIdle()
        viewModel.onAlarmToggleClick()
        advanceUntilIdle()

        assertFalse(repository.isAlarmEnabled())
        assertFalse(viewModel.uiState.value.isEnabled)
    }

    @Test
    fun `when toggle is clicked, then state updates from saved value emitted by repository`() = runTest {
        val repository = FakeAlarmSettingsRepository(
            enabled = false,
            valueSavedAfterSet = false,
        )
        val viewModel = createViewModel(alarmSettingsRepository = repository)

        advanceUntilIdle()
        viewModel.onAlarmToggleClick()
        advanceUntilIdle()

        assertFalse(repository.isAlarmEnabled())
        assertFalse(viewModel.uiState.value.isEnabled)
    }

    @Test
    fun `when toggle save fails, then persisted state is kept and failure side effect is emitted`() =
        runTest {
            val repository = FakeAlarmSettingsRepository(
                enabled = false,
                exceptionOnSet = IllegalStateException("Write failed"),
            )
            val viewModel = createViewModel(alarmSettingsRepository = repository)
            val sideEffects = mutableListOf<MainSideEffect>()
            val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.sideEffects
                    .take(1)
                    .toList(sideEffects)
            }

            advanceUntilIdle()
            viewModel.onAlarmToggleClick()
            advanceUntilIdle()

            collectJob.join()
            assertFalse(repository.isAlarmEnabled())
            assertFalse(viewModel.uiState.value.isEnabled)
            assertEquals(
                listOf(MainSideEffect.ShowAlarmSettingsChangeFailed),
                sideEffects,
            )
        }

    @Test
    fun `when toggle save is cancelled, then it is not handled as failure`() = runTest {
        val repository = FakeAlarmSettingsRepository(
            enabled = false,
            exceptionOnSet = CancellationException("Save cancelled"),
        )
        val viewModel = createViewModel(alarmSettingsRepository = repository)
        val sideEffects = mutableListOf<MainSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.sideEffects.toCollection(sideEffects)
        }

        advanceUntilIdle()
        viewModel.onAlarmToggleClick()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEnabled)
        assertEquals(emptyList<MainSideEffect>(), sideEffects)
    }

    @Test
    fun `initial state is disabled when stored flag is disabled`() = runTest {
        val viewModel = createViewModel(
            alarmSettingsRepository = FakeAlarmSettingsRepository(enabled = false),
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEnabled)
    }

    @Test
    fun `is alarm active returns true when alarm is active`() = runTest {
        val viewModel = createViewModel(
            alarmController = FakeAlarmController(
                initialState = AlarmState.Active(AlarmStartReason.SystemLowBattery),
            ),
        )

        assertTrue(viewModel.isAlarmActive())
    }

    @Test
    fun `is alarm active returns false when alarm is idle`() = runTest {
        val viewModel = createViewModel(
            alarmController = FakeAlarmController(initialState = AlarmState.Idle),
        )

        assertFalse(viewModel.isAlarmActive())
    }

    @Test
    fun `test alarm uses shared trigger after delay without changing main screen state`() = runTest {
        val alarmController = FakeAlarmController()
        val viewModel = createViewModel(
            alarmSettingsRepository = FakeAlarmSettingsRepository(enabled = false),
            alarmController = alarmController,
        )

        advanceUntilIdle()
        viewModel.onTestAlarmClick()

        assertTrue(viewModel.uiState.value.isTestAlarmPending)
        assertEquals(BatteryLowAlarmHandler.TEST_ALARM_DELAY_SECONDS, viewModel.uiState.value.testAlarmSecondsRemaining)
        assertEquals(emptyList<AlarmStartReason>(), alarmController.startedReasons)

        advanceTimeBy(BatteryLowAlarmHandler.TEST_ALARM_DELAY_MS)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isTestAlarmPending)
        assertEquals(null, viewModel.uiState.value.testAlarmSecondsRemaining)
        assertEquals(listOf(AlarmStartReason.TestAlarmFlow), alarmController.startedReasons)
    }

    @Test
    fun `test alarm countdown updates once per second while pending`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.onTestAlarmClick()

        assertEquals(4, viewModel.uiState.value.testAlarmSecondsRemaining)

        advanceTimeBy(BatteryLowAlarmHandler.TEST_ALARM_TICK_MS)
        runCurrent()
        assertEquals(3, viewModel.uiState.value.testAlarmSecondsRemaining)

        advanceTimeBy(BatteryLowAlarmHandler.TEST_ALARM_TICK_MS)
        runCurrent()
        assertEquals(2, viewModel.uiState.value.testAlarmSecondsRemaining)

        advanceTimeBy(BatteryLowAlarmHandler.TEST_ALARM_TICK_MS)
        runCurrent()
        assertEquals(1, viewModel.uiState.value.testAlarmSecondsRemaining)
    }

    @Test
    fun `test alarm finish preserves alarm enabled state changed while pending`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = false)
        val viewModel = createViewModel(alarmSettingsRepository = repository)

        advanceUntilIdle()
        viewModel.onTestAlarmClick()
        assertTrue(viewModel.uiState.value.isTestAlarmPending)
        assertEquals(BatteryLowAlarmHandler.TEST_ALARM_DELAY_SECONDS, viewModel.uiState.value.testAlarmSecondsRemaining)

        viewModel.onAlarmToggleClick()
        runCurrent()
        assertTrue(viewModel.uiState.value.isEnabled)
        assertEquals(BatteryLowAlarmHandler.TEST_ALARM_DELAY_SECONDS, viewModel.uiState.value.testAlarmSecondsRemaining)

        advanceTimeBy(BatteryLowAlarmHandler.TEST_ALARM_DELAY_MS)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isTestAlarmPending)
        assertEquals(null, viewModel.uiState.value.testAlarmSecondsRemaining)
        assertTrue(viewModel.uiState.value.isEnabled)
    }

    @Test
    fun `when update is available, then pending update is set on ui state`() = runTest {
        val viewModel = createViewModel(
            checkForAppUpdate = availableUpdateChecker(),
        )

        advanceUntilIdle()

        assertEquals(
            AppUpdateUiModel(
                versionName = "1.1.0",
                releaseNotes = "Notes",
                releasePageUrl = "https://github.com/example/repo/releases/tag/v1.1.0",
            ),
            viewModel.uiState.value.pendingUpdate,
        )
    }

    @Test
    fun `when update is clicked, then opener receives release url and pending update is cleared`() = runTest {
        val opener = FakeUpdateOpener()
        val viewModel = createViewModel(
            checkForAppUpdate = availableUpdateChecker(),
            updateOpener = opener,
        )
        advanceUntilIdle()

        viewModel.onUpdateClick()

        assertEquals(
            listOf("https://github.com/example/repo/releases/tag/v1.1.0"),
            opener.openedUrls,
        )
        assertNull(viewModel.uiState.value.pendingUpdate)
    }

    @Test
    fun `when alarm is active, then update button is not shown`() = runTest {
        val store = InMemoryUpdateCheckAttemptStore()
        val viewModel = createViewModel(
            alarmController = FakeAlarmController(
                initialState = AlarmState.Active(AlarmStartReason.SystemLowBattery),
            ),
            checkForAppUpdate = availableUpdateChecker(store = store),
            updateCheckAttemptStore = store,
        )

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingUpdate)
        assertEquals("1.1.0", store.pendingUpdate?.versionName)
    }

    @Test
    fun `when relaunching within interval, then persisted pending update is shown again`() = runTest {
        val store = InMemoryUpdateCheckAttemptStore(
            lastCheckAttemptAtMillis = 500L,
            pendingUpdate = AppUpdate(
                versionName = "1.1.0",
                releaseNotes = "Notes",
                releasePageUrl = "https://github.com/example/repo/releases/tag/v1.1.0",
            ),
        )
        val viewModel = createViewModel(
            checkForAppUpdate = pendingWithinIntervalChecker(store),
            updateCheckAttemptStore = store,
        )

        advanceUntilIdle()

        assertEquals(
            AppUpdateUiModel(
                versionName = "1.1.0",
                releaseNotes = "Notes",
                releasePageUrl = "https://github.com/example/repo/releases/tag/v1.1.0",
            ),
            viewModel.uiState.value.pendingUpdate,
        )
    }

    @Test
    fun `when update is clicked, then persisted pending update is cleared`() = runTest {
        val store = InMemoryUpdateCheckAttemptStore()
        val viewModel = createViewModel(
            checkForAppUpdate = availableUpdateChecker(store = store),
            updateCheckAttemptStore = store,
        )
        advanceUntilIdle()

        viewModel.onUpdateClick()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingUpdate)
        assertNull(store.pendingUpdate)
    }
}

private class InMemoryUpdateCheckAttemptStore(
    var lastCheckAttemptAtMillis: Long? = null,
    var pendingUpdate: AppUpdate? = null,
) : UpdateCheckAttemptStore {
    override suspend fun getLastCheckAttemptAtMillis(): Long? = lastCheckAttemptAtMillis

    override suspend fun setLastCheckAttemptAtMillis(epochMillis: Long) {
        lastCheckAttemptAtMillis = epochMillis
    }

    override suspend fun getPendingUpdate(): AppUpdate? = pendingUpdate

    override suspend fun setPendingUpdate(update: AppUpdate) {
        pendingUpdate = update
    }

    override suspend fun clearPendingUpdate() {
        pendingUpdate = null
    }
}

private class FakeUpdateOpener : UpdateOpener {
    val openedUrls = mutableListOf<String>()

    override fun openReleasePage(url: String) {
        openedUrls += url
    }
}

private class FakeAlarmSettingsRepository(
    enabled: Boolean,
    private val valueSavedAfterSet: Boolean? = null,
    private val exceptionOnSet: Exception? = null,
) : AlarmSettingsRepository {
    private val alarmEnabledFlow = MutableStateFlow(enabled)

    override val alarmEnabled: Flow<Boolean> = alarmEnabledFlow.asStateFlow()

    override suspend fun isAlarmEnabled(): Boolean {
        return alarmEnabledFlow.value
    }

    override suspend fun setAlarmEnabled(enabled: Boolean) {
        exceptionOnSet?.let { throw it }
        alarmEnabledFlow.value = valueSavedAfterSet ?: enabled
    }

    override suspend fun toggleAlarmEnabled() {
        setAlarmEnabled(!isAlarmEnabled())
    }
}

private class FakeAlarmController(
    initialState: AlarmState = AlarmState.Idle,
    private val startException: Exception? = null,
) : AlarmController {
    override var state: AlarmState = initialState
        private set

    val startedReasons = mutableListOf<AlarmStartReason>()
    val stoppedReasons = mutableListOf<AlarmStopReason>()

    override suspend fun startAlarm(reason: AlarmStartReason): AlarmStartResult {
        startException?.let { throw it }
        startedReasons += reason

        val activeState = state as? AlarmState.Active
        if (activeState != null) {
            return AlarmStartResult.AlreadyActive(activeState)
        }

        val newState = AlarmState.Active(reason)
        state = newState
        return AlarmStartResult.Started(newState)
    }

    override fun stopAlarm(reason: AlarmStopReason): AlarmStopResult {
        stoppedReasons += reason

        val activeState = state as? AlarmState.Active ?: return AlarmStopResult.AlreadyIdle
        state = AlarmState.Idle
        return AlarmStopResult.Stopped(
            previousState = activeState,
            reason = reason,
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
