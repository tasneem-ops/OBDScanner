package com.example.obdscanner.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.obdscanner.model.dto.ConnectionStatus
import com.example.obdscanner.model.dto.SensorReading

@Database(entities = arrayOf(SensorReading::class, ConnectionStatus::class), version = 1)
abstract class SensorReadingDB : RoomDatabase(){
    abstract fun getDao() : SensorReadingDao
    companion object{
        @Volatile
        private var INSTANCE: SensorReadingDB? = null
        fun getInstance (context: Context): SensorReadingDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, SensorReadingDB::class.java, "sensor_database")
                    .build()
                INSTANCE = instance
                instance }
        }
    }
}