package com.example.batteryalarm.di

import android.content.Context
import com.example.batteryalarm.domain.privacy.PrivacyConsentRepository
import com.example.batteryalarm.privacy.AnalyticsCollectionController
import com.example.batteryalarm.privacy.CrashlyticsCollectionController
import com.example.batteryalarm.privacy.DataStorePrivacyConsentRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PrivacyModule {
    @Provides
    @Singleton
    fun providePrivacyConsentRepository(
        @ApplicationContext context: Context,
    ): PrivacyConsentRepository = DataStorePrivacyConsentRepository(context)

    @Provides
    @Singleton
    fun provideAnalyticsCollectionController(
        firebaseAnalytics: FirebaseAnalytics,
    ): AnalyticsCollectionController = AnalyticsCollectionController { enabled ->
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    @Provides
    @Singleton
    fun provideCrashlyticsCollectionController(): CrashlyticsCollectionController =
        object : CrashlyticsCollectionController {
            private val crashlytics: FirebaseCrashlytics
                get() = FirebaseCrashlytics.getInstance()

            override fun setCollectionEnabled(enabled: Boolean) {
                crashlytics.setCrashlyticsCollectionEnabled(enabled)
            }

            override fun deleteUnsentReports() {
                crashlytics.deleteUnsentReports()
            }
        }
}
