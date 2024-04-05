package com.example.obdscanner.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.obdscanner.R
import com.example.obdscanner.ble.BluetoothLeService
import com.example.obdscanner.ble.ScannerViewModel
import com.example.obdscanner.ble.ScannerViewModelFactory
import com.example.obdscanner.databinding.FragmentScanningBinding
import com.example.obdscanner.logData

private const val TAG = "ScanningFragment"
class ScanningFragment : Fragment() {
    private lateinit var binding : FragmentScanningBinding
    private var bluetoothAdapter : BluetoothAdapter? = null
    private lateinit var scanner: ScannerViewModel
    lateinit var leDeviceListAdapter : LeDeviceListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scanning, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bluetoothManager: BluetoothManager =
            requireContext().getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        initBluetooth()
        leDeviceListAdapter = LeDeviceListAdapter {
            //Start Connecting Service
            val intent = Intent(requireActivity(), BluetoothLeService::class.java)
            intent.putExtra("MAC", it.MAC)
            requireActivity().startService(intent)
            Toast.makeText(requireContext(), "Connecting to ${it.MAC}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.apply {
            adapter = leDeviceListAdapter
            layoutManager = LinearLayoutManager(context).apply {
                orientation = RecyclerView.VERTICAL
            }
        }
        }

        private fun initBluetooth() {
            if (bluetoothAdapter == null) {
                Toast.makeText(requireContext(), "No Bluetooth Supported", Toast.LENGTH_SHORT)
                    .show()
            } else {
                checkBluetoothPermission()
            }
        }

        private fun checkBluetoothPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission is not granted, request it
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                        ),
                        REQUEST_BT_PERMISSION
                    )
                } else {
                    if (bluetoothAdapter?.isEnabled == false) {
                        Toast.makeText(
                            requireContext(),
                            "You Need to turn on Bluetooth",
                            Toast.LENGTH_SHORT
                        ).show()
                        val enableBtIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                        startActivity(enableBtIntent)
                    } else {
                        if (checkLocationPermission()) {
                            startScan()
                        } else {
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ),
                                My_LOCATION_PERMISSION_ID
                            )
                        }
                    }
                }
            } else {
                if (bluetoothAdapter?.isEnabled == false) {
                    Toast.makeText(
                        requireContext(),
                        "You Need to turn on Bluetooth",
                        Toast.LENGTH_SHORT
                    ).show()
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    resultLauncher.launch(enableBtIntent)
                } else {
                    if (checkLocationPermission()) {
                        startScan()
                    } else {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ),
                            My_LOCATION_PERMISSION_ID
                        )
                    }
                }
            }
        }

        private fun checkLocationPermission(): Boolean {
            var result = false
            if ((ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                result = true
            }
            return result
        }

        var resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                    if (checkLocationPermission()) {
                        startScan()
                    } else {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ),
                            My_LOCATION_PERMISSION_ID
                        )
                    }
                }
            }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == My_LOCATION_PERMISSION_ID && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                startScan()
            }
        }

        private fun startScan(){
            val scannerViewModelFactory = ScannerViewModelFactory(bluetoothAdapter!!.bluetoothLeScanner)
            scanner = ViewModelProvider(this, scannerViewModelFactory).get(ScannerViewModel::class.java)
            scanner.scanLeDevice()
            scanner.responseDataState.observe(viewLifecycleOwner) { result ->
                Log.i("TAG", "onViewCreated: StateChanged")
                Log.i(TAG, "onViewCreated: ${result}")
                logData(requireContext(), "Scanning Fragment", "$result")
                leDeviceListAdapter.submitList(result)
                leDeviceListAdapter.notifyDataSetChanged()
            }
            scanner.scanningState.observe(viewLifecycleOwner) {
                Log.i("TAG", "onViewCreated: Scanning changed")
                when (it) {
                    1 -> Toast.makeText(requireContext(), "Scanning", Toast.LENGTH_SHORT).show()
                    0 -> Toast.makeText(requireContext(), "Stopped Scanning", Toast.LENGTH_SHORT).show()
                }
            }
        }

        companion object {
            const val REQUEST_BT_PERMISSION = 2
            private const val My_LOCATION_PERMISSION_ID = 5005
        }
}