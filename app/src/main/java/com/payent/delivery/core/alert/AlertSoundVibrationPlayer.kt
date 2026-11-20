package com.payent.delivery.core.alert

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.payent.delivery.R

interface AlertPlayer {
    fun startAlert(soundEnabled: Boolean = true, vibrationEnabled: Boolean = true)
    fun stopAlert()
    fun isAlertPlaying(): Boolean
}

/**
 * Manages foreground audio alert playback and vibration patterns for new order assignments.
 * Enforces single active playback, audio focus management, and DND / ringer mode compliance.
 */
class AlertSoundVibrationPlayer(
    private val context: Context
) : AlertPlayer {
    companion object {
        private const val TAG = "AlertSoundPlayer"

        // Repeating vibration waveform: [wait, vibrate, pause, vibrate, pause, long vibrate, pause]
        val REPEATING_VIBRATION_PATTERN = longArrayOf(0, 450, 200, 450, 200, 700, 400)
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var isPlaying = false

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    /**
     * Starts looping audio alert and repeating vibration, respecting user settings and device sound profiles.
     */
    @Synchronized
    override fun startAlert(
        soundEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        // Stop any running playback first to guarantee single playback loop
        stopAlert()

        val ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
        val isDndActive = isDoNotDisturbActive()

        // 1. Handle Vibration
        if (vibrationEnabled && !isDndActive && ringerMode != AudioManager.RINGER_MODE_SILENT) {
            startVibration()
        }

        // 2. Handle Audio Playback
        if (soundEnabled && !isDndActive && ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            startAudioPlayback()
        }

        isPlaying = true
    }

    /**
     * Stops audio and vibration immediately and releases all system resources.
     */
    @Synchronized
    override fun stopAlert() {
        stopAudioPlayback()
        stopVibration()
        isPlaying = false
    }

    override fun isAlertPlaying(): Boolean = isPlaying

    private fun startVibration() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                val vibrationEffect = VibrationEffect.createWaveform(
                    REPEATING_VIBRATION_PATTERN,
                    0 // Repeat from index 0 until cancelled
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val combinedAttributes = android.os.VibrationAttributes.Builder()
                        .setUsage(android.os.VibrationAttributes.USAGE_RINGTONE)
                        .build()
                    vibrator.vibrate(vibrationEffect, combinedAttributes)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(vibrationEffect, audioAttributes)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start vibration: ${e.message}")
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel vibration: ${e.message}")
        }
    }

    private fun startAudioPlayback() {
        try {
            // Request Audio Focus
            val focusGranted = requestAudioFocus()
            if (!focusGranted) {
                Log.w(TAG, "Audio focus request rejected, skipping sound playback.")
                return
            }

            val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.order_alert}")
            val player = MediaPlayer()
            player.setAudioAttributes(audioAttributes)
            player.setDataSource(context, soundUri)
            player.isLooping = true
            player.setOnPreparedListener { mp ->
                mp.start()
            }
            player.setOnErrorListener { mp, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                stopAudioPlayback()
                true
            }
            player.prepareAsync()
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio playback: ${e.message}")
            stopAudioPlayback()
        }
    }

    private fun stopAudioPlayback() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.reset()
                player.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer: ${e.message}")
        } finally {
            mediaPlayer = null
            abandonAudioFocus()
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (audioManager == null) return false
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(false)
            .setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS,
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        // Pause or stop when losing focus
                        try {
                            if (mediaPlayer?.isPlaying == true) {
                                mediaPlayer?.pause()
                            }
                        } catch (_: Exception) {}
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        try {
                            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                                mediaPlayer?.start()
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
            .build()
        audioFocusRequest = request
        val result = audioManager.requestAudioFocus(request)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        try {
            audioFocusRequest?.let { request ->
                audioManager?.abandonAudioFocusRequest(request)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error abandoning audio focus: ${e.message}")
        } finally {
            audioFocusRequest = null
        }
    }

    private fun isDoNotDisturbActive(): Boolean {
        val filter = notificationManager?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
        return filter == NotificationManager.INTERRUPTION_FILTER_NONE ||
                filter == NotificationManager.INTERRUPTION_FILTER_ALARMS
    }
}
