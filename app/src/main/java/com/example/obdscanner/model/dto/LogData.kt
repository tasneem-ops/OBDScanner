package com.example.obdscanner.model.dto

data class LogData(
    val deviceId : String,
    val tag : String,
    val message : String,
    val timestamp: String
)
