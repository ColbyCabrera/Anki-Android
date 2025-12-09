package com.ichi2.widget

import android.database.Cursor
import androidx.compose.ui.graphics.Color
import com.ichi2.anki.CollectionManager
import com.ichi2.anki.libanki.Collection
import com.ichi2.anki.libanki.DB
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HeatmapWidgetTest {

    @Test
    fun testGetColorForCount() {
        assertEquals(Color(0xFFE0E0E0), HeatmapWidget.getColorForCount(0))
        assertEquals(Color(0xFF9BE9A8), HeatmapWidget.getColorForCount(1))
        assertEquals(Color(0xFF9BE9A8), HeatmapWidget.getColorForCount(5))
        assertEquals(Color(0xFF40C463), HeatmapWidget.getColorForCount(6))
        assertEquals(Color(0xFF40C463), HeatmapWidget.getColorForCount(10))
        assertEquals(Color(0xFF30A14E), HeatmapWidget.getColorForCount(11))
        assertEquals(Color(0xFF30A14E), HeatmapWidget.getColorForCount(20))
        assertEquals(Color(0xFF216E39), HeatmapWidget.getColorForCount(21))
    }

    @Test
    fun testFetchHeatmapData() = runTest {
        // Mock Cursor
        val mockCursor = mock<Cursor>()
        // Simulate 2 rows:
        // 1. day=100, count=5
        // 2. day=101, count=10
        // moveToNext returns true twice, then false
        whenever(mockCursor.moveToNext()).doReturn(true).doReturn(true).doReturn(false)
        whenever(mockCursor.getLong(0)).doReturn(100L).doReturn(101L)
        whenever(mockCursor.getInt(1)).doReturn(5).doReturn(10)

        // Mock DB
        val mockDb = mock<DB> {
            on { query(any(), any()) } doReturn mockCursor
            on { query(any()) } doReturn mockCursor
        }

        // Mock Collection
        val mockCol = mock<Collection> {
            on { db } doReturn mockDb
        }

        // Inject mock collection
        CollectionManager.setColForTests(mockCol)

        try {
            // Execute
            val result = HeatmapWidget.fetchHeatmapData()

            // Verify
            assertEquals(2, result.size)
            assertEquals(5, result[100L])
            assertEquals(10, result[101L])
        } finally {
            // Cleanup
            CollectionManager.setColForTests(null)
        }
    }
}
