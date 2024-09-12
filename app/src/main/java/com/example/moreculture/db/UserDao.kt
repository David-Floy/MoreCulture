package com.example.moreculture.db

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.google.firebase.firestore.auth.User


@Dao
interface UserDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(userAccount: UserAccount)

    @Query("SELECT user_mapRadius FROM users LIMIT 1")
    fun getUserRadius(): Double

    @Query("SELECT * FROM users WHERE user_id = :userId")
    fun getUserById(userId: Int) : UserAccount

    @Update
    suspend fun updateUser(user: UserAccount)



    @Query("UPDATE users SET user_mapRadius = :userMapRadius WHERE user_id = 1")
    suspend fun updateUserRadius(userMapRadius: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserTagCrossRef(userTagCrossRef: List <UserTagCrossRef>)

    @Transaction
    suspend fun updateTagIds(userId : Int, tagIds: List<Int>) {
        val crossRefs = tagIds.map { tagId -> UserTagCrossRef(tagId, userId) }
        Log.d("","Event inserted with ID: $userId")

        tagIds.forEach { tagId ->  insertUserTagCrossRef(crossRefs)
        }
    }

    @Query("""
        SELECT UT.tag_id
        FROM user_tags UT
        WHERE UT.user_id = :userId
    """)
    fun getTagIdsForUser(userId: Int): List<Int>

}