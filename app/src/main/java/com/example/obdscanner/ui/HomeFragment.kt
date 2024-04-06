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
import androidx.navigation.Navigation
import com.example.obdscanner.Constants
import com.example.obdscanner.R
import com.example.obdscanner.databinding.FragmentHomeBinding
import com.example.obdscanner.model.db.LocalDataSource
import com.example.obdscanner.ui.viewmodel.DataViewModel
import com.example.obdscanner.ui.viewmodel.DataViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var binding : FragmentHomeBinding
    private lateinit var dataViewModel: DataViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModelFactory = DataViewModelFactory(LocalDataSource.getInstance(requireContext()))
        dataViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(DataViewModel::class.java)
        binding.dashboardCard.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToDashboardFragment()
            Navigation.findNavController(it).navigate(action)
        }
        binding.sensorsCard.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToAllSensorsFragment()
            Navigation.findNavController(it).navigate(action)
        }
        binding.settingsCard.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
            Navigation.findNavController(it).navigate(action)
        }
        binding.connectionCard.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToScanningFragment()
            Navigation.findNavController(it).navigate(action)
        }
        lifecycleScope.launch {
            dataViewModel.connectionStatus
                .collectLatest {
                    if (it == null){
                        Log.i("TAG", "onViewCreated: NULL!!!!")
                    }
                    else{
                        when(it.state){
                            Constants.STATE_CONNECTED ->{
                                binding.disconnectedCard.visibility = View.GONE
                                binding.connectedCard.visibility = View.VISIBLE
                                binding.connectionCard.isClickable = false
                            }
                            Constants.STATE_DISCONNECTED ->{
                                binding.disconnectedCard.visibility = View.VISIBLE
                                binding.connectedCard.visibility = View.GONE
                                binding.connectionCard.isClickable = true
                            }
                            Constants.STATE_CONNECTING ->{
                                binding.disconnectedCard.visibility = View.VISIBLE
                                binding.connectedCard.visibility = View.GONE
                                binding.connectionCard.isClickable = false
                            }
                        }
                    }
                }
        }
    }

}