package com.linkbit.billrt.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.R
import com.linkbit.billrt.databinding.ItemCustomerBinding
import com.linkbit.billrt.model.Customer

class CustomerAdapter(
    private var customerList: List<Customer>,
    private val onItemClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customerList[position]
        holder.bind(customer)
        holder.itemView.setOnClickListener { 
            onItemClick(customer)
        }
    }

    override fun getItemCount() = customerList.size

    fun updateData(newCustomerList: List<Customer>) {
        customerList = newCustomerList
        notifyDataSetChanged()
    }

    inner class CustomerViewHolder(private val binding: ItemCustomerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(customer: Customer) {
            val context = binding.root.context
            binding.tvCustomerId.text = customer.id.toString()
            binding.tvCustomerName.text = customer.nama
            binding.tvMikrotikUsername.text = customer.userMk
            binding.tvTelepon.text = customer.telp
            binding.tvInstallationDate.text = customer.tgl
            binding.tvWilayah.text = customer.wil
            
            // Set billing status text
            binding.tvStatus.text = customer.statusTagihan

            // Set billing status color
            if (customer.statusTagihan.contains("Lunas", ignoreCase = true)) {
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.md_green_500))
            } else {
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.md_red_A700))
            }

            // Handle arrears display
            binding.llArrears.removeAllViews()
            if (customer.bulanAngka.isNotEmpty()) {
                binding.llArrears.visibility = View.VISIBLE
                val periods = customer.bulanAngka
                val colors = intArrayOf(R.color.md_red_A700, R.color.md_pink_A700, R.color.md_purple_A700)

                periods.forEachIndexed { index, period ->
                    val textView = TextView(context).apply {
                        text = period
                        textSize = 12f
                        setTextColor(Color.WHITE)
                        val colorRes = colors[index % colors.size]
                        setBackgroundColor(ContextCompat.getColor(context, colorRes))
                        setPadding(8, 4, 8, 4)

                        val lp = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        lp.setMargins(0, 0, 8, 4)
                        layoutParams = lp
                    }
                    binding.llArrears.addView(textView)
                }
            } else {
                binding.llArrears.visibility = View.GONE
            }
        }
    }
}
