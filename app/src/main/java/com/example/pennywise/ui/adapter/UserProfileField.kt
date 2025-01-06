package com.example.pennywise.ui.adapter

data class UserProfileField(
    val label: String,
    var value: String?,
    val fieldType: FieldType,
    val isPassword: Boolean = false,
    val editable: Boolean = false,
    val dbFieldName: String

) {
    enum class FieldType {
        TEXT, IMAGE, PASSWORD,DATE
    }
}
