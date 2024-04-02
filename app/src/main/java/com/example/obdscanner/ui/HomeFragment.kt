package com.example.obdscanner.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.obdscanner.R
import com.example.obdscanner.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding : FragmentHomeBinding
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
    }

}