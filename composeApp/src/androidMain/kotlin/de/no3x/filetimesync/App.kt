package de.no3x.filetimesync

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import de.no3x.filetimesync.domain.FileTimestamp
import de.no3x.filetimesync.ui.GenerateJsonScreen
import de.no3x.filetimesync.ui.share.DownloadAndProcessJsonScreen
import de.no3x.filetimesync.ui.share.ShareToDriveButton
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File

@Composable
@Preview
fun App() {
    MaterialTheme {
        val context = LocalContext.current
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = ("package:" + context.packageName).toUri()
            context.startActivity(intent)
        }
        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            var screen by remember { mutableStateOf(AppScreen.MAIN) }
            var exportedJsonUri by remember { mutableStateOf<Uri?>(null) }
            var lastProcessedCount by remember { mutableStateOf(0) }

            when (screen) {
                AppScreen.MAIN -> {
                    Column {
                        Button(onClick = {
                            screen = AppScreen.GENERATE_JSON
                        }) { Text("Export Timestamps (Old Device)") }
                        Button(onClick = {
                            screen = AppScreen.DOWNLOAD_AND_PROCESS_JSON
                        }) { Text("Import & Apply Timestamps (New Device)") }
                    }
                }

                AppScreen.GENERATE_JSON -> {
                    GenerateJsonScreen(
                        onExported = { uri ->
                            exportedJsonUri = uri
                            screen = AppScreen.SHARE_JSON
                        }
                    )
                }

                AppScreen.SHARE_JSON -> {
                    ShareToDriveButton(jsonUri = exportedJsonUri)
                    Button(onClick = { screen = AppScreen.MAIN }) { Text("Done / Back") }
                }


                AppScreen.DOWNLOAD_AND_PROCESS_JSON -> {
                    DownloadAndProcessJsonScreen { timestamps ->
                        val updated = updateTimestamps(timestamps)
                        lastProcessedCount = updated
                        screen = AppScreen.SUMMARY
                    }
                    Button(onClick = { screen = AppScreen.MAIN }) { Text("Back") }
                }

                AppScreen.SUMMARY -> {
                    Text("Updated timestamps for $lastProcessedCount files!")
                    Button(onClick = { screen = AppScreen.MAIN }) { Text("Back to Main") }
                }
            }
        }
    }
}

enum class AppScreen {
    MAIN, GENERATE_JSON, SHARE_JSON, DOWNLOAD_AND_PROCESS_JSON, SUMMARY
}

fun updateTimestamps(timestamps: List<FileTimestamp>): Int {
    // Try to update file timestamps, return the number of files updated.
    var updated = 0
    val root = File("/storage/emulated/0") // Or SD card root if needed
    timestamps.forEach { ft ->
        val file = File(root, ft.path)
        if (file.exists()) {
            file.setLastModified(ft.timestamp)
            updated++
        }
    }
    return updated
}