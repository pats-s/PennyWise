package com.example.pennywise.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "saving_goals",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    ),ForeignKey(
        entity = Category::class,
        parentColumns = ["categoryId"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE)
    ]
)
data class SavingGoal(
    @PrimaryKey(autoGenerate = true) val goalId: Int = 0,
    @ColumnInfo(name = "userId") val userId: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "targetAmount") val targetAmount: Double,
    @ColumnInfo(name = "currentProgress") val currentProgress: Double = 0.0,
    @ColumnInfo(name = "deadline") val deadline: Long? = null,

    // Fields for No Spending Streaks
    @ColumnInfo(name = "categoryId") val categoryId: Int?, // The category to avoid spending in
    @ColumnInfo(name = "streakStartDate") val streakStartDate: Long?, // Timestamp for the streak start
    @ColumnInfo(name = "currentStreakDays") val currentStreakDays: Int = 0, // Number of days in the current streak
    @ColumnInfo(name = "longestStreakDays") val longestStreakDays: Int = 0, // User's longest streak
    @ColumnInfo(name = "last_modified") val lastModified: Long = System.currentTimeMillis()
)
