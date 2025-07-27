package de.no3x.filetimesync.ui.share

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ShareToDriveButton(jsonUri: Uri?) {
    val shareLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}
    Button(
        onClick = {
            if (jsonUri != null) {
                val driveIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, jsonUri)
                    setPackage("com.google.android.apps.docs") // Google Drive package
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                shareLauncher.launch(
                    Intent.createChooser(
                        driveIntent,
                        "Upload JSON to Google Drive"
                    )
                )
            }
        },
        enabled = jsonUri != null
    ) {
        Text("Upload JSON to Google Drive")
    }
}