package com.example.stockinsight.data.model

import java.util.Date

data class User(
    var id: String,
    val username: String?,
    val male: Boolean?,
    val dateOfBirth: Date?,
    val address: String?,
    val phoneNumber: String?,
    val email: String,
//    val profileImageUrl: String?,
    val isVerified: Boolean,
    val provider: String?,
    val providerId: String?,
    val token: String?,
    val watchlist: List<WatchlistItem>?
) {
    constructor(): this(
        "",
        "",
        null,
        null,
        "",
        "",
        "",
        false,
        null,
        null,
        null,
        emptyList()
    )
}