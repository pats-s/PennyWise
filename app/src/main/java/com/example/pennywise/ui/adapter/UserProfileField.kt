package com.example.pennywise.ui.adapter

data class UserProfileField(
    val label: String,
    var value: String?,
    val fieldType: FieldType,
    val isPassword: Boolean = false, // Flag to indicate if it's a password field
    val editable: Boolean = false,
    val dbFieldName: String // Actual Firestore field name

) {
    enum class FieldType {
        TEXT, IMAGE, PASSWORD,DATE
    }
}
