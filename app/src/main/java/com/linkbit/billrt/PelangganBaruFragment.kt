package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentPelangganBaruBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PelangganBaruFragment : BaseFragment() {

    private var _binding: FragmentPelangganBaruBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganBaruBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvPelangganBaru.layoutManager = LinearLayoutManager(context)
        fetchData()
    }

    private fun fetchData() {
        binding.progressBar.visibility = View.VISIBLE
        apiService.getDataPelanggan().enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val pelangganList = response.body()?.data ?: emptyList()
                    val filteredList = pelangganList.filter { isNewPelanggan(it.tglDaftar) }
                    binding.rvPelangganBaru.adapter = PelangganAdapter(filteredList)
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun isNewPelanggan(tglDaftar: String?): Boolean {
        if (tglDaftar.isNullOrEmpty()) return false
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val dateDaftar = sdf.parse(tglDaftar)
            val sevenDaysAgo = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -7)
            }.time
            return dateDaftar?.after(sevenDaysAgo) ?: false
        } catch (e: Exception) {
            return false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
