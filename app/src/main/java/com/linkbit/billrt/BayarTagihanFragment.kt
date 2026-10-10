package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.linkbit.billrt.databinding.FragmentBayarTagihanBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class BayarTagihanFragment : BaseFragment() {

    private var _binding: FragmentBayarTagihanBinding? = null
    private val binding get() = _binding!!
    private val args: BayarTagihanFragmentArgs by navArgs()
    private var currentTagihan: TagihanBelumBayar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBayarTagihanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Toolbar Navigation
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        
        // Konfirmasi Pembayaran
        binding.btnKonfirmasiBayar.setOnClickListener {
            currentTagihan?.let { tagihan ->
                val sheet = KonfirmasiBayarMultiBottomSheetFragment.newInstance(tagihan)
                sheet.show(childFragmentManager, "KonfirmasiBayarMultiBottomSheet")
            } ?: Toast.makeText(context, "Data belum dimuat", Toast.LENGTH_SHORT).show()
        }

        // Listener jika pembayaran berhasil
        setFragmentResultListener("payment_multi_successful") { _, bundle ->
            if (bundle.getBoolean("refresh")) {
                fetchDataTagihan()
            }
        }

        fetchDataTagihan()
    }

    private fun fetchDataTagihan() {
        apiService.getPelangganBelumBayarAll(args.pelangganId).enqueue(object : Callback<TagihanBelumBayarResponse> {
            override fun onResponse(call: Call<TagihanBelumBayarResponse>, response: Response<TagihanBelumBayarResponse>) {
                if (!isAdded || _binding == null) return
                
                if (response.isSuccessful && response.body()?.status == true) {
                    val list = response.body()?.data
                    if (!list.isNullOrEmpty()) {
                        displayData(list[0])
                    } else {
                        Toast.makeText(context, "Data tagihan tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil data tagihan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TagihanBelumBayarResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayData(tagihan: TagihanBelumBayar) {
        currentTagihan = tagihan
        binding.tvNamaPelanggan.text = tagihan.namaPelanggan
        binding.tvIdPelanggan.text = "ID: ${tagihan.idPelanggan}"
        binding.tvWilayah.text = tagihan.wilayah
        binding.tvPaket.text = tagihan.namaPaket
        binding.tvTelepon.text = tagihan.teleponPelanggan ?: "-"
        binding.tvMikrotikUsername.text = tagihan.mikrotikUsername ?: "-"
        
        // Menampilkan rincian tunggakan dalam format baris baru (newline)
        if (!tagihan.rincianTunggakan.isNullOrEmpty()) {
            binding.tvPeriodeTunggakan.text = tagihan.rincianTunggakan.joinToString("\n")
        } else {
            binding.tvPeriodeTunggakan.text = tagihan.periodeTunggakan
        }
        
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        binding.tvTotalNominal.text = formatter.format(tagihan.totalNominal)
        
        binding.tvTglBayarTerakhir.text = "Terakhir Bayar: ${tagihan.tglBayarTerakhir}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
