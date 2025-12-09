package com.ichi2.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProviders
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
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

        // Approximate layout calculations
        // Left Labels: ~25dp
        // Right Panel: ~120dp
        // Padding: 16dp (8 start + 8 end)
        // Gap between sections: 16dp
        // Total reserved width: 25 + 120 + 16 + 16 = ~177dp
        val availableWidth = size.width - 180.dp

        // Cell width 10.dp + 2.dp gap = 12.dp
        val numWeeks = (availableWidth.value / 12).toInt().coerceAtLeast(1)

        val today = System.currentTimeMillis()
        val dayMillis = 86400000L
        val currentDayIndex = today / dayMillis

        // Calculate ISO Day of Week (0 = Mon, 6 = Sun)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = today
        // Calendar.DAY_OF_WEEK: Sun=1, Mon=2, ... Sat=7
        // Convert to Mon=0, ... Sun=6
        val dow = calendar.get(Calendar.DAY_OF_WEEK)
        val todayDoW = (dow + 5) % 7

        val todayCount = data[currentDayIndex] ?: 0

        Row(
            modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.background)
                .padding(8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Left Section: Heatmap ---
            // Removed Alignment.CenterVertically to prevent vertical clipping if content is tall
            Row(
                modifier = GlanceModifier.defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day Labels
                // Note: Using wrapper Boxes instead of Spacers to reduce child count.
                // Glance/RemoteViews has a per-container child limit (~10), so we embed
                // spacing in the Box height (12.dp = 10.dp content + 2.dp gap).
                Column(
                    modifier = GlanceModifier.padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    days.forEachIndexed { index, day ->
                        Box(
                            modifier = GlanceModifier.height(if (index < 6) 12.dp else 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = day, style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant, fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Grid
                // Note: Same wrapper Box approach as day labels to reduce child count
                Row {
                    for (w in (numWeeks - 1) downTo 0) {
                        Column(
                            modifier = GlanceModifier.padding(end = 2.dp)
                        ) {
                            for (d in 0..6) {
                                val dayOffset = (w * 7) + (todayDoW - d)
                                val checkDayIndex = currentDayIndex - dayOffset
                                val count = data[checkDayIndex] ?: 0
                                val (colorProvider, alpha) = getColorForCount(
                                    count,
                                    GlanceTheme.colors
                                )

                                // Wrapper Box with built-in spacing (12.dp = 10.dp cell + 2.dp gap)
                                Box(
                                    modifier = GlanceModifier.height(if (d < 6) 12.dp else 10.dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Box(
                                        modifier = GlanceModifier.size(10.dp).background(
                                                colorProvider.getColor(context).copy(alpha = alpha)
                                            ).cornerRadius(2.dp)
                                    ) {}
                                }
                            }
                        }
                    }
                }
            }

            Spacer(GlanceModifier.width(16.dp))

            // --- Right Section: Info & Action ---
            Column(
                horizontalAlignment = Alignment.End, modifier = GlanceModifier.fillMaxHeight()
            ) {
                // Star Icon in Circle
                Box(
                    modifier = GlanceModifier.size(40.dp)
                        .background(GlanceTheme.colors.surfaceVariant).cornerRadius(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_star_white),
                        contentDescription = "Star",
                        modifier = GlanceModifier.size(24.dp),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
                    )
                }

                Spacer(GlanceModifier.height(8.dp))

                Text(
                    text = "Anki", style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "$todayCount cards reviewed", style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp
                    )
                )

                Spacer(GlanceModifier.defaultWeight())

                // Add Card Button
                Box(
                    modifier = GlanceModifier.background(GlanceTheme.colors.surfaceVariant)
                        .cornerRadius(20.dp).clickable(actionStartActivity<NoteEditorActivity>())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            provider = ImageProvider(R.drawable.add_24px),
                            contentDescription = null,
                            modifier = GlanceModifier.size(16.dp),
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        Text(
                            text = "ADD CARD", style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }

    companion object HeatmapLogic {
        /**
         * Returns the base color and alpha for the heatmap cell.
         */
        fun getColorForCount(count: Int, colors: ColorProviders): Pair<ColorProvider, Float> {
            return when {
                count == 0 -> colors.surfaceVariant to 0.5f
                count <= 5 -> colors.primary to 0.25f
                count <= 20 -> colors.primary to 0.5f
                count <= 40 -> colors.primary to 0.8f
                else -> colors.primary to 1f
            }
        }

        suspend fun fetchHeatmapData(): Map<Long, Int> {
            return try {
                CollectionManager.withCol {
                    val data = mutableMapOf<Long, Int>()
                    // revlog id is in milliseconds
                    val query =
                        "SELECT CAST(id/86400000 AS INTEGER) as day, count() FROM revlog GROUP BY day"

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

@Preview(widthDp = 300, heightDp = 180)
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
            dummyData[today - i] = 0
        }
        if (i % 2 == 0) {
            dummyData[today - i] = 1
        }
        if (i % 3 == 0) {
            dummyData[today - i] = 6
        }
        if (i % 5 == 0) {
            dummyData[today - i] = 11
        }
        if (i % 11 == 0) {
            dummyData[today - i] = 21
        }
    }
    dummyData[today] = 294

    androidx.compose.runtime.CompositionLocalProvider(
        LocalSize provides androidx.compose.ui.unit.DpSize(300.dp, 400.dp)
    ) {
        HeatmapWidget().HeatmapContent(dummyData, context)
    }
}
