package com.example.obdscanner.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.obdscanner.R
import com.example.obdscanner.databinding.BleItemBinding
import com.example.obdscanner.model.dto.BleDevice

private const val TAG = "LeDeviceListAdapter"
class LeDeviceListAdapter(val onClick : (BleDevice) -> Unit) : ListAdapter<BleDevice, BLeDeviceViewHolder>(BLeDeviceDiffUtil()) {
    lateinit var binding: BleItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BLeDeviceViewHolder {
        Log.i(TAG, "onCreateViewHolder: ")
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.ble_item, parent, false)
        return BLeDeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BLeDeviceViewHolder, position: Int) {
        Log.i(TAG, "onBindViewHolder: ${getItem(position).MAC}")
        binding.device = getItem(position)
        holder.binding.connectBtn.setOnClickListener { onClick(getItem(position)) }
    }
}
class BLeDeviceViewHolder(var binding : BleItemBinding) : RecyclerView.ViewHolder(binding.root)

class BLeDeviceDiffUtil : DiffUtil.ItemCallback<BleDevice>() {
    override fun areItemsTheSame(oldItem: BleDevice, newItem: BleDevice): Boolean {
        return oldItem.MAC == newItem.MAC
    }

    override fun areContentsTheSame(oldItem: BleDevice, newItem: BleDevice): Boolean {
        return oldItem == newItem
    }
}
