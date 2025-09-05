package com.example.madproject

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "trips", foreignKeys = [
    ForeignKey(
        entity = User::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )
])
data class Trip(
    @PrimaryKey(autoGenerate = true) val tripId: Long = 0,
    val userId: Long,
    val departure: String,
    val destination: String,
    val eta: String,
    val status: String = "Started",
    val imageUri: String? = null
)