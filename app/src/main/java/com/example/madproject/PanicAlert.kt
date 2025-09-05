package com.example.madproject

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "panic_alerts")
data class PanicAlert(
    @PrimaryKey val alertId: Long,
    val userId: Long,
    val tripId: Long,
    val timestamp: Long
)