package com.example.batteryalarm.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.batteryalarm.domain.update.InstalledAppVersionName

class PackageManagerInstalledAppVersionName(
    private val appContext: Context,
) : InstalledAppVersionName {
    override fun versionName(): String {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.packageManager.getPackageInfo(
                appContext.packageName,
                PackageManager.PackageInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            appContext.packageManager.getPackageInfo(appContext.packageName, 0)
        }
        return packageInfo.versionName.orEmpty()
    }
}
