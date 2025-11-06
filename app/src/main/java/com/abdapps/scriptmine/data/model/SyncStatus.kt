package com.abdapps.scriptmine.data.model

enum class SyncStatus {
    PENDING,      // Needs to be synced
    SYNCING,      // Currently syncing
    SYNCED,       // Successfully synced
    CONFLICT,     // Conflict detected
    ERROR         // Sync failed
}