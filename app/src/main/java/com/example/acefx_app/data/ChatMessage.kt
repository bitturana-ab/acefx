package com.example.acefx_app.data

//data class ChatMessage(
//    val senderName: String,
//    val message: String,
//    val time: String,
//    val isUser: Boolean // true for client/company, false for admin
//)

// chat for POST request
data class ChatMessageRequest(
    val message: String
)
// chat message for showing
data class ChatMessage(
    val _id: String,             // MongoDB document ID
    val clientId: String,        // Reference to User _id
    val receiverId: String,        // Reference to User _id
    val senderRole: String,          // "client" or "admin"
    val message: String,         // Actual chat message
    val time: String,            // Time field from backend (Date)
    val createdAt: String,       // Automatically added by Mongoose
    val updatedAt: String        // Automatically added by Mongoose
)
data class ChatMessageResponse(
    val success: Boolean,
    val data: ChatMessage
)

data class ChatListResponse(
    val success: Boolean,
    val data: List<ChatMessage>
)