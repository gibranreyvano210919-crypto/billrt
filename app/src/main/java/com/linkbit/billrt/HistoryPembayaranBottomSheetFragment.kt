package com.linkbit.billrt

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.adapter.TimelineAdapter
import com.linkbit.billrt.databinding.BottomSheetHistoryPembayaranBinding
import com.linkbit.billrt.model.HistoryTagihan
import com.linkbit.billrt.model.TimelineItem
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class HistoryPembayaranBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetHistoryPembayaranBinding? = null
    private val binding get() = _binding!!

    private lateinit var timelineAdapter: TimelineAdapter

    private var customerId: Int = 0
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    private var isSpinnerInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            customerId = it.getInt(ARG_CUSTOMER_ID)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetHistoryPembayaranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupYearSpinner()

        if (customerId > 0) {
            fetchHistoryPembayaran(customerId, selectedYear)
        } else {
            Toast.makeText(context, "ID Pelanggan tidak valid.", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }
    }

    private fun setupRecyclerView() {
        timelineAdapter = TimelineAdapter(emptyList())
        binding.rvTimeline.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timelineAdapter
        }
    }

    private fun setupYearSpinner() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 3..currentYear + 1).map { it.toString() }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTahun.adapter = adapter

        val defaultIndex = years.indexOf(selectedYear.toString())
        if (defaultIndex >= 0) {
            binding.spinnerTahun.setSelection(defaultIndex)
        }

        binding.spinnerTahun.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val chosenYear = years[position].toIntOrNull() ?: currentYear
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true
                    return
                }
                if (chosenYear != selectedYear) {
                    selectedYear = chosenYear
                    fetchHistoryPembayaran(customerId, selectedYear)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchHistoryPembayaran(idPelanggan: Int, tahun: Int? = null) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.tagihanApiService.getDetailPelangganBayar2(idPelanggan, tahun)
                if (response.status && response.data != null) {
                    val timelineItems = response.data.map { tagihan ->
                        mapToTimelineItem(tagihan)
                    }
                    timelineAdapter.updateData(timelineItems)
                    
                    response.profil?.let { profil ->
                        binding.tvNamaPelanggan.text = profil.namaPelanggan
                        binding.tvIdPelanggan.text = "ID: ${profil.idPelanggan}"
                        binding.tvWilayah.text = "Wilayah: ${profil.wilayah ?: "-"}"
                        binding.tvInstallationDate.text = "Tgl Pasang: ${profil.installationDate ?: "-"}"
                        
                        // Menampilkan nama pencatat terakhir secara dinamis di header
                        val latestPaid = response.data.firstOrNull { t -> t.statusTagihan == 1 }
                        if (latestPaid != null) {
                            val name = latestPaid.namaPencatat
                            val id = latestPaid.idUserPencatat
                            val headerText = if (!name.isNullOrEmpty() && name != "-") {
                                if (id != null && id > 0) "Pencatat Terakhir: $name (ID: $id)" else "Pencatat Terakhir: $name"
                            } else "Pencatat Terakhir: Sistem"
                            binding.tvAdminPencatat.text = headerText
                            binding.tvAdminPencatat.visibility = View.VISIBLE
                        } else {
                            binding.tvAdminPencatat.visibility = View.GONE
                        }

                        if (response.data.isNotEmpty()) {
                            val namaPaket = response.data[0].namaPaket ?: "-"
                            val hargaPaket = response.data[0].hargaPaket
                            val textPaket = if (hargaPaket != null && hargaPaket > 0) {
                                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                                val formattedHarga = currencyFormat.format(hargaPaket).replace("Rp", "Rp ")
                                "Paket: $namaPaket ($formattedHarga)"
                            } else {
                                "Paket: $namaPaket"
                            }
                            binding.tvPaket.text = textPaket
                            binding.tvPaket.visibility = View.VISIBLE
                        } else {
                            binding.tvPaket.visibility = View.GONE
                        }
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil riwayat pembayaran", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mapToTimelineItem(tagihan: HistoryTagihan): TimelineItem {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val formattedAmount = currencyFormat.format(tagihan.jumlahTagihan).replace("Rp", "Rp ")

        val dotColor = when (tagihan.statusTagihan) {
            1 -> "#4CAF50" // Lunas
            2 -> "#FF9800" // Tagout
            else -> "#F44336" // Belum Bayar
        }

        val dateDisplay = if (tagihan.statusTagihan == 1) {
            if (!tagihan.tanggalBayar.isNullOrEmpty()) "Dibayar: ${tagihan.tanggalBayar}" else "Lunas"
        } else {
            if (!tagihan.tglJatuhTempo.isNullOrEmpty()) "Jatuh Tempo: ${tagihan.tglJatuhTempo}" else "Belum Dibayar"
        }

        // Gabungkan Nama dan ID untuk item list
        val adminInfo = if (tagihan.statusTagihan == 1) {
            val name = tagihan.namaPencatat
            val id = tagihan.idUserPencatat
            if (!name.isNullOrEmpty() && name != "-") {
                if (id != null && id > 0) "$name (ID: $id)" else name
            } else null
        } else null

        val bulanNama = tagihan.bulanNama ?: "Bulan ${tagihan.bulanTagihan}"
        val statusText = tagihan.statusText ?: when (tagihan.statusTagihan) {
            1 -> "Lunas"
            2 -> "Tagout (Ditangguhkan)"
            else -> "Belum Bayar"
        }
        return TimelineItem(
            title = "$bulanNama ${tagihan.tahunTagihan}",
            subtitle = formattedAmount,
            dateDisplay = dateDisplay,
            statusText = statusText,
            dotColor = dotColor,
            description = "",
            hasInvoice = tagihan.idTagihan != 0 && tagihan.idTagihan != "0",
            idTagihan = tagihan.idTagihan.toString(),
            adminPencatat = adminInfo
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "HistoryPembayaranBottomSheetFragment"
        private const val ARG_CUSTOMER_ID = "customer_id"

        fun newInstance(customerId: Int): HistoryPembayaranBottomSheetFragment {
            val fragment = HistoryPembayaranBottomSheetFragment()
            val args = Bundle()
            args.putInt(ARG_CUSTOMER_ID, customerId)
            fragment.arguments = args
            return fragment
        }
    }
}
