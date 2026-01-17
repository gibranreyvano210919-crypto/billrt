package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.linkbit.billrt.databinding.FragmentPembayaranDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PembayaranDetailFragment : Fragment() {

    private var _binding: FragmentPembayaranDetailBinding? = null
    private val binding get() = _binding!!
    private val apiService: ApiService by lazy { ApiConfig.getApiService() }

    private var tagihan: TagihanData? = null
    private var pelanggan: PelangganData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tagihan = it.getSerializable(ARG_TAGIHAN) as? TagihanData
            pelanggan = it.getSerializable(ARG_PELANGGAN) as? PelangganData
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
        tagihan?.let { bindData(it) }
        setupListeners()
    }

    private fun bindData(tagihan: TagihanData) {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        binding.tvPaymentUserInfo.text = "${pelanggan?.namaPelanggan} - ${tagihan.idPelanggan}"
        binding.tvPaymentPaketName.text = pelanggan?.namaPaket
        binding.tvPaymentPaketAmount.text = format.format(pelanggan?.harga?.toDoubleOrNull() ?: 0.0)
        binding.tvPaymentPpnAmount.text = format.format(0) // Assuming PPN is 0

        val monthNames = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val monthIndex = tagihan.bulanTagihan?.toIntOrNull()
        val period = if (monthIndex != null && monthIndex in 1..12) {
            "${monthNames[monthIndex - 1]} ${tagihan.tahunTagihan}"
        } else {
            "Periode tidak valid"
        }
        binding.tvPaymentMonthPeriod.text = period

        if (tagihan.statusTagihan != "lunas") {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.etTglBayar.setText(sdf.format(Calendar.getInstance().time))
        } else {
            binding.etTglBayar.setText(tagihan.tglBayar ?: "-")
        }

        binding.tvPaymentTotalAmount.text = format.format(tagihan.totalBayar?.toDoubleOrNull() ?: 0.0)
    }

    private fun setupListeners() {
        binding.etTglBayar.setOnClickListener { showDatePicker() }
        binding.btnPayNow.setOnClickListener { processPayment() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            binding.etTglBayar.setText(selectedDate)
        }, year, month, day).show()
    }

    private fun processPayment() {
        val totalBayarFloat = pelanggan?.harga?.toFloatOrNull() ?: 0f

        val request = InputTagihanRequest(
            id_pelanggan = pelanggan?.idPelanggan?.toIntOrNull() ?: 0,
            bulan = tagihan?.bulanTagihan?.toIntOrNull() ?: 0,
            tahun = tagihan?.tahunTagihan?.toIntOrNull() ?: 0,
            total_bayar = totalBayarFloat,
            status_tagihan = "lunas",
            id_paket = pelanggan?.idPaket?.toIntOrNull() ?: 0,
            keterangan = "Pembayaran via Android",
            metode_bayar = "CASH",
            tgl_bayar = binding.etTglBayar.text.toString()
        )

        apiService.createTagihan(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == true) {
                        Toast.makeText(context, body.message, Toast.LENGTH_SHORT).show()
                        // Optionally, pop back stack or refresh UI
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(context, "Gagal: ${body?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Gagal menyimpan pembayaran: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Log.e("PembayaranDetail", "API Call Failed", t)
                Toast.makeText(context, "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TAGIHAN = "tagihan"
        private const val ARG_PELANGGAN = "pelanggan"

        @JvmStatic
        fun newInstance(tagihan: TagihanData, pelanggan: PelangganData) =
            PembayaranDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TAGIHAN, tagihan)
                    putSerializable(ARG_PELANGGAN, pelanggan)
                }
            }
    }
}
