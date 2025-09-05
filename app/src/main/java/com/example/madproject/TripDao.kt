package com.example.madproject

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert
    suspend fun insert(trip: Trip)

    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY tripId DESC")
    suspend fun getTripsByUser(userId: Long): List<Trip>

    @Query("UPDATE trips SET status = :status WHERE tripId = :tripId")
    suspend fun updateStatus(tripId: Long, status: String)

    @Query("DELETE FROM trips WHERE tripId = :tripId")
    suspend fun deleteTrip(tripId: Long)

    @Query("UPDATE trips SET imageUri = :imageUri WHERE tripId = :tripId")
    suspend fun updateImageUri(tripId: Long, imageUri: String)

    @Query("SELECT t.* FROM trips t INNER JOIN contacts c ON t.userId = c.linkedUserId WHERE c.userId = :contactUserId")
    fun getTripsForContact(contactUserId: Long): LiveData<List<Trip>>
}