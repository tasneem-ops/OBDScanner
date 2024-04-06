package com.example.obdscanner.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.obdscanner.Constants.STATE_CONNECTED
import com.example.obdscanner.Constants.STATE_CONNECTING
import com.example.obdscanner.Constants.STATE_DISCONNECTED
import com.example.obdscanner.R
import com.example.obdscanner.databinding.FragmentDashboardBinding
import com.example.obdscanner.model.db.LocalDataSource
import com.example.obdscanner.ui.viewmodel.DataViewModel
import com.example.obdscanner.ui.viewmodel.DataViewModelFactory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class DashboardFragment : Fragment() {
    private lateinit var binding : FragmentDashboardBinding
    private lateinit var dataViewModel: DataViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_dashboard, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModelFactory = DataViewModelFactory(LocalDataSource.getInstance(requireContext()))
        dataViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(DataViewModel::class.java)
        lifecycleScope.launch {
            dataViewModel.singleSensorData
                .catch {  }
                .collectLatest {
                    if(it == null){
                        Log.i("TAG", "onViewCreated: Null !!!!")
                    }
                    else{
                        binding.sensorDataText.text = it.data.toString()
                        binding.sensorUnitText.text = it.unit
                        binding.sensorNameText.text = it.name
                    }

                }
        }
        dataViewModel.getAllSensorReadings()
        dataViewModel.getSensorReading(0x01)
    }
}