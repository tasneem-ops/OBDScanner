package com.example.obdscanner.model.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_status")
data class ConnectionStatus(
    @PrimaryKey
    val id : Int = 1,
    var state : Int
)
