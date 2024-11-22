import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pennywise.data.local.PennyWiseDatabase
import com.example.pennywise.data.repository.PennyWiseRepository

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    // Initialize the repository using the Room database instance
    private val repository: PennyWiseRepository by lazy {
        val database = PennyWiseDatabase.getDatabase(context) // Get the Room database instance
        PennyWiseRepository(
            userDao = database.userDao(),
            walletDao = database.walletDao(),
            transactionDao = database.transactionDao(),
            savingGoalDao = database.savingGoalDao(),
            categoryDao = database.categoryDao()
        )
    }

    override suspend fun doWork(): Result {
        // Retrieve the walletId from input data
        val walletId = inputData.getInt("walletId", -1)
        if (walletId == -1) {
            // Fail the work if walletId is not valid
            return Result.failure()
        }

        return try {
            // Perform synchronization for all entities
            repository.syncUsers()             // Sync Users
            repository.syncWallets()           // Sync Wallets
            repository.syncTransactions(walletId = walletId) // Sync Transactions for the given walletId
            repository.syncSavingGoals(userId = 1) // Replace with dynamic userId if needed
            repository.syncCategories()        // Sync Categories
            // Indicate successful completion of the work
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // Indicate failure of the work
            Result.failure()
        }
    }
}