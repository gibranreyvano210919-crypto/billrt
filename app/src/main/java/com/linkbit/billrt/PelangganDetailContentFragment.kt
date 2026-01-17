package com.linkbit.billrt

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.linkbit.billrt.databinding.FragmentPelangganDetailContentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class PelangganDetailContentFragment : Fragment() {

    private var _binding: FragmentPelangganDetailContentBinding? = null
    private val binding get() = _binding!!
    private val apiService: ApiService by lazy { ApiConfig.getApiService() }

    private var pelanggan: PelangganData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            pelanggan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_PELANGGAN, PelangganData::class.java)
            } else {
                it.getSerializable(ARG_PELANGGAN) as? PelangganData
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganDetailContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        pelanggan?.let {
            bindPelangganData(it)
            fetchCurrentMonthBill(it.idPelanggan)
        } 
    }

    private fun bindPelangganData(pelanggan: PelangganData) {
        // Rincian Data Pelanggan
        binding.tvDetailNamaPelanggan.text = pelanggan.namaPelanggan
        binding.tvDetailAlamat.text = pelanggan.alamat
        binding.tvDetailTelepon.text = pelanggan.telepon
        binding.tvDetailTanggalDaftar.text = pelanggan.tglDaftar
        binding.tvDetailWilayah.text = pelanggan.namaWilayah

        // Info Jaringan (Mikrotik)
        binding.tvDetailMikrotikUsername.text = pelanggan.mikrotikUsername
        binding.tvDetailStaticIp.text = pelanggan.staticIp
        binding.tvDetailStatusPppoe.text = pelanggan.statusPppoe

        // Info Teknis FTTH
        binding.tvDetailMacAddress.text = pelanggan.macAddress
        binding.tvDetailSignal.text = "${pelanggan.signalRx} / ${pelanggan.signalTx}"
        binding.tvDetailFtthStatus.text = pelanggan.ftthStatus
    }

    private fun fetchCurrentMonthBill(idPelanggan: String?) {
        val pelangganId = idPelanggan?.toIntOrNull() ?: return
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        apiService.getTagihanBulanan(pelangganId, currentYear).enqueue(object : Callback<BulanTagihanResponse> {
            override fun onResponse(call: Call<BulanTagihanResponse>, response: Response<BulanTagihanResponse>) {
                if (response.isSuccessful) {
                    val calendar = Calendar.getInstance()
                    val currentMonth = calendar.get(Calendar.MONTH) + 1
                    val currentBill = response.body()?.data?.find { it.angka == currentMonth }

                    if (currentBill != null) {
                        bindBillData(currentBill)
                    } else {
                        displayNoBillInfo()
                    }
                } else {
                    handleApiError("Gagal memuat data tagihan: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BulanTagihanResponse>, t: Throwable) {
                handleApiFailure(t, "getTagihanBulanan")
            }
        })
    }

    private fun bindBillData(tagihan: BulanTagihanData) {
        binding.tvDetailPeriodeTagihan.text = tagihan.nama

        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        binding.tvDetailTotalBayar.text = format.format(tagihan.totalBayar)

        binding.tvDetailStatusTagihan.text = tagihan.statusTagihan?.replaceFirstChar { it.uppercase() }
        if (tagihan.statusLunas) {
            binding.tvDetailStatusTagihan.setTextColor(Color.GREEN)
        } else {
            binding.tvDetailStatusTagihan.setTextColor(Color.RED)
        }
    }

    private fun displayNoBillInfo() {
        binding.tvDetailPeriodeTagihan.text = "-"
        binding.tvDetailTotalBayar.text = "-"
        binding.tvDetailStatusTagihan.text = "Tidak ada tagihan bulan ini"
        binding.tvDetailStatusTagihan.setTextColor(Color.GRAY)
    }
    
    private fun handleApiError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        Log.e("PelangganDetailContent", message)
    }

    private fun handleApiFailure(t: Throwable, funcName: String) {
        Toast.makeText(context, "Koneksi Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
        Log.e("PelangganDetailContent", "API Call Failed in $funcName", t)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PELANGGAN = "pelanggan"

        @JvmStatic
        fun newInstance(pelanggan: PelangganData) =
            PelangganDetailContentFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PELANGGAN, pelanggan)
                }
            }
    }
}
