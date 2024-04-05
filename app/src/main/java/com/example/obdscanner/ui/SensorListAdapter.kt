package com.example.obdscanner.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.obdscanner.R
import com.example.obdscanner.databinding.SensorItemBinding
import com.example.obdscanner.model.dto.SensorReading

private const val TAG = "SensorListAdapter"
class SensorListAdapter() : ListAdapter<SensorReading, SensorViewHolder>(SensorDiffUtil()) {
    lateinit var binding: SensorItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        Log.i(TAG, "onCreateViewHolder: ")
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.sensor_item, parent, false)
        return SensorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        Log.i(TAG, "onBindViewHolder: ")
    }
}
class SensorViewHolder(var binding : SensorItemBinding) : RecyclerView.ViewHolder(binding.root)

class SensorDiffUtil : DiffUtil.ItemCallback<SensorReading>() {
    override fun areItemsTheSame(oldItem: SensorReading, newItem: SensorReading): Boolean {
        return oldItem.pid == newItem.pid
    }

    override fun areContentsTheSame(oldItem: SensorReading, newItem: SensorReading): Boolean {
        return oldItem == newItem
    }
}