package com.example.pennywise

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.pennywise.PennyWiseDatabase
import com.example.pennywise.local.predefinedCategories
import com.example.pennywise.remote.Transaction
import com.example.pennywise.remote.User
import com.example.pennywise.ui.viewmodel.SharedViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var repository: PennyWiseRepository
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        repository = PennyWiseRepository.getInstance(applicationContext)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        val fabSendMoney = findViewById<FloatingActionButton>(R.id.fab_send_money)
        fabSendMoney.setOnClickListener {
            showSendMoneyDialog()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    private fun showSendMoneyDialog() {
        // Inflate dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_send_money, null)

        // Initialize dialog components
        val rgSendReceive = dialogView.findViewById<RadioGroup>(R.id.rg_send_receive)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_amount)
        val etEmail = dialogView.findViewById<EditText>(R.id.et_email)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btn_submit)

        // Create the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()


        btnSubmit.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            val email = etEmail.text.toString()

            if (amount == null || email.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (rgSendReceive.checkedRadioButtonId == R.id.rb_send) {
                sendMoney(amount, email)
            } else {
                requestMoney(amount, email)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun sendMoney(amount: Double, recipientEmail: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            fetchUserWalletId(userId) { senderWalletId ->
                repository.getWallet(senderWalletId, onSuccess = { senderWallet ->
                    if (amount > senderWallet.balance) {
                        Toast.makeText(this, "Insufficient balance!", Toast.LENGTH_SHORT).show()
                        return@getWallet
                    }

                    checkIfEmailExists(recipientEmail, onSuccess = { exists ->
                        if (exists) {
                            fetchRecipientDetailsByEmail(recipientEmail, onSuccess = { recipientUser ->
                                val recipientWalletId = recipientUser.walletId

                                repository.getWallet(recipientWalletId, onSuccess = { recipientWallet ->
                                    val senderNewBalance = senderWallet.balance - amount
                                    val recipientNewBalance = recipientWallet.balance + amount

                                    repository.updateWalletBalance(senderWalletId, senderNewBalance, onSuccess = {
                                        createTransaction(
                                            walletId = senderWalletId,
                                            amount = amount,
                                            type = "Expense",
                                            categoryName = "Penny Send"
                                        )
                                        sharedViewModel.updateWalletBalance(senderNewBalance)
                                    }, onFailure = { exception ->
                                        Toast.makeText(this, "Failed to update sender's wallet: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    })

                                    repository.updateWalletBalance(recipientWalletId, recipientNewBalance, onSuccess = {
                                        createTransaction(
                                            walletId = recipientWalletId,
                                            amount = amount,
                                            type = "Income",
                                            categoryName = "Penny Receive"
                                        )
                                        Toast.makeText(this, "Money sent successfully!", Toast.LENGTH_SHORT).show()
                                    }, onFailure = { exception ->
                                        Toast.makeText(this, "Failed to update recipient's wallet: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    })

                                }, onFailure = { exception ->
                                    Toast.makeText(this, "Failed to fetch recipient's wallet: ${exception.message}", Toast.LENGTH_SHORT).show()
                                })
                            }, onFailure = { exception ->
                                Toast.makeText(this, "Failed to fetch recipient details: ${exception.message}", Toast.LENGTH_SHORT).show()
                            })
                        } else {
                            Toast.makeText(this, "This email is not registered in the app", Toast.LENGTH_SHORT).show()
                        }
                    }, onFailure = { exception ->
                        Toast.makeText(this, "Error checking email: ${exception.message}", Toast.LENGTH_SHORT).show()
                    })
                }, onFailure = { exception ->
                    Toast.makeText(this, "Failed to fetch sender's wallet: ${exception.message}", Toast.LENGTH_SHORT).show()
                })
            }
        }
    }


    private fun fetchUserWalletId(userId: String, onWalletIdFetched: (String) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val walletId = snapshot.getString("walletId")
                if (!walletId.isNullOrEmpty()) {
                    onWalletIdFetched(walletId)
                } else {
                    Log.e("InsightsFragment", "Wallet ID is null or empty")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("InsightsFragment", "Failed to fetch wallet ID: ${exception.message}")
            }
    }

    private fun checkIfEmailExists(email: String, onSuccess: (Boolean) -> Unit, onFailure: (Exception) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    onSuccess(true)
                } else {
                    onSuccess(false)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    private fun fetchRecipientDetailsByEmail(email: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val user = querySnapshot.documents[0].toObject(User::class.java)
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onFailure(Exception("User not found"))
                    }
                } else {
                    onFailure(Exception("Email not registered"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    private fun createTransaction(walletId: String, amount: Double, type: String, categoryName: String) {
        val categoryId = predefinedCategories.firstOrNull { it.name == categoryName }?.id ?: return

        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            walletId = walletId,
            amount = amount,
            type = type,
            categoryId = categoryId,
            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            description = if (type == "Expense") "Sent money" else "Received money"
        )

        repository.addTransaction(transaction, onSuccess = {
            if (type == "Expense") {
                sharedViewModel.addTransaction(transaction)
            }
        }, onFailure = { exception ->
            Log.e("Transaction", "Failed to create transaction: ${exception.message}")
        })
    }



    private fun requestMoney(amount: Double, payerEmail: String) {
        val requestorEmail = FirebaseAuth.getInstance().currentUser?.email

        if (requestorEmail.isNullOrEmpty()) {
            Toast.makeText(this, "Failed to get user email", Toast.LENGTH_SHORT).show()
            return
        }

        val requestId = UUID.randomUUID().toString()
        val request = mapOf(
            "requestId" to requestId,
            "requestorEmail" to requestorEmail,
            "payerEmail" to payerEmail,
            "amount" to amount,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance().collection("pendingRequests")
            .document(requestId)
            .set(request)
            .addOnSuccessListener {
                Toast.makeText(this, "Request sent successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send request: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



}