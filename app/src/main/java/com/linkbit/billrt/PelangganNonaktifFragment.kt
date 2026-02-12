package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentPelangganNonaktifBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganNonaktifFragment : BaseFragment() {

    private var _binding: FragmentPelangganNonaktifBinding? = null
    private val binding get() = _binding!!

    private lateinit var pelangganAdapter: PelangganNonaktifAdapter
    private var pelangganList: List<PelangganNonaktif> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPelangganNonaktifBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        fetchData(null, null) // Load all non-active customers initially
    }

    private fun setupRecyclerView() {
        pelangganAdapter = PelangganNonaktifAdapter(
            emptyList(),
            onDetailClick = { pelanggan ->
                // Navigate to detail, you might need a new detail fragment for this data model
                // For now, I'll just show a Toast
                Toast.makeText(context, "Clicked on ${pelanggan.namaPelanggan}", Toast.LENGTH_SHORT).show()
            },
            onMenuClick = null
        )
        binding.rvPelangganNonaktif.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganNonaktif.adapter = pelangganAdapter
    }

    private fun setupSearchView() {
        binding.searchViewPelangganNonaktif.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })
    }

    private fun fetchData(bulan: Int?, tahun: Int?) {
        binding.progressBar.visibility = View.VISIBLE
        apiService.getPelangganNonaktif(bulan, tahun).enqueue(object : Callback<PelangganNonaktifResponse> {
            override fun onResponse(call: Call<PelangganNonaktifResponse>, response: Response<PelangganNonaktifResponse>) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    pelangganList = responseBody?.data ?: emptyList()
                    pelangganAdapter.updateData(pelangganList)
                    binding.tvTotalPelanggan.text = "Total: ${responseBody?.total ?: 0}"
                } else {
                    Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PelangganNonaktifResponse>, t: Throwable) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filter(query: String?) {
        val filtered = if (query.isNullOrEmpty()) {
            pelangganList
        } else {
            pelangganList.filter {
                it.namaPelanggan?.contains(query, ignoreCase = true) == true || 
                it.idPelanggan.contains(query, ignoreCase = true)
            }
        }
        pelangganAdapter.updateData(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
