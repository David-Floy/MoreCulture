package com.example.moreculture.db

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update


@Dao
interface UserDao {

    // Insert a user
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(userAccount: UserAccount)

    // Get all users
    @Query("SELECT user_mapRadius FROM users LIMIT 1")
    fun getUserRadius(): Double

    // Get user by ID
    @Query("SELECT * FROM users WHERE user_id = :userId")
    fun getUserById(userId: Int) : UserAccount

    // Update user
    @Update
    suspend fun updateUser(user: UserAccount)

    // Update user radius
    @Query("UPDATE users SET user_mapRadius = :userMapRadius WHERE user_id = 1")
    suspend fun updateUserRadius(userMapRadius: Double)

    // Insert user-tag cross-reference
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserTagCrossRef(userTagCrossRef: List <UserTagCrossRef>)

    // Delete user-tag cross-references for a specific user
    @Query("DELETE FROM user_tags WHERE user_id = :userId")
    suspend fun deleteUserTagCrossRefsForUser(userId: Int)

    // Update user tags
    @Transaction
    suspend fun updateTagIds(userId: Int, tagIds: List<Int>) {
        // 1. Delete existing tag associations for the user
        deleteUserTagCrossRefsForUser(userId)

        // 2. Insert new tag associations
        val crossRefs = tagIds.map { tagId -> UserTagCrossRef(tagId, userId) }
        Log.d("", "Users selected tagsIds inserted with ID: $tagIds")

        insertUserTagCrossRef(crossRefs) // Insert all cross-references at once
    }

    // Get all user tags
    @Query("""
        SELECT UT.tag_id
        FROM user_tags UT
        WHERE UT.user_id = :userId
    """)
    fun getTagIdsForUser(userId: Int): List<Int>

}
