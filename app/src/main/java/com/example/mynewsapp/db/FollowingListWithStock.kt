package com.example.mynewsapp.db

import androidx.room.Embedded
import androidx.room.Relation

data class FollowingListWithStock(
    @Embedded val followingList: FollowingList,
    @Relation(
        parentColumn = "followingListId",
        entityColumn = "parentFollowingListId"
    )
    val stocks: List<Stock>
)