package com.example.obdscanner.ui.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.obdscanner.model.db.LocalDataSource
import com.example.obdscanner.model.dto.SensorReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DataViewModel(private val localDataSource: LocalDataSource) : ViewModel() {
    private val _responseDataState = MutableStateFlow<List<SensorReading>>(emptyList())
    val respnseDataState: StateFlow<List<SensorReading>> = _responseDataState.asStateFlow()

    private val _singleSensorData = MutableStateFlow<SensorReading>(SensorReading(0x01, "Engine Speed", "RPM", 0))
    val singleSensorData: StateFlow<SensorReading> = _singleSensorData.asStateFlow()

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
}