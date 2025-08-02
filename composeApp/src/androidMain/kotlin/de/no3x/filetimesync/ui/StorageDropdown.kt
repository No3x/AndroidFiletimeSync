package de.no3x.filetimesync.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageDropdown(
    selectedDir: File?,
    onDirSelected: (File) -> Unit
) {
    // Dynamically scan for storage directories
    val storageDirs by remember {
        mutableStateOf(
            File("/storage").listFiles()
                ?.filter { it.isDirectory && it.canRead() && it.name != "self" }
                ?.sortedBy { it.absolutePath }
                ?: listOf(File("/storage/emulated/0"))
        )
    }
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(selectedDir ?: storageDirs.firstOrNull()) }

    // Automatically select default directory if none selected yet
    LaunchedEffect(storageDirs) {
        if (selectedDir == null && storageDirs.isNotEmpty()) {
            val defaultDir = storageDirs.first()
            selected = defaultDir
            onDirSelected(defaultDir)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selected?.absolutePath ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Storage") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            storageDirs.forEach { dir ->
                DropdownMenuItem(
                    text = { Text(dir.absolutePath) },
                    onClick = {
                        selected = dir
                        onDirSelected(dir)
                        expanded = false
                    }
                )
            }
        }
    }
}