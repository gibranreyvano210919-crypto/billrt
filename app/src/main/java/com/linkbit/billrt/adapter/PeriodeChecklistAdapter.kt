package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPeriodeChecklistBinding
import java.text.NumberFormat
import java.util.*

data class PeriodeItem(
    val id: String,
    val nama: String,
    val nominal: Float,
    var isChecked: Boolean = true
)

class PeriodeChecklistAdapter(
    private var items: List<PeriodeItem>,
    private val onCheckedChange: () -> Unit
) : RecyclerView.Adapter<PeriodeChecklistAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPeriodeChecklistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPeriodeChecklistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        holder.binding.tvPeriodeNama.text = item.nama
        holder.binding.tvPeriodeNominal.text = formatter.format(item.nominal)
        
        // Remove listener before setting state to avoid triggering it
        holder.binding.cbPeriode.setOnCheckedChangeListener(null)
        holder.binding.cbPeriode.isChecked = item.isChecked

        holder.binding.cbPeriode.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
            onCheckedChange()
        }

        holder.itemView.setOnClickListener {
            holder.binding.cbPeriode.isChecked = !holder.binding.cbPeriode.isChecked
        }
    }

    override fun getItemCount(): Int = items.size

    fun getCheckedIds(): List<String> = items.filter { it.isChecked }.map { it.id }
    
    fun getCheckedTotal(): Float = items.filter { it.isChecked }.sumOf { it.nominal.toDouble() }.toFloat()

    fun updateData(newItems: List<PeriodeItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
