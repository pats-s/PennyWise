package com.example.pennywise.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pennywise.data.model.Transaction
import com.example.pennywise.data.model.User
import com.example.pennywise.data.model.Wallet
import com.example.pennywise.data.model.SavingGoal
import com.example.pennywise.data.model.Category
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Wallet::class, Transaction::class, SavingGoal::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class PennyWiseDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingGoalDao(): SavingGoalDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: PennyWiseDatabase? = null

        fun getDatabase(context: Context): PennyWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PennyWiseDatabase::class.java,
                    "finance_database"
                )
                    .addCallback(DatabaseCallback()) // Add the callback
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Use coroutines to call the suspend function
                GlobalScope.launch {
                    INSTANCE?.categoryDao()?.insertCategories(getPredefinedCategories())
                }
            }
        }


        private fun getPredefinedCategories(): List<Category> {
            return listOf(
                Category(name = "Groceries", type = "Expense"),
                Category(name = "Rent", type = "Expense"),
                Category(name = "Transportation", type = "Expense"),
                Category(name = "Salary", type = "Income"),
                Category(name = "Gifts", type = "Income"),
                Category(name = "Investments", type = "Income")
            )
        }
    }
}
