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
import com.linkbit.billrt.adapter.TimelineAdapter
import com.linkbit.billrt.databinding.BottomSheetHistoryPembayaranBinding
import com.linkbit.billrt.model.TimelineItem
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class HistoryPembayaranBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetHistoryPembayaranBinding? = null
    private val binding get() = _binding!!

    private lateinit var timelineAdapter: TimelineAdapter

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
        setupRecyclerView()

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

    private fun setupRecyclerView() {
        timelineAdapter = TimelineAdapter(emptyList())
        binding.rvTimeline.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timelineAdapter
        }
    }

    private fun fetchHistoryPembayaran(idPelanggan: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.tagihanApiService.getDetailPelangganBayar2(idPelanggan)
                if (response.status && response.timeline != null) {
                    timelineAdapter.updateData(response.timeline)
                    response.profil?.let {
                        binding.tvNamaPelanggan.text = it.namaPelanggan
                        binding.tvIdPelanggan.text = "ID: ${it.idPelanggan}"
                        binding.tvPaket.text = it.paket
                        binding.tvWilayah.text = it.wilayah
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil riwayat pembayaran", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
