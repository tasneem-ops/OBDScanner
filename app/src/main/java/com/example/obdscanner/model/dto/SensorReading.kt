package com.example.obdscanner.model.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_readings")
data class SensorReading(
    @PrimaryKey
    val pid : Byte,
    val name : String,
    val unit : String,
    var data : Int
)
