package com.linkbit.billrt

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.linkbit.billrt.databinding.FragmentKuitansiBinding
import java.text.NumberFormat
import java.util.Locale

class KuitansiFragment : Fragment() {

    private var _binding: FragmentKuitansiBinding? = null
    private val binding get() = _binding!!

    private var pelanggan: PelangganData? = null
    private var tagihan: TagihanData? = null
    private var pembayaran: PembayaranData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            pelanggan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_PELANGGAN, PelangganData::class.java)
            } else {
                it.getSerializable(ARG_PELANGGAN) as? PelangganData
            }
            @Suppress("DEPRECATION")
            tagihan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_TAGIHAN, TagihanData::class.java)
            } else {
                it.getSerializable(ARG_TAGIHAN) as? TagihanData
            }
            @Suppress("DEPRECATION")
            pembayaran = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_PEMBAYARAN, PembayaranData::class.java)
            } else {
                it.getSerializable(ARG_PEMBAYARAN) as? PembayaranData
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKuitansiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindData()
        binding.btnKuitansiSelesai.setOnClickListener {
            parentFragmentManager.popBackStack(DaftarPelangganFragment.DETAIL_PELANGGAN_BACKSTACK_NAME, 0)
        }
    }

    private fun bindData() {
        if (pelanggan == null || tagihan == null || pembayaran == null) return

        binding.kuitansiNamaPelanggan.text = "${pelanggan?.namaPelanggan} (ID: ${pelanggan?.idPelanggan})"
        binding.kuitansiTglBayar.text = pembayaran?.tglBayar

        val monthNames = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val monthIndex = tagihan?.bulanTagihan?.toIntOrNull()
        val period = if (monthIndex != null && monthIndex in 1..12) {
            "Tagihan Bulan ${monthNames[monthIndex - 1]} ${tagihan?.tahunTagihan}"
        } else {
            "Periode tidak valid"
        }
        binding.kuitansiPeriode.text = period

        binding.kuitansiMetodeBayar.text = pembayaran?.metodeBayar

        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val totalAmount = pembayaran?.jumlahBayar?.toDouble() ?: 0.0
        binding.kuitansiTotalBayar.text = format.format(totalAmount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PELANGGAN = "pelanggan"
        private const val ARG_TAGIHAN = "tagihan"
        private const val ARG_PEMBAYARAN = "pembayaran"

        @JvmStatic
        fun newInstance(pelanggan: PelangganData, tagihan: TagihanData, pembayaran: PembayaranData) =
            KuitansiFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PELANGGAN, pelanggan)
                    putSerializable(ARG_TAGIHAN, tagihan)
                    putSerializable(ARG_PEMBAYARAN, pembayaran)
                }
            }
    }
}
