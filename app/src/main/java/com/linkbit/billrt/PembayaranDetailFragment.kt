package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.linkbit.billrt.databinding.FragmentPembayaranDetailBinding
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PembayaranDetailFragment : Fragment() {

    private var _binding: FragmentPembayaranDetailBinding? = null
    private val binding get() = _binding!!

    private var tagihanId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tagihanId = it.getString(ARG_TAGIHAN_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPembayaranDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tagihanId?.let { id ->
            fetchDetailPembayaran(id)
        }
    }

    private fun fetchDetailPembayaran(id: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getDetailBayar(id)
                if (response.status) {
                    val dataJson = response.data
                    if (response.isLunas) {
                        // Use Gson to parse the data object if needed, or parse manually from JsonElement
                        // For simplicity, let's assume we can map it to DetailBayarLunasData
                        val detail = ApiClient.tagihanApiService.getDetailPelangganNew(0) // Dummy call example
                        // In a real scenario, you'd parse response.data using Gson
                        displayLunasDetail(dataJson)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun displayLunasDetail(data: com.google.gson.JsonElement) {
        val detail = com.google.gson.Gson().fromJson(data, DetailBayarLunasData::class.java)
        
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
            maximumFractionDigits = 0
        }

        binding.namaPelanggan.text = "Pelanggan: ${detail.pelanggan}"
        binding.namaPaket.text = "Username: ${detail.username}"
        binding.harga.text = "Nominal: ${numberFormat.format(detail.nominal)}"
        binding.bulanTagihan.text = "Periode: ${detail.periode}"
        binding.tahunTagihan.text = "Metode: ${detail.metode}"
        binding.tglBayar.text = "Tgl Bayar: ${detail.tglBayar}"
        
        // You might want to add more fields to your layout for admin, catatan, etc.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TAGIHAN_ID = "tagihanId"

        @JvmStatic
        fun newInstance(tagihanId: String) =
            PembayaranDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TAGIHAN_ID, tagihanId)
                }
            }
    }
}
