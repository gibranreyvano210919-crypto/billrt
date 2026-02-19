package com.linkbit.billrt

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.adapter.PaymentDetailAdapter
import com.linkbit.billrt.adapter.PaymentSummaryAdapter
import com.linkbit.billrt.databinding.BottomSheetHistoryPembayaranBinding
import com.linkbit.billrt.model.PaymentDetail
import com.linkbit.billrt.model.PaymentSummary
import com.linkbit.billrt.model.Tagihan
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class HistoryPembayaranBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetHistoryPembayaranBinding? = null
    private val binding get() = _binding!!

    private lateinit var summaryAdapter: PaymentSummaryAdapter
    private lateinit var detailAdapter: PaymentDetailAdapter

    private var allPaymentDetails: MutableList<PaymentDetail> = mutableListOf()
    private var customerId: Int = 0

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
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
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
        setupRecyclerViews()

        if (customerId > 0) {
            fetchHistoryPembayaran(customerId)
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

    private fun setupRecyclerViews() {
        summaryAdapter = PaymentSummaryAdapter(emptyList()) { summary ->
            val filteredDetails = allPaymentDetails.filter { it.tahun == summary.tahun }
            detailAdapter.updateData(filteredDetails)
        }
        binding.rvPaymentSummary.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = summaryAdapter
        }

        detailAdapter = PaymentDetailAdapter(emptyList()) { paymentDetail ->
            val detailFragment = DetailTagihanFragment.newInstance(paymentDetail.idTagihan)
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.nav_host_fragment, detailFragment) // Ganti dengan ID container utama Anda
                ?.addToBackStack(null)
                ?.commit()
            dismiss() // Tutup bottom sheet setelah navigasi
        }
        binding.rvPaymentDetail.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = detailAdapter
        }
    }

    private fun fetchHistoryPembayaran(idPelanggan: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.tagihanApiService.getDetailPelangganBayar(idPelanggan)
                if (response.status && response.data != null) {
                    processAndDisplayData(response.data)
                } else {
                    Toast.makeText(context, "Gagal mengambil riwayat pembayaran", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processAndDisplayData(tagihanList: List<Tagihan>) {
        val paymentDetails = mutableListOf<PaymentDetail>()
        val summaryMap = mutableMapOf<Int, MutableList<Tagihan>>()

        for (tagihan in tagihanList) {
            val tahun = tagihan.tahunTagihan.toIntOrNull() ?: 0
            val bulan = tagihan.bulanTagihan.toIntOrNull() ?: 0
            summaryMap.getOrPut(tahun) { mutableListOf() }.add(tagihan)

            paymentDetails.add(
                PaymentDetail(
                    idTagihan = tagihan.idTagihan,
                    tahun = tahun,
                    bulanTagihan = bulan,
                    bulanNama = tagihan.bulanNama,
                    jumlahTagihan = tagihan.hargaPaket.toDouble(), // Use hargaPaket
                    status = tagihan.statusTagihan,
                    tglBayar = tagihan.tanggalBayar ?: "-",
                    metode = tagihan.metodeBayar ?: "-",
                    namaPaket = tagihan.namaPaket,
                    hargaPaket = tagihan.hargaPaket.toDouble()
                )
            )
        }

        val paymentSummaries = summaryMap.map { (tahun, listTagihan) ->
            val totalTagihan = listTagihan.size
            val lunasCount = listTagihan.count { it.statusTagihan == 1 }
            val totalNominal = listTagihan.sumOf { it.hargaPaket.toDouble() } // Use hargaPaket
            val totalBayar = listTagihan.filter { it.statusTagihan == 1 }.sumOf { it.hargaPaket.toDouble() } // Use hargaPaket
            val persentase = if (totalTagihan > 0) (lunasCount.toDouble() / totalTagihan * 100) else 0.0

            PaymentSummary(tahun, totalTagihan, lunasCount, totalNominal, totalBayar, persentase)
        }.sortedByDescending { it.tahun }

        summaryAdapter.updateData(paymentSummaries)

        val sortedPaymentDetails = paymentDetails.sortedWith(compareByDescending<PaymentDetail> { it.tahun }.thenBy { it.bulanTagihan })
        allPaymentDetails.clear()
        allPaymentDetails.addAll(sortedPaymentDetails)

        val mostRecentYear = paymentSummaries.firstOrNull()?.tahun
        if (mostRecentYear != null) {
            val initialDetails = allPaymentDetails.filter { it.tahun == mostRecentYear }
            detailAdapter.updateData(initialDetails)
        } else {
            detailAdapter.updateData(emptyList())
        }
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
