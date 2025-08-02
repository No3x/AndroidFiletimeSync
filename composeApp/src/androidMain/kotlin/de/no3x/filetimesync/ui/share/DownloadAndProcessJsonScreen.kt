package de.no3x.filetimesync.ui.share

import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import de.no3x.filetimesync.domain.FileTimestamp
import de.no3x.filetimesync.ui.StorageDropdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

@Composable
fun DownloadAndProcessJsonScreen(
    processFileTimestamps: (File, List<FileTimestamp>) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    var status by remember { mutableStateOf("Waiting for json file selection...") }
    var selectedDir by remember { mutableStateOf<File?>(null) }
    var showProgress by remember { mutableStateOf(false) }
    var progressStage by remember { mutableStateOf(0f) }
    var currentFileName by remember { mutableStateOf<String?>(null) }
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
        if (selectedDir != null) {
            val pickJsonLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri: Uri? ->
                if (uri == null) {
                    status = "No file selected."
                    return@rememberLauncherForActivityResult
                }

                coroutineScope.launch {
                    status = "Reading file..."
                    showProgress = true
                    progressStage = 0f
                    currentFileName = null

                    val jsonStr = withContext(Dispatchers.IO) {
                        runCatching {
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                BufferedReader(InputStreamReader(input)).readText()
                            }
                        }.getOrNull()
                    }

                    if (jsonStr == null) {
                        status = "Failed to read JSON file."
                        showProgress = false
                        return@launch
                    }

                    val timestamps = withContext(Dispatchers.IO) {
                        runCatching {
                            Json.decodeFromString<List<FileTimestamp>>(jsonStr)
                        }.getOrNull()
                    }

                    if (timestamps == null) {
                        status = "Invalid JSON format."
                        showProgress = false
                        return@launch
                    }

                    status = "Loaded ${timestamps.size} entries, processing..."

                    withContext(Dispatchers.Default) {
                        timestamps.forEachIndexed { index, timestamp ->
                            // Process each file here if needed
                            withContext(Dispatchers.Main) {
                                currentFileName = timestamp.path
                                progressStage = (index + 1) / timestamps.size.toFloat()
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        currentFileName = null
                        status = "File processed successfully!"
                        showProgress = false
                    }
                    processFileTimestamps(selectedDir!!, timestamps)
                }
            }

            Text("Step 1: Download the JSON file from Google Drive (using the Drive app or browser), bluetooth or other to your device.")
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                progressStage = 0f
                pickJsonLauncher.launch(arrayOf("application/json", "text/plain"))
            }) {
                Text("Step 2: Pick JSON File")
            }
            Spacer(Modifier.height(12.dp))
            Text(status)

            if (showProgress) {
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedGlowingProgressBar(progress = progressStage)
                currentFileName?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Processing: $it")
                }
            }
        }
    }
}

@Composable
fun AnimatedGlowingProgressBar(progress: Float) {
    val glowColors = listOf(Color(0xFF38BDF8), Color(0xFF34D399))
    val brush = Brush.horizontalGradient(colors = glowColors)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(5.dp))
            .padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress)
                .height(10.dp)
                .blur(20.dp)
                .background(brush, RoundedCornerShape(5.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress)
                .height(10.dp)
                .background(brush, RoundedCornerShape(5.dp))
        )
    }
}