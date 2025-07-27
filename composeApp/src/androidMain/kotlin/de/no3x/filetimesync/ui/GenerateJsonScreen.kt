package de.no3x.filetimesync.ui

import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import de.no3x.filetimesync.domain.FileTimestamp
import kotlinx.serialization.json.Json
import java.io.File

fun collectFileTimestamps(dir: File, baseDir: File): List<FileTimestamp> =
    dir.walkTopDown()
        .filter { it.isFile }
        .filter {
            if (it.path.contains("Android/media")) {
                return@filter it.path.contains("WhatsApp/Media")
            }
            return@filter true
        }
        .map { FileTimestamp(it.relativeTo(baseDir).path, it.lastModified()) }
        .toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateJsonScreen(onExported: (Uri?) -> Unit) {
    var status by remember { mutableStateOf("") }
    var exporting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    fun exportTimestamps() {
        exporting = true
        // Choose root directory for full SD card, or use any path
        val sdCard = File("/storage/emulated/0") // You may need to detect this
        if (!sdCard.exists() || !sdCard.isDirectory) {
            status = "SD card not found!"
            exporting = false
            return
        }

        val fileTimestamps = collectFileTimestamps(sdCard, sdCard)
        val jsonString = Json.encodeToString(fileTimestamps)
        // Save to Downloads or SD card root, as permitted
        val exportFile =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "file_timestamps.json")
        try {
            exportFile.writeText(jsonString)
            status = "Exported to: ${exportFile.absolutePath}"
        } catch (e: Exception) {
            status = "Export failed: ${e.message}"
        }
        exporting = false
        onExported(FileProvider.getUriForFile(context, "${context.packageName}.provider", exportFile))
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { exportTimestamps() }, enabled = !exporting) {
            Text("Export all SD card file timestamps")
        }
        Spacer(Modifier.height(16.dp))
        Text(status)
    }
}
