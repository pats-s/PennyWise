package com.example.pennywise.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pennywise.data.model.SavingGoal

@Dao
interface SavingGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingGoal(savingGoal: SavingGoal): Long

    @Update
    suspend fun updateSavingGoal(savingGoal: SavingGoal)

    @Delete
    suspend fun deleteSavingGoal(savingGoal: SavingGoal)

    @Query("SELECT * FROM saving_goals WHERE userId = :userId")
    suspend fun getSavingGoalsByUser(userId: Int): List<SavingGoal>

    @Query("""
        SELECT * FROM saving_goals 
        WHERE userId = :userId AND categoryId = :categoryId
    """)
    suspend fun getSavingGoalsByCategory(userId: Int, categoryId: Int): List<SavingGoal>
}
