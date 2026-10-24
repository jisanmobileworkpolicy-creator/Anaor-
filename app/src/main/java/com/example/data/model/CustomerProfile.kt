package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_profile")
data class CustomerProfile(
    @PrimaryKey
    val id: String = "active_user",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val locality: String = "",
    val district: String = "Murshidabad",
    val pinCode: String = "742149",
    val referralCode: String = "JMREF-" + ((1000..9999).random()),
    val isLoggedIn: Boolean = false
)
