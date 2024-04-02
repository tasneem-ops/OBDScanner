package com.example.obdscanner.ble

import android.bluetooth.le.BluetoothLeScanner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ScannerViewModelFactory(private val bluetoothLeScanner : BluetoothLeScanner) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ScannerViewModel::class.java)){
            ScannerViewModel(bluetoothLeScanner) as T
        }else{
            throw IllegalArgumentException("ViewModel Class Not Found")
        }
    }
}