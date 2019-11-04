package com.purplekraken.loadguard.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
import android.media.AudioAttributes.USAGE_ALARM
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_REQUEST_FAILED
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import androidx.media.AudioManagerCompat.AUDIOFOCUS_GAIN_TRANSIENT

internal object RingtonePlayer {
    private const val TAG = "RingtonePlayer"
    private val handlerThread = HandlerThread(TAG)
    private val handler: Handler
    private var playbackSession: PlaybackSession? = null

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    private fun getAudioManager(ctx: Context): AudioManager {
        return ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun play(ctx: Context) {
        val appCtx = ctx.applicationContext
        handler.post {
            if (playbackSession == null) {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val ringtone = RingtoneManager.getRingtone(appCtx, uri)
                if (ringtone != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        // TODO: how to loop on <= 28 ?
                        ringtone.isLooping = true
                    }
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(USAGE_ALARM)
                        .setContentType(CONTENT_TYPE_SONIFICATION)
                        .build()
                    ringtone.audioAttributes = audioAttributes
                    val audioManager =
                        getAudioManager(appCtx)
                    val compatAttributes = AudioAttributesCompat.wrap(audioAttributes)
                    if (compatAttributes != null) {
                        val audioFocusRequest =
                            AudioFocusRequestCompat.Builder(AUDIOFOCUS_GAIN_TRANSIENT)
                                .setAudioAttributes(compatAttributes)
                                .setOnAudioFocusChangeListener({}, handler)
                                .build()
                        val result =
                            AudioManagerCompat.requestAudioFocus(audioManager, audioFocusRequest)
                        if (result == AUDIOFOCUS_REQUEST_FAILED) {
                            Log.e(TAG, "audio focus request failed")
                        } else {
                            ringtone.play()
                            playbackSession =
                                PlaybackSession(
                                    ringtone,
                                    audioFocusRequest
                                )
                        }
                    } else {
                        Log.e(TAG, "failed to convert AudioAttributes to AudioAttributesCompat")
                    }
                } else {
                    Log.e(TAG, "failed to get ringtone \"$uri\"")
                }
            }
        }
    }

    fun stop(ctx: Context) {
        handler.post {
            playbackSession?.stop(ctx)
            playbackSession = null
        }
    }

    private class PlaybackSession(
        val ringtone: Ringtone,
        val audioFocusRequest: AudioFocusRequestCompat
    ) {
        fun stop(ctx: Context) {
            val appCtx = ctx.applicationContext
            with(ringtone) {
                if (isPlaying) {
                    stop()
                }
            }
            AudioManagerCompat.abandonAudioFocusRequest(
                getAudioManager(
                    appCtx
                ), audioFocusRequest)
        }
    }
}
