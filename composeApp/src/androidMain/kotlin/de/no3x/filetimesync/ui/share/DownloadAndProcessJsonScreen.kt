package de.no3x.filetimesync.ui.share

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.no3x.filetimesync.domain.FileTimestamp
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun DownloadAndProcessJsonScreen(
    processFileTimestamps: (List<FileTimestamp>) -> Unit
) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("Waiting for file selection...") }

    val pickJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            status = "No file selected."
            return@rememberLauncherForActivityResult
        }
        status = "Reading file..."

        // Read the JSON as text
        val jsonStr = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input)).readText()
            }
        }.getOrNull()
        if (jsonStr == null) {
            status = "Failed to read JSON file."
            return@rememberLauncherForActivityResult
        }

        // Parse the JSON
        val timestamps = runCatching {
            Json.decodeFromString<List<FileTimestamp>>(jsonStr)
        }.getOrNull()
        if (timestamps == null) {
            status = "Invalid JSON format."
            return@rememberLauncherForActivityResult
        }

        status = "Loaded ${timestamps.size} entries, processing..."
        // Process (calls your logic)
        processFileTimestamps(timestamps)
        status = "File processed successfully!"
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Step 1: Download the JSON file from Google Drive (using the Drive app or browser), bluetooth or or other to your device.")
        Spacer(Modifier.height(12.dp))
        Button(onClick = { pickJsonLauncher.launch(arrayOf("application/json", "text/plain")) }) {
            Text("Step 2: Pick JSON File")
        }
        Spacer(Modifier.height(12.dp))
        Text(status)
    }
}