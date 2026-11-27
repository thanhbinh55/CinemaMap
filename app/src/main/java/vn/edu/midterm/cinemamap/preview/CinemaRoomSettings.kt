package vn.edu.midterm.cinemamap.preview

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import vn.edu.midterm.cinemamap.R
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

internal object CinemaRoomSettings {
    private const val PREFS = "cinema_preview_room"
    private val keys = listOf("room.width", "screen.width", "first.distance", "row.spacing", "row.rise", "eye.height")

    fun load(context: Context, saved: Bundle?): CinemaRoomSpec {
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val defaults = values(CinemaRoomSpec())
        return try {
            val fromBundle = saved?.containsKey(keys[0]) == true
            val v = keys.mapIndexed { index, key -> if (fromBundle) saved!!.getFloat(key, defaults[index]) else preferences.getFloat(key, defaults[index]) }
            CinemaRoomSpec(v[0], v[1], v[2], v[3], v[4], v[5],
                if (fromBundle) saved!!.getBoolean("room.lighting", true) else preferences.getBoolean("room.lighting", true))
        } catch (_: RuntimeException) {
            CinemaRoomSpec()
        }
    }

    fun storeState(bundle: Bundle, spec: CinemaRoomSpec) {
        keys.zip(values(spec)).forEach { (key, value) -> bundle.putFloat(key, value) }
        bundle.putBoolean("room.lighting", spec.enhancedLighting)
    }

    fun save(context: Context, spec: CinemaRoomSpec) {
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        keys.zip(values(spec)).forEach { (key, value) -> editor.putFloat(key, value) }
        editor.putBoolean("room.lighting", spec.enhancedLighting).apply()
    }

    private fun values(spec: CinemaRoomSpec) = listOf(spec.roomWidth, spec.screenWidth, spec.firstRowDistance, spec.rowSpacing, spec.rowRise, spec.eyeHeight)

    fun show(context: Context, current: CinemaRoomSpec, onApply: (CinemaRoomSpec) -> Unit) {
        val density = context.resources.displayMetrics.density
        val body = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((20 * density).toInt(), (8 * density).toInt(), (20 * density).toInt(), 0)
        }
        val values = values(current).toMutableList()
        val format = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).apply { maximumFractionDigits = 2 }
        val labels = listOf(R.string.room_width, R.string.room_screen_width, R.string.room_first_row, R.string.room_spacing, R.string.room_rise, R.string.room_eye_height)
        val ranges = listOf(16f to 24f, 8f to 14f, 3f to 7f, .9f to 1.5f, .12f to .24f, .95f to 1.35f)
        val steps = listOf(.1f, .1f, .1f, .01f, .01f, .01f)
        body.addView(TextView(context).apply { setText(R.string.room_mock_notice) })
        labels.forEachIndexed { index, label ->
            val text = TextView(context).apply {
                textSize = 14f
                setPadding(0, (12 * density).toInt(), 0, 0)
                text = context.getString(R.string.room_value, context.getString(label), format.format(values[index]))
            }
            body.addView(text)
            body.addView(SeekBar(context).apply {
                contentDescription = context.getString(label)
                max = ((ranges[index].second - ranges[index].first) / steps[index]).roundToInt()
                progress = ((values[index] - ranges[index].first) / steps[index]).roundToInt()
                minimumHeight = (48 * density).toInt()
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
                    override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        values[index] = (ranges[index].first + progress * steps[index]).coerceIn(ranges[index].first, ranges[index].second)
                        text.text = context.getString(R.string.room_value, context.getString(label), format.format(values[index]))
                    }
                })
            })
        }
        val lighting = MaterialCheckBox(context).apply {
            setText(R.string.room_lighting)
            isChecked = current.enhancedLighting
        }
        body.addView(lighting)
        val scroll = ScrollView(context).apply { addView(body) }
        val wrapper = FrameLayout(context).apply {
            addView(scroll, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                minOf(context.resources.displayMetrics.heightPixels / 2, (440 * density).toInt())))
        }
        MaterialAlertDialogBuilder(context).setTitle(R.string.room_settings_title)
            .setView(wrapper)
            .setNegativeButton(R.string.action_cancel, null)
            .setNeutralButton(R.string.room_defaults) { _, _ -> onApply(CinemaRoomSpec()) }
            .setPositiveButton(R.string.action_apply) { _, _ ->
                onApply(CinemaRoomSpec(values[0], values[1], values[2], values[3], values[4], values[5], lighting.isChecked))
            }.show()
    }
}
