package de.no3x.filetimesync.ui

import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import de.no3x.filetimesync.domain.FileTimestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateJsonScreen(onExported: (Uri?) -> Unit) {
    var status by remember { mutableStateOf("") }
    var exporting by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var selectedDir by remember { mutableStateOf<File?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        StorageDropdown(
            selectedDir = selectedDir,
            onDirSelected = { selectedDir = it }
        )
        selectedDir?.let {
            Spacer(Modifier.height(8.dp))
            Text(it.absolutePath)
            Spacer(Modifier.height(16.dp))
        }
        Button(
            onClick = {
                if (!exporting) {
                    coroutineScope.launch {
                        exporting = true
                        progress = 0f
                        status = "Scanning files..."
                        val sdCard = selectedDir ?: File("/storage/emulated/0")
                        if (!sdCard.exists() || !sdCard.isDirectory) {
                            status = "SD card not found!"
                            exporting = false
                            return@launch
                        }

                        // Gather files on IO dispatcher
                        val files = withContext(Dispatchers.IO) {
                            sdCard.walkTopDown()
                                .filter { it.isFile }
                                .filter {
                                    if (it.path.contains("Android/media")) {
                                        it.path.contains("WhatsApp/Media")
                                    } else true
                                }
                                .toList()
                        }
                        val total = files.size.coerceAtLeast(1)
                        status = "Found $total files. Building list..."

                        val fileTimestamps = mutableListOf<FileTimestamp>()

                        // Use IO dispatcher for processing, but switch to Main for UI updates
                        withContext(Dispatchers.IO) {
                            files.forEachIndexed { idx, it ->
                                fileTimestamps += FileTimestamp(it.relativeTo(sdCard).path, it.lastModified())
                                if (idx % 50 == 0 || idx == total - 1) {
                                    val prog = idx.toFloat() / total
                                    withContext(Dispatchers.Main) { progress = prog }
                                }
                            }
                        }
                        withContext(Dispatchers.Main) { progress = 1f }

                        val jsonString = Json.encodeToString(fileTimestamps)
                        status = "Saving JSON..."
                        val exportFile = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            "file_timestamps.json"
                        )
                        try {
                            withContext(Dispatchers.IO) {
                                exportFile.writeText(jsonString)
                            }
                            status = "Exported to: ${exportFile.absolutePath}"
                            onExported(
                                FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    exportFile
                                )
                            )
                        } catch (e: Exception) {
                            status = "Export failed: ${e.message}"
                            onExported(null)
                        }
                        exporting = false
                    }
                }
            },
            enabled = !exporting
        ) {
            Text("Export file timestamps")
        }
        if (exporting) {
            Spacer(Modifier.height(16.dp))
            AnimatedGlowingProgressBar(
                progress = progress,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(status)
    }
}
