package com.example.batteryalarm.update

import android.content.Context
import android.content.Intent
import android.net.Uri

fun interface UpdateOpener {
    fun openReleasePage(url: String)
}

class AndroidUpdateOpener(
    private val appContext: Context,
    private val isAllowedUrl: (String) -> Boolean = { true },
) : UpdateOpener {
    override fun openReleasePage(url: String) {
        val trimmed = url.trim()
        if (trimmed.isEmpty() || !isAllowedUrl(trimmed)) {
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trimmed)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
        } catch (_: Exception) {
            // Fail silently; user can try again later.
        }
    }
}
