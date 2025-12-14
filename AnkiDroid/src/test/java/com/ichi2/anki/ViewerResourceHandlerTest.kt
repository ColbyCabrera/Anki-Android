package com.ichi2.anki

import android.net.Uri
import android.webkit.WebResourceRequest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ichi2.anki.libanki.CollectionFiles
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.annotation.Config
import java.io.File
import java.nio.charset.StandardCharsets

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ViewerResourceHandlerTest {

    private lateinit var tempDir: File
    private lateinit var ankiDroidDir: File
    private lateinit var mediaDir: File
    private lateinit var secretFile: File

    @Before
    fun setUp() {
        // Create a temp directory
        tempDir = File(System.getProperty("java.io.tmpdir"), "anki_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        // Create AnkiDroid directory
        ankiDroidDir = File(tempDir, "AnkiDroid")
        ankiDroidDir.mkdirs()

        // Create collection.media directory
        mediaDir = File(ankiDroidDir, "collection.media")
        mediaDir.mkdirs()

        // Set the override
        CollectionHelper.ankiDroidDirectoryOverride = ankiDroidDir

        // Create a secret file outside the media directory (in tempDir)
        secretFile = File(tempDir, "secret.txt")
        secretFile.writeText("This is secret data", StandardCharsets.UTF_8)
    }

    @After
    fun tearDown() {
        CollectionHelper.ankiDroidDirectoryOverride = null
        tempDir.deleteRecursively()
    }

    @Test
    fun testPathTraversalVulnerability() {
        // Setup ViewerResourceHandler
        val context = ApplicationProvider.getApplicationContext<AnkiDroidApp>()
        val handler = ViewerResourceHandler(context)

        // Construct a path that traverses up from mediaDir to secretFile
        // mediaDir is tempDir/AnkiDroid/collection.media
        // we want to access tempDir/secret.txt
        // Path should be ../../secret.txt
        val maliciousPath = "../../secret.txt"

        val uri = mock<Uri> {
            on { path } doReturn maliciousPath
        }

        val request = mock<WebResourceRequest> {
            on { url } doReturn uri
            on { method } doReturn "GET"
        }

        // Execute
        val response = handler.shouldInterceptRequest(request)

        // Verify
        // If fixed, response should be null (blocked)
        if (response != null) {
             val content = response.data.bufferedReader().use { it.readText() }
             if (content == "This is secret data") {
                 fail("Vulnerability confirmed! Secret file was accessed.")
             } else {
                 // Might be some other response, but if not null, it's suspicious if we expected block.
                 // However, the handler returns null if file not found.
                 // If path traversal is blocked, it returns null.
                 // If it returns something, it must be the file content (which we checked) or something else.
             }
        }

        assertNull("Expected path traversal request to be blocked (return null)", response)
    }
}
