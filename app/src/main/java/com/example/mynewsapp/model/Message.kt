package com.example.mynewsapp.model

import java.time.LocalDateTime
import java.util.*

data class Message (
    val sender: User,
    val messageContent: String,
    val createdAt: LocalDateTime?
)