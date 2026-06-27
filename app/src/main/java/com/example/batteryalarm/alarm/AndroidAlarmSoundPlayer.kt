package com.example.batteryalarm.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.batteryalarm.di.ApplicationScope
import com.example.batteryalarm.domain.AlarmSoundPlayer
import com.example.batteryalarm.domain.AlarmSoundSession
import com.example.batteryalarm.domain.AlarmSoundSessionPlayback
import com.example.batteryalarm.domain.VolumeRampController
import kotlinx.coroutines.CoroutineScope

class AndroidAlarmSoundPlayer(
    private val context: Context,
    @param:ApplicationScope private val scope: CoroutineScope,
) : AlarmSoundPlayer {
    private val lock = Any()
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val session = AlarmSoundSession(VolumeRampController(scope))
    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun startLooping() {
        synchronized(lock) {
            stopLocked()
            val alarmUri = defaultAlarmUri()
            if (alarmUri == null) {
                Log.w(TAG, "No default alarm sound available")
                return
            }

            requestAudioFocus()
            val player = MediaPlayer()
            try {
                player.setAudioAttributes(alarmAudioAttributes())
                player.setDataSource(context, alarmUri)
                player.isLooping = true
                player.setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error what=$what extra=$extra")
                    stopLocked()
                    true
                }
                player.prepare()
                mediaPlayer = player
                session.start(
                    MediaPlayerPlayback(
                        player = player,
                    ),
                )
                Log.d(TAG, "Started looping alarm sound uri=$alarmUri")
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to start alarm sound", exception)
                player.release()
                abandonAudioFocus()
            }
        }
    }

    override fun stop() {
        synchronized(lock) {
            stopLocked()
        }
    }

    private fun stopLocked() {
        session.stop()
        mediaPlayer = null
        abandonAudioFocus()
    }

    private fun defaultAlarmUri(): Uri? =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: Settings.System.DEFAULT_ALARM_ALERT_URI

    private fun alarmAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(alarmAudioAttributes())
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN,
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { request ->
                audioManager.abandonAudioFocusRequest(request)
            }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private class MediaPlayerPlayback(
        private val player: MediaPlayer,
    ) : AlarmSoundSessionPlayback {
        override fun beginLoopingFromSilence() {
            player.setVolume(0f, 0f)
            player.start()
        }

        override fun setVolume(volume: Float) {
            player.setVolume(volume, volume)
        }

        override fun tearDown() {
            player.runCatching {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        }
    }

    companion object {
        private const val TAG = "AndroidAlarmSoundPlayer"
    }
}
