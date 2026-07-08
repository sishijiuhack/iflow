package com.sishijiuhack.iflow.privacy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

class PrivacyBackupConfigTest {
    private val projectDir = Path.of("").toAbsolutePath()

    @Test
    fun manifestDisablesAndroidBackup() {
        val application = parseXml(projectDir.resolve("src/main/AndroidManifest.xml"))
            .getElementsByTagName("application")
            .item(0)

        assertEquals("false", application.attributes.getNamedItem("android:allowBackup").nodeValue)
    }

    @Test
    fun backupRulesExcludeLedgerDatabaseFiles() {
        val excludedPaths = parseXml(projectDir.resolve("src/main/res/xml/backup_rules.xml"))
            .getElementsByTagName("exclude")
            .toPathSet()

        assertTrue(excludedPaths.containsAll(databaseSidecarPaths))
    }

    @Test
    fun dataExtractionRulesExcludeLedgerDatabaseFilesFromCloudAndDeviceTransfer() {
        val document = parseXml(projectDir.resolve("src/main/res/xml/data_extraction_rules.xml"))
        val cloudBackupPaths = document.getElementsByTagName("cloud-backup")
            .item(0)
            .childNodes
            .toPathSet()
        val deviceTransferPaths = document.getElementsByTagName("device-transfer")
            .item(0)
            .childNodes
            .toPathSet()

        assertTrue(cloudBackupPaths.containsAll(databaseSidecarPaths))
        assertTrue(deviceTransferPaths.containsAll(databaseSidecarPaths))
    }

    private fun parseXml(path: Path) = Files.newInputStream(path).use { input ->
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input)
    }

    private fun org.w3c.dom.NodeList.toPathSet(): Set<String> {
        return (0 until length)
            .map { item(it) }
            .filter { it.nodeName == "exclude" }
            .map { node ->
                val domain = node.attributes.getNamedItem("domain").nodeValue
                val path = node.attributes.getNamedItem("path").nodeValue
                "$domain/$path"
            }
            .toSet()
    }

    private companion object {
        val databaseSidecarPaths = setOf(
            "database/iflow.db",
            "database/iflow.db-wal",
            "database/iflow.db-shm",
            "database/iflow.db-journal",
        )
    }
}
