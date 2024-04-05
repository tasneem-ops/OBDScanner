package com.example.obdscanner.model.db

import android.content.Context
import androidx.core.os.persistableBundleOf
import com.example.obdscanner.model.dto.ConnectionStatus
import com.example.obdscanner.model.dto.SensorReading
import kotlinx.coroutines.flow.Flow

class LocalDataSource private constructor(context: Context){
    private var sensorReadingDao: SensorReadingDao = SensorReadingDB.getInstance(context).getDao()
    companion object{
        @Volatile
        private var INSTANCE: LocalDataSource? = null
        fun getInstance (context: Context): LocalDataSource{
            return INSTANCE ?: synchronized(this){
                val instance = LocalDataSource(context)
                INSTANCE = instance
                instance
            }
        }
    }
    suspend fun insertOrUpdate(sensorReading: SensorReading){
        sensorReadingDao.insertOrUpdate(sensorReading)
    }
    suspend fun delete(sensorReading: SensorReading){
        sensorReadingDao.delete(sensorReading)
    }
    fun getAll() : Flow<List<SensorReading>>{
        return sensorReadingDao.getAll()
    }
    fun getSensorReading(pid : Byte) : Flow<SensorReading>{
        return sensorReadingDao.getSensorReading(pid)
    }
    suspend fun updateConnectionStatus(connectionStatus: ConnectionStatus){
        sensorReadingDao.updateConnectionStatus(connectionStatus)
    }
    fun getConnectionStatus() : Flow<ConnectionStatus>{
        return sensorReadingDao.getConnectionStatus()
    }
}