package com.example.madproject

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Contact(
    @PrimaryKey(autoGenerate = true) val contactId: Long = 0,
    val userId: Long,
    val linkedUserId: Long,
    val name: String,
    val phoneNumber: String,
    val isEmergencyContact: Boolean = false
)