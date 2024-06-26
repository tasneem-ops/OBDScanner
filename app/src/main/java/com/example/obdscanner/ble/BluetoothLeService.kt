package com.example.obdscanner.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.obdscanner.Constants.STATE_CONNECTED
import com.example.obdscanner.Constants.STATE_DISCONNECTED
import com.example.obdscanner.logData
import com.example.obdscanner.model.db.LocalDataSource
import com.example.obdscanner.model.dto.ConnectionStatus
import com.example.obdscanner.model.dto.SensorReading
import com.example.obdscanner.notification.NOTIFICATION_ID
import com.example.obdscanner.notification.getForegroundNotification
import com.example.obdscanner.notification.sendDataNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import java.util.Queue
import java.util.Random
import java.util.UUID
import kotlin.math.log

class BluetoothLeService : Service() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    val mainHandler = Handler(Looper.getMainLooper())
    var address : String? = null
    private val commandQueue: Queue<Runnable> = ArrayDeque()
    private val readQueue : Queue<BluetoothGattCharacteristic> = ArrayDeque()
    private var commandQueueBusy = false
    var nrTries = 0
    lateinit var localDataSource: LocalDataSource
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent?.action.equals("STOP")){
            Log.i(TAG, "onStartCommand: Stopping")
            bluetoothGatt?.disconnect()
            stopSelf()
            return START_NOT_STICKY;
        }
        else{
            startForeground()
            if(initialize()){
                intent?.getStringExtra("MAC")?.let {
                    Log.i(TAG, "onStartCommand: Connecting....")
                    connect(it)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private fun startForeground(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val bluetoothPermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            if (bluetoothPermission == PackageManager.PERMISSION_DENIED) {
                stopSelf()
                return
            }
        }
        try {
            val notification = getForegroundNotification(this).build()
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                } else {
                    0
                }
            )
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e is ForegroundServiceStartNotAllowedException
            ) {
                Log.i(
                    TAG,
                    "startForeground: App is not in a valid state to start foreground service"
                )
            }
        }
    }
    fun initialize(): Boolean {
        localDataSource = LocalDataSource.getInstance(this)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }
    @SuppressLint("MissingPermission", "LogNotTimber")
    fun connect(address: String): Boolean {
        this.address = address
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                // connect to the GATT server on the device
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                return true
            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.")
                return false
            }
            // connect to the GATT server on the device
        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized")
            logData(this, TAG, "BluetoothAdapter not initialized")
            return false
        }
    }
    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.i(TAG, "onConnectionStateChange: Connected")
                logData(this@BluetoothLeService, TAG, "onConnectionStateChange: Connected")
                scope.launch {
                    localDataSource.updateConnectionStatus(ConnectionStatus(state = STATE_CONNECTED))
                }
            }
            else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.i(TAG, "onConnectionStateChange: Disconnected")
                logData(this@BluetoothLeService, TAG, "onConnectionStateChange: Disconnected")
                scope.launch {
                    localDataSource.updateConnectionStatus(ConnectionStatus(state = STATE_DISCONNECTED))
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service =
                    gatt?.getService(UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB"))
                val char =
                    service?.getCharacteristic(UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB"))
                if (char != null) {
                    Log.i(TAG, "onServicesDiscovered: Found Characteristic")
                    logData(this@BluetoothLeService, TAG, "onServicesDiscovered: Found Characteristic FFF1")
                    setCharacteristicNotification(char, true)
                } else {
                    Log.i(TAG, "onServicesDiscovered: Characteristic is Null")
                    logData(this@BluetoothLeService, TAG, "onServicesDiscovered: Characteristic is Null")
                }
            }
            else {
                Log.w(TAG, "onServicesDiscovered received: $status")
                logData(this@BluetoothLeService,TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.i(TAG, "onCharacteristicWrite: ${characteristic?.uuid}")
            logData(this@BluetoothLeService, TAG, "onCharacteristicWrite: ${characteristic?.uuid}")
            completedCommand()
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.i(TAG, "onDescriptorWrite: ${descriptor?.characteristic?.uuid}")
            logData(this@BluetoothLeService, TAG, "onDescriptorWrite: ${descriptor?.characteristic?.uuid}")
            //Start Sending AT Commands
            for (pid in DecodingUtils.getSensors().keys){
                writeATCommand(pid)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.i(TAG, "onCharacteristicChanged: Finallly Got the value!!!!  ${characteristic?.uuid}      ${characteristic?.value?.decodeToString()} ${String(characteristic?.value ?: byteArrayOf() , Charsets.UTF_8)}")
            logData(this@BluetoothLeService, TAG, "onCharacteristicChanged: Finallly Got the value!!!!  ${characteristic?.uuid}")
            val pid = characteristic?.value?.get(1)
            val firstByte = characteristic?.value?.get(3)
            val secondByte = characteristic?.value?.get(4)
            if(firstByte != null && secondByte != null){
                sendDataNotification(this@BluetoothLeService, "${characteristic.value}")
                logData(this@BluetoothLeService, TAG, "Data From OBD2: ${characteristic.value}")
                scope.launch {
                    val sensorReading = DecodingUtils.decodePidData(pid, firstByte, secondByte)
                    if (sensorReading  != null){
                        localDataSource.insertOrUpdate(sensorReading)
                    }
                }
            }
            else{
                sendDataNotification(this@BluetoothLeService, "No Data")
                logData(this@BluetoothLeService, TAG, "Data From OBD2: No Data")
            }
        }
    }
    @SuppressLint("MissingPermission")
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        bluetoothGatt?.let { gatt ->
            gatt.setCharacteristicNotification(characteristic, enabled)
            val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
            if(descriptor == null){
                Log.i(TAG, "setCharacteristicNotification: Null Descriptor")
                logData(this, TAG, "setCharacteristicNotification: Null Descriptor")
            }
            else{
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                val writeResult = gatt.writeDescriptor(descriptor)
                if(writeResult){
                    Log.i(TAG, "setCharacteristicNotification: Good Done Writing")
                    logData(this, TAG, "setCharacteristicNotification: Good Done Writing")
                } else {
                    Log.i(TAG, "setCharacteristicNotification: Couldn't Write")
                    logData(this, TAG, "setCharacteristicNotification: Couldn't Write")
                }
            }
        } ?: run {
            Log.w(TAG, "BluetoothGatt not initialized")
        }
    }
    @SuppressLint("MissingPermission")
    fun writeATCommand(pid : Byte): Boolean {
        if(bluetoothGatt == null){
            return false
        }
        else{
            val service = bluetoothGatt?.getService(UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB"))
            val characteristic = service?.getCharacteristic(UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB"))
            if (characteristic != null) {
                Log.i(TAG, "Writing to Characteristic ${characteristic.uuid} with pid: ${pid}")
                logData(this@BluetoothLeService, TAG, "Writing to Characteristic: ${characteristic.uuid} with pid: ${pid}")

                // Check if this characteristic actually has READ property
                if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ == 0) {
                    Log.e(TAG, "ERROR: Characteristic cannot be read")
                    return false
                }
                // Enqueue the read command now that all checks have been passed
                val result = commandQueue!!.add(Runnable {
                    characteristic.value = byteArrayOf(0x01, pid)

                    if (!bluetoothGatt?.writeCharacteristic(characteristic)!!) {
                        Log.e(
                            TAG,
                            String.format(
                                "ERROR: readCharacteristic failed for characteristic: %s",
                                characteristic.uuid
                            )
                        )
                        completedCommand()
                    } else {
                        Log.d(TAG, String.format("reading characteristic <%s>", characteristic.uuid))
                        nrTries++
                    }
                })
                if (result) {
                    nextCommand()
                } else {
                    Log.e(TAG, "ERROR: Could not enqueue read characteristic command")
                }
                return result
            }
            else{
                return false
            }
        }
    }
    private fun nextCommand() {
        // If there is still a command being executed then bail out
        if (commandQueueBusy) {
            return
        }

        // Check if we still have a valid gatt object
        if (bluetoothGatt == null) {
            Log.e(
                TAG,
                java.lang.String.format(
                    "ERROR: GATT is 'null' for peripheral, clearing command queue"
                )
            )
            commandQueue!!.clear()
            commandQueueBusy = false
            return
        }

        // Execute the next command in the queue
        if (commandQueue!!.size > 0) {
            val bluetoothCommand = commandQueue.peek()
            commandQueueBusy = true
            nrTries = 0
            mainHandler.post(Runnable {
                try {
                    bluetoothCommand.run()
                } catch (ex: java.lang.Exception) {
                    Log.e(
                        TAG,
                        java.lang.String.format(
                            "ERROR: Command exception for device"
                        ),
                        ex
                    )
                }
            })
        }
    }
    private fun completedCommand() {
        commandQueueBusy = false
//        isRetrying = false
        commandQueue!!.poll()
        nextCommand()
    }
    companion object{
        private const val TAG = "BluetoothLeService"
    }
}