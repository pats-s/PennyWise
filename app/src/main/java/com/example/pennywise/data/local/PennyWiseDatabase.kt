package com.example.pennywise.data.local

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pennywise.data.local.dao.CategoryDao
import com.example.pennywise.data.local.dao.SavingGoalDao
import com.example.pennywise.data.local.dao.TransactionDao
import com.example.pennywise.data.local.dao.UserDao
import com.example.pennywise.data.local.dao.WalletDao
import com.example.pennywise.data.model.Transaction
import com.example.pennywise.data.model.User
import com.example.pennywise.data.model.Wallet
import com.example.pennywise.data.model.SavingGoal
import com.example.pennywise.data.model.Category
import com.google.firebase.firestore.FirebaseFirestore
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

    @SuppressLint("StaticFieldLeak")
    companion object {
        @Volatile
        private var INSTANCE: PennyWiseDatabase? = null
        private lateinit var firebaseFirestore: FirebaseFirestore
        fun getDatabase(context: Context): PennyWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PennyWiseDatabase::class.java,
                    "finance_database"
                )
                    .addCallback(DatabaseCallback()) // Add the callback
                    .build()

                firebaseFirestore = FirebaseFirestore.getInstance()
                INSTANCE = instance
                instance
            }
        }
        fun getFirestore(): FirebaseFirestore {
            return firebaseFirestore
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
