package com.example.pennywise.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.pennywise.R

class ProfileAdapter(
    private val fields: List<UserProfileField>,
    private val context: Context,
    private val onPasswordSave: (String) -> Unit,
    private val onFieldUpdate: (UserProfileField) -> Unit // Callback for general field updates (e.g., Name, Last Name)

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_IMAGE = 1
        private const val TYPE_PASSWORD = 2
        private const val TYPE_DOB = 3 // New type for DOB
        private const val TYPE_NAME = 4 // New type for Name


    }

    override fun getItemViewType(position: Int): Int {
        return when (fields[position].fieldType) {
            UserProfileField.FieldType.TEXT -> {
                if (fields[position].label == "First Name" || fields[position].label == "Last Name") TYPE_NAME
                else TYPE_TEXT
            }
            UserProfileField.FieldType.IMAGE -> TYPE_IMAGE
            UserProfileField.FieldType.PASSWORD -> TYPE_PASSWORD
            UserProfileField.FieldType.DATE -> TYPE_DOB

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            TYPE_TEXT -> TextViewHolder(inflater.inflate(R.layout.item_text_field, parent, false))
            TYPE_IMAGE -> ImageViewHolder(inflater.inflate(R.layout.item_image_field, parent, false))
            TYPE_PASSWORD -> PasswordViewHolder(inflater.inflate(R.layout.item_password_field, parent, false))
            TYPE_DOB -> DOBViewHolder(inflater.inflate(R.layout.item_dob_field, parent, false)) // Inflate DOB layout
            TYPE_NAME -> NameViewHolder(inflater.inflate(R.layout.item_name_field, parent, false)) // Inflate name field

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val field = fields[position]
        when (holder) {
            is TextViewHolder -> holder.bind(field)
            is ImageViewHolder -> holder.bind(field)
            is PasswordViewHolder -> holder.bind(field, onPasswordSave)
            is NameViewHolder -> holder.bind(field) {
                updatedName -> onFieldUpdate(field.copy(value = updatedName))
            }
        }
    }

    override fun getItemCount(): Int = fields.size


    class NameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameLabel: TextView = itemView.findViewById(R.id.nameFieldLabel)
        private val nameValue: EditText = itemView.findViewById(R.id.nameFieldValue)
        private val saveButton: Button = itemView.findViewById(R.id.saveNameButton)

        fun bind(field: UserProfileField, onNameSave: (String) -> Unit) {
            nameLabel.text = field.label
            nameValue.setText(field.value)

            saveButton.setOnClickListener {
                val newName = nameValue.text.toString()
                if (newName.isEmpty()) {
                    Toast.makeText(itemView.context, "Please enter a valid ${field.label}", Toast.LENGTH_SHORT).show()
                } else {
                    onNameSave(newName)
                    Toast.makeText(itemView.context, "${field.label} updated successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // ViewHolder for text fields
    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val label: TextView = itemView.findViewById(R.id.textFieldLabel)
        private val value: TextView = itemView.findViewById(R.id.textFieldValue)

        fun bind(field: UserProfileField) {
            label.text = field.label
            value.text = field.value
        }
    }

    // ViewHolder for image fields
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.imageField)

        fun bind(field: UserProfileField) {
            // Handle image loading (use a library like Glide or Picasso for real implementations)
        }
    }

    // ViewHolder for password fields
    class PasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val currentPassword: TextView = itemView.findViewById(R.id.currentPasswordTextView)
        private val newPassword: EditText = itemView.findViewById(R.id.newPasswordEditText)
        private val confirmPassword: EditText = itemView.findViewById(R.id.confirmPasswordEditText)
        private val saveButton: Button = itemView.findViewById(R.id.savePasswordButton)

        fun bind(field: UserProfileField, onPasswordSave: (String) -> Unit) {
            currentPassword.text = field.value?.let { "*".repeat(it.length) }
            saveButton.setOnClickListener {
                val newPass = newPassword.text.toString()
                val confirmPass = confirmPassword.text.toString()

                if (newPass.isEmpty() || confirmPass.isEmpty()) {
                    Toast.makeText(itemView.context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else if (newPass != confirmPass) {
                    Toast.makeText(itemView.context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else {
                    onPasswordSave(newPass)
                }
            }
        }
    }
//    class DOBViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val dobLabel: TextView = itemView.findViewById(R.id.textViewLabel)
//        private val dobValue: EditText = itemView.findViewById(R.id.editTextValue)
//        private val saveButton: Button = itemView.findViewById(R.id.saveButton)
//
//        fun bind(field: UserProfileField, onDOBSave: (String) -> Unit) {
//            dobLabel.text = field.label
//            dobValue.setText(field.value)
//
//            // Enable editing only if DOB is null
//            dobValue.isEnabled = field.value == null
//            saveButton.isEnabled = field.value == null
//
//            saveButton.setOnClickListener {
//                val newDOB = dobValue.text.toString()
//
//                if (newDOB.isEmpty()) {
//                    Toast.makeText(itemView.context, "Please enter a valid DOB", Toast.LENGTH_SHORT).show()
//                } else {
//                    onDOBSave(newDOB)
//                    Toast.makeText(itemView.context, "DOB updated successfully", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
class DOBViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val dobLabel: TextView = itemView.findViewById(R.id.textFieldLabel)
    private val dobValue: EditText = itemView.findViewById(R.id.textFieldValue)
    private val saveButton: Button = itemView.findViewById(R.id.saveButton)

    fun bind(field: UserProfileField, onDOBSave: (String) -> Unit) {
        dobLabel.text = field.label
        dobValue.setText(field.value)
        Log.d("dob",field.value?: "dob is null")

        // Disable editing and button if DOB is not null
        val isDobNull = field.value.isNullOrEmpty()
        dobValue.isEnabled = isDobNull
        saveButton.isEnabled = isDobNull

        // Add a click listener to the save button
        saveButton.setOnClickListener {
            val newDOB = dobValue.text.toString()

            if (newDOB.isEmpty()) {
                Toast.makeText(itemView.context, "Please enter a valid DOB", Toast.LENGTH_SHORT).show()
            } else {
                onDOBSave(newDOB) // Pass the new DOB to the callback
                Toast.makeText(itemView.context, "DOB updated successfully", Toast.LENGTH_SHORT).show()

                // Disable editing and the save button after saving
                dobValue.isEnabled = false
                saveButton.isEnabled = false
            }
        }
    }
}

}
