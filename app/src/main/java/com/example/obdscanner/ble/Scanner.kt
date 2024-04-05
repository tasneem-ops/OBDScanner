package com.example.obdscanner.ble

import android.annotation.SuppressLint
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.obdscanner.model.dto.BleDevice
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class ScannerViewModel (private val bluetoothLeScanner : BluetoothLeScanner) : ViewModel(){
    private var scanning = false
    private val handler = Handler()
    private val SCAN_PERIOD: Long = 10000
    private var availableDevices : ArrayList<BleDevice> = arrayListOf<BleDevice>()

    private val _responseDataState = MutableLiveData<List<BleDevice>>()
    val responseDataState: LiveData<List<BleDevice>> = _responseDataState

    private val _scanningState = MutableLiveData<Int>()
    val scanningState: LiveData<Int> = _scanningState
    @SuppressLint("MissingPermission")
    fun scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a pre-defined scan period.
            _scanningState.postValue(SCANNING)
            Log.i(TAG, "scanLeDevice: Scanning")
            availableDevices.clear()
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                Log.i(TAG, "scanLeDevice: Stopping")
                Log.i(TAG, "onScanResult: $availableDevices")
                _responseDataState.postValue(availableDevices)
                _scanningState.postValue(NO_SCAN)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        }
    }
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device : BleDevice = BleDevice(result.device.name ?: "UNKNOWN DEVICE", result.device.address?: "NO MAC ADDRESS FOUND")
            var count = 0
            for(savedDevice in availableDevices){
                if(savedDevice.MAC == device.MAC){
                    count++
                    break
                }
            }
            if(count == 0){
                availableDevices.add(device)
                _responseDataState.postValue(availableDevices)
            }
        }
    }

    companion object{
        const val SCANNING = 1
        const val NO_SCAN = 0
        private const val TAG = "Scanner"

    }
}