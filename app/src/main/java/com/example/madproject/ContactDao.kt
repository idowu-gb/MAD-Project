package com.example.madproject

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ContactDao {
    @Insert
    suspend fun insert(contact: Contact)

    @Query("SELECT * FROM contacts WHERE userId = :userId")
    suspend fun getContactsByUser(userId: Long): List<Contact>

    @Query("SELECT * FROM contacts WHERE contactId = :contactId LIMIT 1")
    suspend fun getContactById(contactId: Long): Contact?

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getContactByPhoneNumber(phoneNumber: String): Contact?

    @Query("SELECT * FROM contacts WHERE userId = :userId AND linkedUserId = :linkedUserId LIMIT 1")
    suspend fun getContactByUserAndLinkedUser(userId: Long, linkedUserId: Long): Contact?

    @Query("SELECT * FROM contacts WHERE linkedUserId = :linkedUserId")
    suspend fun getContactsForLinkedUser(linkedUserId: Long): List<Contact>

    @Delete
    suspend fun delete(contact: Contact)

    @Update
    suspend fun updateContact(contact: Contact)
}