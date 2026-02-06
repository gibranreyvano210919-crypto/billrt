package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentTotalPelangganBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TotalPelangganFragment : BaseFragment() {

    private var _binding: FragmentTotalPelangganBinding? = null
    private val binding get() = _binding!!

    private lateinit var pelangganAdapter: PelangganAdapter
    private var wilayahList = listOf<Wilayah>()
    private var selectedWilayahId: Int? = null
    private var searchQuery: String = ""
    private var allPelanggan: List<PelangganData> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTotalPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupWilayahSpinner()
        fetchWilayah()
        fetchPelanggan() // Initial fetch

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchPelanggan()
        }
    }

    private fun setupRecyclerView() {
        pelangganAdapter = PelangganAdapter(emptyList()) // Click listener removed
        binding.rvTotalPelanggan.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pelangganAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText.orEmpty()
                filterPelanggan()
                return true
            }
        })
    }

    private fun setupWilayahSpinner() {
        binding.spinnerWilayah.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedWilayahId = if (position > 0) {
                    wilayahList[position - 1].id_wilayah
                } else {
                    null
                }
                filterPelanggan()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchWilayah() {
        apiService.getWilayah().enqueue(object : Callback<WilayahResponse> {
            override fun onResponse(call: Call<WilayahResponse>, response: Response<WilayahResponse>) {
                if (response.isSuccessful) {
                    wilayahList = response.body()?.data ?: emptyList()
                    val wilayahNames = mutableListOf("Semua Wilayah").apply {
                        addAll(wilayahList.map { it.nama_wilayah })
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, wilayahNames)
                    binding.spinnerWilayah.adapter = adapter
                }
            }
            override fun onFailure(call: Call<WilayahResponse>, t: Throwable) {}
        })
    }

    private fun fetchPelanggan() {
        binding.progressBar.visibility = View.VISIBLE
        binding.swipeRefreshLayout.isRefreshing = true

        apiService.getDataPelanggan().enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    allPelanggan = response.body()?.data ?: emptyList()
                    filterPelanggan()
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    private fun filterPelanggan() {
        var filteredList = allPelanggan
        if (selectedWilayahId != null) {
            filteredList = filteredList.filter { it.namaWilayah == wilayahList.find { w -> w.id_wilayah == selectedWilayahId }?.nama_wilayah }
        }

        if (searchQuery.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.nama.contains(searchQuery, true) ||
                it.alamat?.contains(searchQuery, true) == true ||
                it.idPelanggan.contains(searchQuery, true)
            }
        }
        pelangganAdapter.updateData(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
