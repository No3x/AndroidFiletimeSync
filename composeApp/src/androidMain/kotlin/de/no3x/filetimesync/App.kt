package de.no3x.filetimesync

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
            var lastProcessedFiles by remember { mutableStateOf<List<FileTimestamp>>(emptyList()) }

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
                    DownloadAndProcessJsonScreen { dir, timestamps ->
                        val updatedFiles = updateTimestamps(dir, timestamps)
                        lastProcessedFiles = updatedFiles
                        screen = AppScreen.SUMMARY
                    }
                    Button(onClick = { screen = AppScreen.MAIN }) { Text("Back") }
                }

                AppScreen.SUMMARY -> {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        var searchQuery by remember { mutableStateOf("") }

                        Text("Updated timestamps for ${lastProcessedFiles.size} files!")
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        val treeEntries = lastProcessedFiles
                            .map { File(it.path) }
                            .filter { file ->
                                searchQuery.isBlank() ||
                                        file.name.contains(searchQuery, ignoreCase = true) ||
                                        file.parent?.contains(searchQuery, ignoreCase = true) == true
                            }
                            .groupBy { it.parent ?: "/" }
                            .toSortedMap()
                            .flatMap { (dir, files) ->
                                val dirEntry = TreeEntry(label = "📁 $dir", indent = 0)
                                val fileEntries = files.sortedBy { it.name }.map {
                                    TreeEntry(label = "└ ${it.name}", indent = 1)
                                }
                                listOf(dirEntry) + fileEntries
                            }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(treeEntries) { entry ->
                                Text(
                                    text = entry.label,
                                    modifier = Modifier.padding(start = (entry.indent * 16).dp, bottom = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(onClick = { screen = AppScreen.MAIN }) {
                            Text("Back to Main")
                        }
                    }
                }
            }
        }
    }
}

enum class AppScreen {
    MAIN, GENERATE_JSON, SHARE_JSON, DOWNLOAD_AND_PROCESS_JSON, SUMMARY
}

data class TreeEntry(val label: String, val indent: Int)

fun updateTimestamps(dir: File, timestamps: List<FileTimestamp>): List<FileTimestamp> {
    val updated : MutableList<FileTimestamp> = mutableListOf()
    timestamps.forEach { ft ->
        val file = File(dir, ft.path)
        if (file.exists()) {
            file.setLastModified(ft.timestamp)
            updated.add(ft)
        }
    }
    return updated.toList()
}
