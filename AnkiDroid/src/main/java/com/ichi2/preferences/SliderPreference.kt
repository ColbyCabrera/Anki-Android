/*
 *  Copyright (c) 2023 Brayan Oliveira <brayandso.dev@gmail.com>
 *
 *  This program is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ichi2.preferences

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.withStyledAttributes
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.ichi2.anki.R
import com.ichi2.anki.common.annotations.NeedsTest
import com.ichi2.anki.ui.compose.preferences.SliderPreferenceContent
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme
import com.ichi2.anki.utils.getFormattedStringOrPlurals

/**
 * Similar to [androidx.preference.SeekBarPreference],
 * but with a material Slider instead of a SeekBar, and more customizable.
 */
@NeedsTest("onTouchListener is only called once")
class SliderPreference(
    context: Context,
    attrs: AttributeSet? = null,
) : Preference(context, attrs) {
    private var valueFrom: Int = 0
    private var valueTo: Int = 0
    private var stepSize: Float = 1F

    private var summaryFormatResource: Int? = null
    private var displayValue: Boolean = false
    private var displayFormat: String? = null

    var value: Int = valueFrom
        set(value) {
            if (field == value) {
                return
            }
            if (value < valueFrom || value > valueTo) {
                throw IllegalArgumentException("value $value should be between the min of $valueFrom and max of $valueTo")
            }
            field = value
            persistInt(value)
            notifyChanged()
        }

    init {
        layoutResource = R.layout.preference_slider

        context.withStyledAttributes(attrs, com.google.android.material.R.styleable.Slider) {
            valueFrom = getIntOrThrow(com.google.android.material.R.styleable.Slider_android_valueFrom)
            valueTo = getIntOrThrow(com.google.android.material.R.styleable.Slider_android_valueTo)
            stepSize = getFloat(com.google.android.material.R.styleable.Slider_android_stepSize, 1F)
        }

        context.withStyledAttributes(attrs, R.styleable.CustomPreference) {
            summaryFormatResource =
                getResourceId(R.styleable.CustomPreference_summaryFormat, 0)
                    .takeIf { it != 0 }
        }

        context.withStyledAttributes(attrs, R.styleable.SliderPreference) {
            displayFormat = getString(R.styleable.SliderPreference_displayFormat)
            displayValue = displayFormat != null ||
                getBoolean(R.styleable.SliderPreference_displayValue, false)
        }
    }

    override fun onGetDefaultValue(
        a: TypedArray,
        index: Int,
    ): Any = a.getInt(index, valueFrom)

    override fun onSetInitialValue(defaultValue: Any?) {
        value = getPersistedInt(defaultValue as Int? ?: valueFrom)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val composeView = holder.findViewById(R.id.compose_view) as ComposeView
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        composeView.setContent {
            AnkiDroidTheme {
                val iconPainter = icon?.let {
                    remember(it) {
                        androidx.compose.ui.graphics.painter.BitmapPainter(it.toBitmap().asImageBitmap())
                    }
                }

                // Calculate dynamic summary if needed
                val dynamicSummary = summaryFormatResource?.let {
                    context.getFormattedStringOrPlurals(it, value)
                } ?: summary?.toString()

                SliderPreferenceContent(
                    title = title?.toString() ?: "",
                    summary = dynamicSummary,
                    value = value,
                    valueFrom = valueFrom,
                    valueTo = valueTo,
                    stepSize = stepSize,
                    displayValue = displayValue,
                    displayFormat = displayFormat,
                    onValueChange = { newValue ->
                        if (newValue != value && callChangeListener(newValue)) {
                            value = newValue
                        }
                    },
                    icon = iconPainter,
                    isIconSpaceReserved = isIconSpaceReserved
                )
            }
        }
    }

    /**
     * Sets the callback to be invoked when this preference is changed by the user
     * (but before the internal state has been updated) on the internal onPreferenceChangeListener,
     * returning true on it by default
     * @param onPreferenceChangeListener The callback to be invoked
     */
    fun setOnPreferenceChangeListener(onPreferenceChangeListener: (newValue: Int) -> Unit) {
        setOnPreferenceChangeListener { _, newValue ->
            if (newValue !is Int) return@setOnPreferenceChangeListener false
            onPreferenceChangeListener(newValue)
            true
        }
    }
}
