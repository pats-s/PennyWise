package com.example.pennywise

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pennywise.remote.User
import com.example.pennywise.ui.adapter.ProfileAdapter
import com.example.pennywise.ui.adapter.UserProfileField
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class ProfileActivity : AppCompatActivity() {

    companion object {
        const val IMAGE_PICKER_REQUEST_CODE = 1001
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var profileFields: MutableList<UserProfileField>
    private lateinit var user: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        recyclerView = findViewById(R.id.recyclerViewProfile)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // checking if there is an actual logged in user
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            Log.d("auth user","no logged in user")
            return
        }


        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(currentUser.uid)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val dob = document.getString("dob") ?: ""
                    val firstname = document.getString("firstname") ?: ""
                    val lastname = document.getString("lastname") ?: ""
                    val email = document.getString("email") ?: ""
                    val password = document.getString("password") ?: ""
                    val profilePicture =
                        document.getString("profilePicture") ?: "default_image_path"
                    val wallet = document.getString("walletId") ?: ""

                    // Initialize the User object
                    user = User(
                        userId = 0.toString(),
                        firstName = firstname,
                        lastName = lastname,
                        email = email,
                        password = password,
                        dateOfBirth = dob.toLongOrNull() ?: 0L,
                        profilePicture = profilePicture,
                        walletId = wallet
                    )

                    // Populate profile fields
                    profileFields = mutableListOf(
                        UserProfileField("First Name", firstname, UserProfileField.FieldType.TEXT, editable = true, dbFieldName = "firstname"),
                        UserProfileField("Last Name", lastname, UserProfileField.FieldType.TEXT, editable = true, dbFieldName = "lastname"),
                        UserProfileField("Email", email, UserProfileField.FieldType.TEXT, dbFieldName = "email"),
//                        UserProfileField("Profile Picture", "", UserProfileField.FieldType.IMAGE, dbFieldName = profilePicture),
                        UserProfileField("Password", password, UserProfileField.FieldType.PASSWORD, dbFieldName = "password"),
                        UserProfileField(
                            "Date of Birth",
                            dob,
                            UserProfileField.FieldType.TEXT,
                            editable = dob == null,
                            dbFieldName = "dob"
                        ),

                        )


                    profileAdapter = ProfileAdapter(
                        profileFields,
                        this,
                        ::onPasswordSave,
                    ) { updatedField ->
                        onFieldUpdate(updatedField)
                    }
                    recyclerView.adapter = profileAdapter
                } else {
                    Toast.makeText(this, "User document does not exist", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to fetch user data: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        val logoutButton: Button = findViewById(R.id.logoutButton)

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Redirect to SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    // Handle updating profile fields
    private fun onFieldUpdate(field: UserProfileField) {
        val userRef = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)

        userRef.update(field.dbFieldName, field.value)
            .addOnSuccessListener {
                Toast.makeText(this, "${field.label} updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error updating ${field.label}: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    // Handle saving password changes
    fun onPasswordSave(newPassword: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
            return
        }

        currentUser.updatePassword(newPassword)
            .addOnSuccessListener {
                val userRef = FirebaseFirestore.getInstance().collection("users")
                    .document(currentUser.uid)

                userRef.update("password", hashPassword(newPassword))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(
                            this,
                            "Password updated in Authentication, but failed to update in Firestore: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to update password in Authentication: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    private fun hashPassword(password: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = messageDigest.digest(password.toByteArray(Charsets.UTF_8))
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }


}
