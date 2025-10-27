package com.example.acefx_app.data

//data class ChatMessage(
//    val senderName: String,
//    val message: String,
//    val time: String,
//    val isUser: Boolean // true for client/company, false for admin
//)
data class ChatMessage(
    val sender: String, // "client" or "admin"
    val text: String,
    val time: String
)
