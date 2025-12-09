package com.ichi2.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.ichi2.anki.CollectionManager
import com.ichi2.anki.NoteEditorActivity
import com.ichi2.anki.R
import timber.log.Timber
import java.util.Calendar

class HeatmapWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                var heatmapData by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }

                LaunchedEffect(Unit) {
                    heatmapData = fetchHeatmapData()
                }

                HeatmapContent(heatmapData, context)
            }
        }
    }

    @Composable
    internal fun HeatmapContent(data: Map<Long, Int>, context: Context) {
        val size = LocalSize.current
        // Estimate number of weeks that can fit.
        // Assume each cell is around 12.dp with 2.dp gap -> 14.dp total width per column
        // We leave some padding for the container
        val availableWidth = size.width - 32.dp // 16dp padding each side
        val numWeeks = (availableWidth.value / 14).toInt().coerceAtLeast(1)

        Column(
            modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.background)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = context.getString(R.string.app_name), style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                // Add Button
                Box(
                    modifier = GlanceModifier.size(32.dp).background(GlanceTheme.colors.primary)
                        .clickable(actionStartActivity<NoteEditorActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_add),
                        contentDescription = "Add Card",
                        modifier = GlanceModifier.size(20.dp),
                        colorFilter = androidx.glance.ColorFilter.tint(GlanceTheme.colors.onPrimary)
                    )
                }
            }

            Spacer(GlanceModifier.height(16.dp))

            // Heatmap Grid
            // We want last 'numWeeks' weeks.
            // Rows: 7 (Sun-Sat)
            // Cols: numWeeks

            val today = System.currentTimeMillis()
            // Normalize to day index
            val dayMillis = 86400000L
            val currentDayIdx = today / dayMillis

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                for (w in (numWeeks - 1) downTo 0) {
                    Column(modifier = GlanceModifier.padding(end = 2.dp)) {
                        for (d in 0..6) {
                            // d=0 is Sunday?
                            // We need to calculate the day index for this cell.
                            val calendar = Calendar.getInstance()
                            val todayDow =
                                calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 (Sun) - 6 (Sat)

                            val weeksFromEnd = (numWeeks - 1) - w
                            val dayOffset = (weeksFromEnd * 7) + (todayDow - d)
                            val targetDayIdx = currentDayIdx - dayOffset

                            val count = data[targetDayIdx] ?: 0
                            val color = getColorForCount(count)

                            Box(
                                modifier = GlanceModifier.size(10.dp) // Cell size
                                    .background(color)
                            ) {}
                            Spacer(GlanceModifier.height(2.dp))
                        }
                    }
                }
            }
        }
    }

    companion object HeatmapLogic {
        fun getColorForCount(count: Int): Color {
            // Simple thresholding
            return when {
                count == 0 -> Color(0xFFE0E0E0) // Light grey for empty
                count <= 5 -> Color(0xFF9BE9A8)
                count <= 10 -> Color(0xFF40C463)
                count <= 20 -> Color(0xFF30A14E)
                else -> Color(0xFF216E39)
            }
        }

        suspend fun fetchHeatmapData(): Map<Long, Int> {
            return try {
                CollectionManager.withCol {
                    val data = mutableMapOf<Long, Int>()
                    // revlog id is in milliseconds
                    val query =
                        "SELECT CAST(id/86400000 AS INTEGER) as day, count() FROM revlog GROUP BY day"

                    // We use useCursor from DB object if available or raw access
                    // Collection has db property.
                    val cursor = this.db.query(query)
                    cursor.use { c ->
                        while (c.moveToNext()) {
                            val day = c.getLong(0)
                            val count = c.getInt(1)
                            data[day] = count
                        }
                    }
                    data
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch heatmap data")
                emptyMap()
            }
        }
    }
}

@Preview
@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
fun HeatmapWidgetPreview() {
    val context = androidx.glance.LocalContext.current
    // Generate dummy data
    val today = System.currentTimeMillis() / 86400000L
    val dummyData = mutableMapOf<Long, Int>()
    // Fill some days
    for (i in 0..100) {
        if (i % 3 == 0) {
             dummyData[today - i] = (1..25).random()
        }
    }
    
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSize provides androidx.compose.ui.unit.DpSize(300.dp, 200.dp)
    ) {
        HeatmapWidget().HeatmapContent(dummyData, context)
    }
}

