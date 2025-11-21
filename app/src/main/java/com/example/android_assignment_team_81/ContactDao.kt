package com.example.android_assignment_team_81

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert
    suspend fun insert(contact: Contact)

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE category = :category ORDER BY name ASC")
    fun getContactsByCategory(category: String): Flow<List<Contact>>

    @Query("SELECT DISTINCT category FROM contacts ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Delete
    suspend fun delete(contact: Contact)
}