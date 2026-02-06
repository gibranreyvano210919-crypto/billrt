package com.linkbit.billrt

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.FragmentRekapTercatatBinding
import com.linkbit.billrt.databinding.ItemRekapTercatatMingguBinding
import com.linkbit.billrt.databinding.ItemRekapTercatatWilayahBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormatSymbols
import java.util.Calendar

class RekapTercatatFragment : BaseFragment() {

    private var _binding: FragmentRekapTercatatBinding? = null
    private val binding get() = _binding!!

    private var selectedMonth: Int
    private var selectedYear: Int
    private lateinit var rekapAdapter: RekapTercatatAdapter

    init {
        val calendar = Calendar.getInstance()
        selectedMonth = calendar.get(Calendar.MONTH) + 1
        selectedYear = calendar.get(Calendar.YEAR)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRekapTercatatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rekapAdapter = RekapTercatatAdapter(emptyList(),
            onShareClick = { rekapMingguan ->
                shareToWhatsApp(rekapMingguan)
            }
        )
        binding.recyclerView.adapter = rekapAdapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchRekapTercatat()
        }

        setupSpinners()
        fetchRekapTercatat()
    }

    private fun setupSpinners() {
        // Month Spinner
        val months = DateFormatSymbols().months
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBulan.adapter = monthAdapter
        binding.spinnerBulan.setSelection(selectedMonth - 1)
        binding.spinnerBulan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonth = position + 1
                fetchRekapTercatat()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Year Spinner
        val years = (2020..selectedYear + 5).toList().map { it.toString() }
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTahun.adapter = yearAdapter
        binding.spinnerTahun.setSelection(years.indexOf(selectedYear.toString()))
        binding.spinnerTahun.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedYear = years[position].toInt()
                fetchRekapTercatat()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchRekapTercatat() {
        _binding?.swipeRefreshLayout?.isRefreshing = true

        apiService.getRekapTercatat(bulan = selectedMonth, tahun = selectedYear).enqueue(object : Callback<RekapTercatatResponse> {
            override fun onResponse(call: Call<RekapTercatatResponse>, response: Response<RekapTercatatResponse>) {
                if (_binding == null) return
                binding.swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    val rekapResponse = response.body()
                    if (rekapResponse != null && rekapResponse.status) {
                        binding.tvPeriode.text = rekapResponse.periode
                        binding.tvTotalTercatat.text = "Total Tercatat: ${rekapResponse.totalTercatat}"
                        binding.cardSummary.visibility = View.VISIBLE
                        rekapAdapter.updateData(rekapResponse.data)
                    } else {
                        Toast.makeText(context, "Gagal mendapatkan data rekap", Toast.LENGTH_SHORT).show()
                        binding.cardSummary.visibility = View.GONE
                        rekapAdapter.updateData(emptyList())
                    }
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    binding.cardSummary.visibility = View.GONE
                    rekapAdapter.updateData(emptyList())
                }
            }

            override fun onFailure(call: Call<RekapTercatatResponse>, t: Throwable) {
                if (_binding == null) return
                binding.swipeRefreshLayout.isRefreshing = false
                Toast.makeText(context, "Koneksi Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                binding.cardSummary.visibility = View.GONE
                rekapAdapter.updateData(emptyList())
            }
        })
    }

    private fun shareToWhatsApp(rekapMingguan: RekapTercatatMingguan) {
        val builder = StringBuilder()
        builder.append("*REKAP TERCATAT ${rekapMingguan.minggu.uppercase()} (${rekapMingguan.rentang})*\n\n")

        var totalPelangganMingguan = 0
        rekapMingguan.dataWilayah.forEach { wilayah ->
            builder.append("*${wilayah.nama_wilayah}* (${wilayah.jumlah} Pelanggan)\n")
            wilayah.pelanggan.forEach { pelanggan ->
                builder.append("- ${pelanggan.nama_pelanggan} (${pelanggan.mikrotik_username})\n")
            }
            builder.append("\n")
            totalPelangganMingguan += wilayah.jumlah
        }
        builder.append("---------------------\n")
        builder.append("*TOTAL MINGGU INI: $totalPelangganMingguan Pelanggan*\n")

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.setPackage("com.whatsapp")
        intent.putExtra(Intent.EXTRA_TEXT, builder.toString())

        try {
            startActivity(intent)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp tidak terinstall.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Adapters for the RecyclerViews
class RekapTercatatAdapter(
    private var rekapList: List<RekapTercatatMingguan>,
    private val onShareClick: (RekapTercatatMingguan) -> Unit
) :
    RecyclerView.Adapter<RekapTercatatAdapter.MingguViewHolder>() {

    private val expandedPosition = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MingguViewHolder {
        val binding = ItemRekapTercatatMingguBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MingguViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MingguViewHolder, position: Int) {
        holder.bind(rekapList[position])
    }

    override fun getItemCount() = rekapList.size

    fun updateData(newRekapList: List<RekapTercatatMingguan>) {
        rekapList = newRekapList
        expandedPosition.clear()
        notifyDataSetChanged()
    }

    inner class MingguViewHolder(private val binding: ItemRekapTercatatMingguBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(rekapMingguan: RekapTercatatMingguan) {
            binding.tvMinggu.text = rekapMingguan.minggu
            binding.tvRentang.text = "(${rekapMingguan.rentang})"

            val isExpanded = expandedPosition.contains(adapterPosition)
            binding.rvWilayah.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.ivExpandArrow.rotation = if (isExpanded) 180f else 0f

            binding.headerMinggu.setOnClickListener {
                if (isExpanded) {
                    expandedPosition.remove(adapterPosition)
                } else {
                    expandedPosition.add(adapterPosition)
                }
                notifyItemChanged(adapterPosition)
            }

            binding.btnShareWhatsapp.setOnClickListener { onShareClick(rekapMingguan) }

            // Setup nested adapter
            val wilayahAdapter = WilayahAdapter(rekapMingguan.dataWilayah)
            binding.rvWilayah.layoutManager = LinearLayoutManager(itemView.context)
            binding.rvWilayah.adapter = wilayahAdapter
        }
    }
}

class WilayahAdapter(private val wilayahList: List<RekapTercatatWilayah>) :
    RecyclerView.Adapter<WilayahAdapter.WilayahViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WilayahViewHolder {
        val binding = ItemRekapTercatatWilayahBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WilayahViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WilayahViewHolder, position: Int) {
        holder.bind(wilayahList[position])
    }

    override fun getItemCount() = wilayahList.size

    class WilayahViewHolder(private val binding: ItemRekapTercatatWilayahBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(rekapWilayah: RekapTercatatWilayah) {
            binding.tvWilayah.text = "${rekapWilayah.nama_wilayah} (${rekapWilayah.jumlah} Pelanggan)"
            binding.llPelanggan.removeAllViews()
            for (pelanggan in rekapWilayah.pelanggan) {
                val pelangganView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_pelanggan_tercatat, binding.llPelanggan, false)
                pelangganView.findViewById<TextView>(R.id.tvNamaPelanggan).text = pelanggan.nama_pelanggan
                pelangganView.findViewById<TextView>(R.id.tvUsername).text = pelanggan.mikrotik_username
                pelangganView.findViewById<TextView>(R.id.tvTanggalCatat).text = pelanggan.tanggal_catat
                binding.llPelanggan.addView(pelangganView)
            }
        }
    }
}