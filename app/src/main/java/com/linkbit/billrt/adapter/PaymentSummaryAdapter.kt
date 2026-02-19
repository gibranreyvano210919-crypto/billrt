package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPaymentSummaryBinding
import com.linkbit.billrt.model.PaymentSummary
import java.text.NumberFormat
import java.util.Locale

class PaymentSummaryAdapter(
    private var summaries: List<PaymentSummary>,
    private val onItemClicked: (PaymentSummary) -> Unit
) : RecyclerView.Adapter<PaymentSummaryAdapter.SummaryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val binding = ItemPaymentSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SummaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        val summary = summaries[position]
        holder.bind(summary)
        holder.itemView.setOnClickListener {
            onItemClicked(summary)
        }
    }

    override fun getItemCount() = summaries.size

    fun updateData(newSummaries: List<PaymentSummary>) {
        summaries = newSummaries
        notifyDataSetChanged()
    }

    inner class SummaryViewHolder(private val binding: ItemPaymentSummaryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(summary: PaymentSummary) {
            val localeID = Locale("in", "ID")
            val currencyFormat = NumberFormat.getCurrencyInstance(localeID)

            binding.tvTahun.text = summary.tahun.toString()
            binding.progressPerforma.progress = summary.persentaseLunas.toInt()
            binding.tvPersentaseLunasValue.text = "${summary.persentaseLunas}%"
            binding.tvTotalTagihan.text = "Total Tagihan: ${summary.totalTagihanCount} (${currencyFormat.format(summary.totalNominalTagihan)})"
            binding.tvTotalBayar.text = "Total Bayar: ${summary.lunasCount} (${currencyFormat.format(summary.totalNominalBayar)})"
        }
    }
}