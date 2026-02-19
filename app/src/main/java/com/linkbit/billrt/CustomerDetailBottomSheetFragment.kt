package com.linkbit.billrt

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetCustomerDetailBinding
import com.linkbit.billrt.model.PelangganDetail
import com.linkbit.billrt.viewmodel.CustomerDetailViewModel

class CustomerDetailBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCustomerDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CustomerDetailViewModel

    private var pelangganId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pelangganId = it.getInt(ARG_PELANGGAN_ID)
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
        _binding = BottomSheetCustomerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        viewModel = ViewModelProvider(this).get(CustomerDetailViewModel::class.java)

        observeViewModel()

        if (pelangganId > 0) {
            viewModel.fetchCustomerDetail(pelangganId)
        } else {
            Toast.makeText(requireContext(), "ID Pelanggan tidak valid", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun observeViewModel() {
        viewModel.customerDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let { updateUi(it) }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You can add a progress bar to your layout and control its visibility here
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUi(detail: PelangganDetail) {
        binding.tvNamaPelanggan.text = "Nama: ${detail.nama_pelanggan}"
        binding.tvAlamatPelanggan.text = "Alamat: ${detail.alamat_pelanggan}"
        binding.tvTeleponPelanggan.text = "Telepon: ${detail.telepon_pelanggan}"
        binding.tvWilayah.text = "Wilayah: ${detail.wilayah}"
        binding.tvPaketInternet.text = "Paket: ${detail.paket_internet}"
        binding.tvStatusAktif.text = "Status: ${detail.status_aktif}"
        binding.tvPppoeUsername.text = "Username PPPoE: ${detail.pppoe_username}"
        binding.tvStaticIp.text = "IP Statis: ${detail.static_ip}"
        binding.tvMacAddress.text = "MAC Address: ${detail.mac_address}"
        binding.tvTglDaftar.text = "Tgl Daftar: ${detail.tgl_daftar}"
        binding.tvTglInstalasi.text = "Tgl Instalasi: ${detail.tgl_instalasi}"
        binding.tvLastPaid.text = "Terakhir Bayar: ${detail.last_paid}"
        binding.tvTeknisi.text = "Teknisi: ${detail.teknisi}"
        binding.tvNotes.text = "Catatan: ${detail.notes}"

        detail.foto_lokasi?.let {
            binding.ivFotoLokasi.isVisible = true
            Glide.with(this)
                .load(it)
                .into(binding.ivFotoLokasi)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CustomerDetailBottomSheetFragment"
        private const val ARG_PELANGGAN_ID = "pelanggan_id"

        fun newInstance(pelangganId: Int): CustomerDetailBottomSheetFragment {
            val fragment = CustomerDetailBottomSheetFragment()
            val args = Bundle()
            args.putInt(ARG_PELANGGAN_ID, pelangganId)
            fragment.arguments = args
            return fragment
        }
    }
}
