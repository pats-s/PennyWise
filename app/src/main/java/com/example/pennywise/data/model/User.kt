package com.example.pennywise.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(
    tableName = "users",
    foreignKeys = [ForeignKey(
        entity = Wallet::class,
        parentColumns = ["walletId"],
        childColumns = ["walletId"],
        onDelete = ForeignKey.CASCADE // Deletes the user if the wallet is deleted
    )]
)
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    @ColumnInfo(name = "firstname") val firstname: String?,
    @ColumnInfo(name = "lastname") val lastname: String?,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "dob") val dateOfBirth: Long,
    @ColumnInfo(name = "profilePicture") val profilePicture: String? = null,
    @ColumnInfo(name = "walletId") val walletId: Int, // Foreign key referencing Wallet.walletId
    @ColumnInfo(name = "last_modified") val lastModified: Long = System.currentTimeMillis()
)