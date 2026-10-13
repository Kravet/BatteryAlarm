package com.example.batteryalarm.di

import android.content.Context
import com.example.batteryalarm.BuildConfig
import com.example.batteryalarm.domain.update.CheckForAppUpdate
import com.example.batteryalarm.domain.update.InstalledAppVersionName
import com.example.batteryalarm.domain.update.LatestStableReleaseSource
import com.example.batteryalarm.domain.update.UpdateCheckAttemptStore
import com.example.batteryalarm.domain.update.UpdateCheckerEnabled
import com.example.batteryalarm.update.AndroidUpdateOpener
import com.example.batteryalarm.update.DataStoreUpdateCheckAttemptStore
import com.example.batteryalarm.update.GitHubLatestReleaseSource
import com.example.batteryalarm.update.GitHubReleaseUrl
import com.example.batteryalarm.update.PackageManagerInstalledAppVersionName
import com.example.batteryalarm.update.UpdateConfig
import com.example.batteryalarm.update.UpdateOpener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UpdateModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = GitHubLatestReleaseSource.defaultClient()

    @Provides
    @Singleton
    fun provideUpdateConfig(): UpdateConfig = UpdateConfig(
        enabled = BuildConfig.UPDATE_CHECKER_ENABLED,
        githubOwner = BuildConfig.UPDATE_GITHUB_OWNER,
        githubRepo = BuildConfig.UPDATE_GITHUB_REPO,
        githubApiBase = BuildConfig.UPDATE_GITHUB_API_BASE.trimEnd('/'),
    )

    @Provides
    @Singleton
    fun provideUpdateCheckerEnabled(
        updateConfig: UpdateConfig,
    ): UpdateCheckerEnabled = updateConfig

    @Provides
    @Singleton
    fun provideUpdateCheckAttemptStore(
        @ApplicationContext context: Context,
    ): UpdateCheckAttemptStore = DataStoreUpdateCheckAttemptStore(context)

    @Provides
    @Singleton
    fun provideLatestStableReleaseSource(
        updateConfig: UpdateConfig,
        okHttpClient: OkHttpClient,
    ): LatestStableReleaseSource = GitHubLatestReleaseSource(
        updateConfig = updateConfig,
        okHttpClient = okHttpClient,
    )

    @Provides
    @Singleton
    fun provideInstalledAppVersionName(
        @ApplicationContext context: Context,
    ): InstalledAppVersionName = PackageManagerInstalledAppVersionName(context)

    @Provides
    @Singleton
    fun provideCheckForAppUpdate(
        isEnabled: UpdateCheckerEnabled,
        attemptStore: UpdateCheckAttemptStore,
        releaseSource: LatestStableReleaseSource,
        installedVersionName: InstalledAppVersionName,
        updateConfig: UpdateConfig,
    ): CheckForAppUpdate = CheckForAppUpdate(
        isEnabled = isEnabled,
        attemptStore = attemptStore,
        releaseSource = releaseSource,
        installedVersionName = installedVersionName,
        isAllowedReleasePageUrl = { url ->
            GitHubReleaseUrl.isAllowedReleasePageUrl(
                url = url,
                owner = updateConfig.githubOwner,
                repo = updateConfig.githubRepo,
            )
        },
    )

    @Provides
    @Singleton
    fun provideUpdateOpener(
        @ApplicationContext context: Context,
        updateConfig: UpdateConfig,
    ): UpdateOpener = AndroidUpdateOpener(
        appContext = context,
        isAllowedUrl = { url ->
            GitHubReleaseUrl.isAllowedReleasePageUrl(
                url = url,
                owner = updateConfig.githubOwner,
                repo = updateConfig.githubRepo,
            )
        },
    )
}
