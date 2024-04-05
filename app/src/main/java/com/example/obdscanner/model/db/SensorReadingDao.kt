package com.example.obdscanner.model.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.obdscanner.model.dto.ConnectionStatus
import com.example.obdscanner.model.dto.SensorReading
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorReadingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(sensorReading: SensorReading)

    @Delete
    suspend fun delete(sensorReading: SensorReading)

    @Query("SELECT * from sensor_readings")
    fun getAll() : Flow<List<SensorReading>>

    @Query("SELECT * from sensor_readings WHERE pid = :pid")
    fun getSensorReading(pid : Byte) : Flow<SensorReading>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateConnectionStatus(connectionStatus: ConnectionStatus)

    @Query("SELECT * FROM connection_status")
    fun getConnectionStatus() : Flow<ConnectionStatus>
}