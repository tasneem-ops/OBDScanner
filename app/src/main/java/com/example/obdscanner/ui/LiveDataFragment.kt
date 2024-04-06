package com.example.obdscanner.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.obdscanner.R
import com.example.obdscanner.databinding.FragmentLivedataBinding
import com.example.obdscanner.model.db.LocalDataSource
import com.example.obdscanner.ui.viewmodel.DataViewModel
import com.example.obdscanner.ui.viewmodel.DataViewModelFactory
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LiveDataFragment : Fragment() {
    private lateinit var binding : FragmentLivedataBinding
    lateinit var lineList : ArrayList<Entry>
    lateinit var lineData: LineData
    private lateinit var dataViewModel: DataViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_livedata, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModelFactory = DataViewModelFactory(LocalDataSource.getInstance(requireContext()))
        dataViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(DataViewModel::class.java)
        lineList = ArrayList()
        var timeStamp = 0f
        lifecycleScope.launch {
            dataViewModel.respnseDataState
                .collect{list ->
                    list.forEach {
                        if(it.pid == 0x0C.toByte()){
                            lineList.add(Entry(timeStamp, it.data.toFloat()))
                            timeStamp.plus(5f)
                            lineData = LineData(LineDataSet(lineList, "Data"))
                            binding.lineChart.data = lineData
                        }
                    }
                }
        }
    }

}