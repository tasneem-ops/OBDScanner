package com.example.obdscanner.model.dto

data class Sensor(
    val name : String,
    val unit : String,
    val pid : Byte,
    val formula : (Int, Int) -> Double)
