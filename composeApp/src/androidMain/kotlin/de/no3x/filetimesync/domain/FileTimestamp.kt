package de.no3x.filetimesync.domain

import kotlinx.serialization.Serializable

@Serializable
data class FileTimestamp(val path: String, val timestamp: Long)

