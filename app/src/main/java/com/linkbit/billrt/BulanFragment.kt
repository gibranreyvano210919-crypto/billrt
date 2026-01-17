package com.linkbit.billrt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentBulanBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class BulanFragment : Fragment() {

    private var _binding: FragmentBulanBinding? = null
    private val binding get() = _binding!!
    private val apiService: ApiService by lazy { ApiConfig.getApiService() }

    private var pelanggan: PelangganData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pelanggan = it.getSerializable(ARG_PELANGGAN) as? PelangganData
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBulanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvBulan.layoutManager = LinearLayoutManager(context)
        setupYearSpinner()
    }

    private fun setupYearSpinner() {
        val spinner = binding.spinnerYearBulan
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2020..currentYear).toList().map { it.toString() }
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = yearAdapter
        spinner.setSelection(years.indexOf(currentYear.toString()))

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fetchBulanTagihan(years[position].toInt())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchBulanTagihan(tahun: Int) {
        val pelangganId = pelanggan?.idPelanggan?.toIntOrNull()
        if (pelangganId == null) {
            Toast.makeText(context, "ID Pelanggan tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        apiService.getTagihanBulanan(pelangganId, tahun).enqueue(object : Callback<BulanTagihanResponse> {
            override fun onResponse(call: Call<BulanTagihanResponse>, response: Response<BulanTagihanResponse>) {
                if (response.isSuccessful) {
                    val bulanList = response.body()?.data ?: emptyList()
                    binding.rvBulan.adapter = BulanAdapter(bulanList) { bulanTagihan ->
                        // Konversi BulanTagihanData ke TagihanData
                        val tagihanData = TagihanData(
                            idPelanggan = pelanggan?.idPelanggan,
                            bulanTagihan = bulanTagihan.angka.toString(),
                            tahunTagihan = tahun.toString(),
                            totalBayar = bulanTagihan.totalBayar.toString(),
                            statusTagihan = bulanTagihan.statusTagihan,
                            tglBayar = if (bulanTagihan.statusLunas) bulanTagihan.tanggalBayar else null
                        )
                        pelanggan?.let { openPembayaranDetail(tagihanData, it) }
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil data tagihan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BulanTagihanResponse>, t: Throwable) {
                Log.e("BulanFragment", "API Call Failed", t)
                Toast.makeText(context, "Koneksi Gagal", Toast.LENGTH_LONG).show()
            }
        })
    }
    
    private fun openPembayaranDetail(tagihan: TagihanData, pelanggan: PelangganData) {
        val fragment = PembayaranDetailFragment.newInstance(tagihan, pelanggan)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PELANGGAN = "pelanggan"

        @JvmStatic
        fun newInstance(pelanggan: PelangganData) =
            BulanFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PELANGGAN, pelanggan)
                }
            }
    }
}
