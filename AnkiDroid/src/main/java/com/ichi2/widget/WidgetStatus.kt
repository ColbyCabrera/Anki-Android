/***************************************************************************************
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package com.ichi2.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * The status of the widget.
 */
object WidgetStatus {
    private var updateJob: Job? = null

    fun updateInBackground(context: Context) {
        val canExecuteTask = updateJob == null || updateJob?.isActive == false

        if (canExecuteTask) {
            Timber.d("WidgetStatus.update(): updating")
            updateJob = launchUpdateJob(context)
        } else {
            Timber.d("WidgetStatus.update(): already running")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun launchUpdateJob(context: Context): Job = GlobalScope.launch {
        try {
            // Update Heatmap Widget
            if (GlanceAppWidgetManager(context).getGlanceIds(HeatmapWidget::class.java)
                    .isNotEmpty()
            ) {
                HeatmapWidget().updateAll(context)
            }

            Timber.v("launchUpdateJob completed")
        } catch (exc: Exception) {
            Timber.w(exc, "failure in widget update")
        }
    }
}
