package com.linkbit.billrt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemBluetoothDeviceBinding

class BluetoothDeviceAdapter(
    private var devices: List<BluetoothDevice>,
    private val onItemClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {

    @SuppressLint("MissingPermission") // Permissions are checked in the Fragment
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBluetoothDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("MissingPermission") // Permissions are checked in the Fragment
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount() = devices.size

    @SuppressLint("MissingPermission")
    fun updateData(newDevices: List<BluetoothDevice>) {
        this.devices = newDevices.sortedBy { it.name ?: "Unknown Device" }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemBluetoothDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(devices[adapterPosition])
                }
            }
        }

        @SuppressLint("MissingPermission") // Permissions are checked in the Fragment
        fun bind(device: BluetoothDevice) {
            binding.tvDeviceName.text = device.name ?: "Unknown Device"
            binding.tvDeviceAddress.text = device.address
        }
    }
}