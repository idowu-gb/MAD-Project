package com.example.madproject

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PanicAlertDao {
    @Insert
    suspend fun insert(panicAlert: PanicAlert)

    @Query("SELECT * FROM panic_alerts WHERE userId IN (SELECT userId FROM contacts WHERE contactId = :contactId)")
    fun getPanicAlertsForContact(contactId: Long): LiveData<List<PanicAlert>>
}