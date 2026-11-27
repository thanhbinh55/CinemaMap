package vn.edu.midterm.cinemamap.preview

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.Surface
import vn.edu.midterm.cinemamap.R

/** UI-thread owner of the decoder. The GL screen owns and releases its Surface. */
internal class CinemaTrailerPlayer(
    context: Context,
    startPositionMs: Int,
    initiallyPlaying: Boolean,
    private val onState: (ready: Boolean, playing: Boolean, failed: Boolean) -> Unit
) {
    private val appContext = context.applicationContext
    private var player: MediaPlayer? = null
    private var surface: Surface? = null
    private var active = false
    private var prepared = false
    private var seeking = false
    private var released = false
    private var failed = false
    private var savedPositionMs = startPositionMs.coerceIn(0, DURATION_MS - 1)
    var playRequested = initiallyPlaying
        private set

    fun onResume() {
        active = true
        // Wait for GL to announce a surface belonging to the current EGL context.
    }

    fun attachSurface(output: Surface) {
        if (released || failed || !active) return
        if (!output.isValid) return
        try {
            surface = output
            if (player == null) preparePlayer() else {
                player?.setSurface(output)
                applyPlayback()
            }
        } catch (error: RuntimeException) {
            fail(error)
        }
    }

    private fun preparePlayer() {
        val media = MediaPlayer()
        player = media
        media.setOnPreparedListener {
            if (released || player !== it) return@setOnPreparedListener
            prepared = true
            if (savedPositionMs > 0) {
                seeking = true
                it.seekTo(savedPositionMs.toLong(), MediaPlayer.SEEK_CLOSEST)
            } else applyPlayback()
        }
        media.setOnSeekCompleteListener {
            if (released || player !== it) return@setOnSeekCompleteListener
            seeking = false
            applyPlayback()
        }
        media.setOnErrorListener { _, what, extra ->
            if (!released && player === media) fail(IllegalStateException("MediaPlayer error $what/$extra"))
            true
        }
        media.setDataSource(appContext, Uri.parse("android.resource://${appContext.packageName}/${R.raw.campfire_loop}"))
        media.setSurface(surface)
        media.isLooping = true
        media.setVolume(0f, 0f)
        media.prepareAsync()
    }

    fun togglePlayback() {
        if (!prepared || failed || released) return
        playRequested = !playRequested
        applyPlayback()
    }

    fun pauseForComparison() {
        playRequested = false
        applyPlayback()
    }

    private fun applyPlayback() {
        val media = player ?: return
        if (!prepared || released || failed) return
        try {
            if (!seeking && active && surface != null && playRequested) {
                media.start()
            } else if (media.isPlaying) media.pause()
            onState(!seeking, media.isPlaying, false)
        } catch (error: IllegalStateException) {
            fail(error)
        }
    }

    fun positionMs(): Int {
        if (prepared && !released && !failed) {
            try {
                savedPositionMs = player?.currentPosition ?: savedPositionMs
            } catch (_: IllegalStateException) {
                // Keep the last saved position if a decoder error races with lifecycle saving.
            }
        }
        return savedPositionMs
    }

    fun onPause() {
        active = false
        positionMs()
        // Recreate on resume: a decoder attached to an old Surface can stall.
        player?.release()
        player = null
        prepared = false
        seeking = false
        surface = null
        onState(false, false, false)
    }

    fun fail(error: Exception) {
        if (released || failed) return
        Log.e("CinemaPreview", "Trailer playback unavailable", error)
        failed = true
        player?.release()
        player = null
        onState(false, false, true)
    }

    fun release() {
        released = true
        player?.release()
        player = null
        surface = null
    }

    companion object {
        const val DURATION_MS = 5_000
    }
}
