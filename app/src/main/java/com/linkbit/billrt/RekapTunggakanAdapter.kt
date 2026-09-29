package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemRekapTunggakanWilayahBinding

class RekapTunggakanAdapter(
    private var rekapList: List<RekapTunggakanWilayah>,
    private val onShareClick: (RekapTunggakanWilayah) -> Unit,
    private val onPrintClick: (RekapTunggakanWilayah) -> Unit
) : RecyclerView.Adapter<RekapTunggakanAdapter.ViewHolder>() {

    private val expandedPosition = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRekapTunggakanWilayahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rekapList[position])
    }

    override fun getItemCount() = rekapList.size

    fun updateData(newRekapList: List<RekapTunggakanWilayah>) {
        rekapList = newRekapList
        expandedPosition.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemRekapTunggakanWilayahBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rekap: RekapTunggakanWilayah) {
            binding.tvNamaWilayahTunggakan.text = "${rekap.nama_wilayah} (${rekap.jumlah})"

            val isExpanded = expandedPosition.contains(adapterPosition)
            binding.llPelangganTunggakan.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.ivExpandArrowTunggakan.rotation = if (isExpanded) 180f else 0f

            binding.headerWilayahTunggakan.setOnClickListener {
                if (isExpanded) {
                    expandedPosition.remove(adapterPosition)
                } else {
                    expandedPosition.add(adapterPosition)
                }
                notifyItemChanged(adapterPosition)
            }

            binding.btnShareTunggakan.setOnClickListener { onShareClick(rekap) }
            binding.btnPrintTunggakan.setOnClickListener { onPrintClick(rekap) }

            // Inflate customer details into the LinearLayout
            binding.llPelangganTunggakan.removeAllViews()
            val inflater = LayoutInflater.from(itemView.context)
            rekap.pelanggan.forEach { pelanggan ->
                val view = inflater.inflate(R.layout.item_rekap_tunggakan_pelanggan, binding.llPelangganTunggakan, false)
                view.findViewById<TextView>(R.id.tv_nama_pelanggan_tunggakan).text = pelanggan.nama_pelanggan
                view.findViewById<TextView>(R.id.tv_username_tunggakan).text = pelanggan.mikrotik_username
                binding.llPelangganTunggakan.addView(view)
            }
        }
    }
}
