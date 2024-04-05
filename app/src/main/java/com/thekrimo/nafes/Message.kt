package com.thekrimo.nafes

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val messageLink: String = "",
    val audioUrl: String = "",
    val timestamp: String =""
)