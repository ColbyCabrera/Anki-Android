package com.ichi2.anki.jsaddons

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ichi2.anki.RobolectricTest
import org.apache.commons.compress.archivers.ArchiveException
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@RunWith(AndroidJUnit4::class)
class TgzPackageExtractSecurityTest : RobolectricTest() {

    @Test
    fun testZipSlipPartialPathVulnerability() {
        // TgzPackageExtract requires a Context.
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val extractor = TgzPackageExtract(context)

        // Reflectively access the private 'zipPathSafety' method.
        val method: Method = TgzPackageExtract::class.java.getDeclaredMethod("zipPathSafety", File::class.java, File::class.java)
        method.isAccessible = true

        val tmpDirPath = System.getProperty("java.io.tmpdir") ?: "/tmp"
        val tmpDir = File(tmpDirPath)

        // Define a legitimate destination directory.
        val safeDir = File(tmpDir, "safe_dir")
        safeDir.mkdirs()

        // Define a malicious file path that sits in a sibling directory sharing the prefix.
        // dest: /tmp/safe_dir
        // malicious: /tmp/safe_dir_suffix/evil.txt
        val maliciousDir = File(tmpDir, "safe_dir_suffix")
        val maliciousFile = File(maliciousDir, "evil.txt")

        try {
            // This SHOULD throw an ArchiveException if secure.
            method.invoke(extractor, maliciousFile, safeDir)

            // If we reach here, the check passed (Vulnerability Exists).
            fail("Vulnerability Reproduction: zipPathSafety failed to catch partial path traversal (Sibling Directory Attack)!")
        } catch (e: InvocationTargetException) {
             if (e.targetException is ArchiveException) {
                 // Expected exception when fixed.
                 return
             }
             throw e.targetException
        } finally {
            safeDir.deleteRecursively()
            maliciousDir.deleteRecursively()
        }
    }
}
