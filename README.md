This is a Kotlin Multiplatform app designed to help users synchronize file timestamps between devices. The app primarily targets Android and uses Jetpack Compose for its user interface. 

The app will scan all storage/emulated/0 files. It will exclude Android/data files except WhatsApp media.

## Features

1. **Export File Timestamps**:
- Scans the SD card or internal storage for files.
- Collects file paths and their last modified timestamps.
- Exports the data as a JSON file to the device's Downloads folder.

2. **Import and Apply Timestamps**:
- Allows users to select a JSON file containing file timestamps.
- Updates the timestamps of matching files on the new device.

3. **Google Drive Integration**:
- Enables users to upload the exported JSON file to Google Drive.
- Supports downloading the JSON file from Google Drive for processing.

4. **Permissions Handling**:
- Requests necessary permissions to manage external storage on Android 11+ devices.

## How It Works

- **Export Process**:
  - The app scans the file system, filters files (e.g., WhatsApp media), and generates a JSON file with file paths and timestamps.
  - The JSON file is saved to the Downloads folder and can be shared via Google Drive.

- **Import Process**:
  - The app reads a JSON file selected by the user.
  - It processes the file and updates the timestamps of matching files on the device.

## Requirements

- Android target 30 or higher for full file management capabilities.
- Permissions for managing external storage.

## Known Issues

- Sometimes there is no feedback if in action was done.
- The app does things on the Main thread, which can lead to UI freezes for large file sets. There was no need to fix it for me.

## Technologies Used

- **Kotlin Multiplatform**: Shared business logic across platforms.
- **Jetpack Compose**: Modern UI toolkit for Android.
- **Kotlin Serialization**: For encoding and decoding JSON data.
- **Android FileProvider**: For secure file sharing.