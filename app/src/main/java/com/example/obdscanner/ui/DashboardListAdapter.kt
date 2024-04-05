package com.example.obdscanner.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.obdscanner.R
import com.example.obdscanner.databinding.DashboardItemBinding
import com.example.obdscanner.model.dto.SensorReading

private const val TAG = "SensorListAdapter"
class DashboardListAdapter() : ListAdapter<SensorReading, DashboardViewHolder>(DashboardDiffUtil()) {
    lateinit var binding: DashboardItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        Log.i(TAG, "onCreateViewHolder: ")
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.dashboard_item, parent, false)
        return DashboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        Log.i(TAG, "onBindViewHolder: ")
    }
}
class DashboardViewHolder(var binding : DashboardItemBinding) : RecyclerView.ViewHolder(binding.root)

class DashboardDiffUtil : DiffUtil.ItemCallback<SensorReading>() {
    override fun areItemsTheSame(oldItem: SensorReading, newItem: SensorReading): Boolean {
        return oldItem.pid == newItem.pid
    }

    override fun areContentsTheSame(oldItem: SensorReading, newItem: SensorReading): Boolean {
        return oldItem == newItem
    }
}