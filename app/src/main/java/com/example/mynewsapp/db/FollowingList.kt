package com.example.mynewsapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "followingList")
data class FollowingList (
    @PrimaryKey(autoGenerate = true)
    val followingListId: Int,
    val listName: String
)