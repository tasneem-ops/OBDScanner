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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.obdscanner.R
import com.example.obdscanner.databinding.FragmentAllSensorsBinding
import com.example.obdscanner.model.db.LocalDataSource
import com.example.obdscanner.ui.viewmodel.DataViewModel
import com.example.obdscanner.ui.viewmodel.DataViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class AllSensorsFragment : Fragment() {
    private lateinit var binding : FragmentAllSensorsBinding
    private lateinit var listAdapter: SensorListAdapter
    private lateinit var dataViewModel: DataViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_sensors, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModelFactory = DataViewModelFactory(LocalDataSource.getInstance(requireContext()))
        dataViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(DataViewModel::class.java)
        listAdapter = SensorListAdapter()
        binding.allSensorsList.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = RecyclerView.VERTICAL
            }
        }
        lifecycleScope.launch {
            dataViewModel.respnseDataState
                .collectLatest {
                    Log.i("TAG", "onViewCreated: ${it}")
                    listAdapter.submitList(it)
                }
        }
    }
}