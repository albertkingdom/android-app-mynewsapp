package com.example.mynewsapp.model

import com.squareup.moshi.JsonClass
import java.time.LocalDateTime
import java.util.*

@JsonClass(generateAdapter = true)
data class Message (
    val sender: User,
    val messageContent: String,
    val createdAt: LocalDateTime?
)