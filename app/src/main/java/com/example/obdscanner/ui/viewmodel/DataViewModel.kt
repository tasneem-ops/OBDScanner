package com.example.obdscanner.ui.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.obdscanner.Constants.STATE_DISCONNECTED
import com.example.obdscanner.ble.DecodingUtils
import com.example.obdscanner.model.db.LocalDataSource
import com.example.obdscanner.model.dto.ConnectionStatus
import com.example.obdscanner.model.dto.SensorReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DataViewModel(private val localDataSource: LocalDataSource) : ViewModel() {
    private val _responseDataState = MutableStateFlow<List<SensorReading>>(getDummyList())
    val respnseDataState: StateFlow<List<SensorReading>> = _responseDataState.asStateFlow()
    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus(1, STATE_DISCONNECTED))
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _singleSensorData = MutableStateFlow<SensorReading>(SensorReading(0x01, "Engine Speed", "RPM", 0.0))
    val singleSensorData: StateFlow<SensorReading> = _singleSensorData.asStateFlow()
    init {
        initDB()
        getConnectionStatus()
        getAllSensorReadings()
    }


    fun getAllSensorReadings(){
        viewModelScope.launch {
            localDataSource.getAll()
                .collectLatest {
                    _responseDataState.value = it
                }
        }
    }

    fun getSensorReading(pid : Byte){
        viewModelScope.launch {
            localDataSource.getSensorReading(pid)
                .collectLatest {
                    _singleSensorData.value = it
                }
        }
    }

    fun getConnectionStatus(){
        viewModelScope.launch {
            localDataSource.getConnectionStatus()
                .collectLatest {
                    _connectionStatus.value = it
                }
        }
    }
    fun getDummyList() : List<SensorReading>{
        val list = ArrayList<SensorReading>()
        DecodingUtils.getSensors().values.forEach {
            list.add(SensorReading(it.pid,it.name, it.unit, 0.0))
        }
        return list
    }
    private fun initDB() {
        viewModelScope.launch {
            localDataSource.updateConnectionStatus(ConnectionStatus(1, STATE_DISCONNECTED))
            getDummyList().forEach {
                localDataSource.insertOrUpdate(SensorReading(it.pid, it.name, it.unit, 0.0))
            }
        }

    }

}