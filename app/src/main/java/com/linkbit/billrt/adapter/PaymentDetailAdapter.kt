package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.R
import com.linkbit.billrt.databinding.ItemPaymentDetailBinding
import com.linkbit.billrt.model.PaymentDetail
import java.text.NumberFormat
import java.util.Locale

class PaymentDetailAdapter(
    private var details: List<PaymentDetail>,
    private val onItemClick: (PaymentDetail) -> Unit
) : RecyclerView.Adapter<PaymentDetailAdapter.DetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = ItemPaymentDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(details[position])
    }

    override fun getItemCount() = details.size

    fun updateData(newDetails: List<PaymentDetail>) {
        details = newDetails
        notifyDataSetChanged()
    }

    inner class DetailViewHolder(private val binding: ItemPaymentDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(details[position])
                }
            }
        }

        fun bind(detail: PaymentDetail) {
            val context = binding.root.context
            val localeID = Locale("in", "ID")
            val currencyFormat = NumberFormat.getCurrencyInstance(localeID)

            binding.tvBulanTahun.text = "${detail.bulanNama} ${detail.tahun}"
            binding.tvJumlahTagihan.text = currencyFormat.format(detail.hargaPaket)
            binding.tvIdTagihan.text = detail.idTagihan
            binding.tvNamaPaket.text = detail.namaPaket

            if (detail.status == 1) {
                binding.ivStatusIcon.setImageResource(R.drawable.ic_check_circle)
                binding.ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.success))
                binding.tvTanggalBayar.text = "Bayar: ${detail.tglBayar} (${detail.metode})"
            } else {
                binding.ivStatusIcon.setImageResource(R.drawable.ic_error)
                binding.ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.error))
                binding.tvTanggalBayar.text = "Belum Lunas"
            }
        }
    }
}