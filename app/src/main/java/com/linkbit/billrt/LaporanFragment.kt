package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentLaporanBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class LaporanFragment : BaseFragment() {

    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!

    private lateinit var laporanWilayahAdapter: LaporanWilayahAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchLaporanData()
    }

    private fun setupRecyclerView() {
        laporanWilayahAdapter = LaporanWilayahAdapter(emptyList())
        binding.rvLaporanWilayah.layoutManager = LinearLayoutManager(context)
        binding.rvLaporanWilayah.adapter = laporanWilayahAdapter
    }

    private fun fetchLaporanData() {
        binding.progressBar.visibility = View.VISIBLE

        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        apiService.getLaporanWilayah(month, year).enqueue(object : Callback<List<DetailWilayah>> {
            override fun onResponse(call: Call<List<DetailWilayah>>, response: Response<List<DetailWilayah>>) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    laporanWilayahAdapter.updateData(data)
                } else {
                    Toast.makeText(context, "Gagal mengambil data laporan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<DetailWilayah>>, t: Throwable) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}