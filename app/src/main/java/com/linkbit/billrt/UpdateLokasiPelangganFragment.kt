package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentUpdateLokasiPelangganBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateLokasiPelangganFragment : BaseFragment() {

    private var _binding: FragmentUpdateLokasiPelangganBinding? = null
    private val binding get() = _binding!!
    private lateinit var lokasiAdapter: UpdateLokasiAdapter
    private var originalList: List<PelangganData> = emptyList()
    private var wilayahList: List<Wilayah> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateLokasiPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        fetchWilayahAndPelanggan()
    }

    private fun setupRecyclerView() {
        lokasiAdapter = UpdateLokasiAdapter(emptyList()) { pelanggan ->
            if (!pelanggan.idPelanggan.isNullOrEmpty() && !pelanggan.nama.isNullOrEmpty()) {
                val action = UpdateLokasiPelangganFragmentDirections.actionUpdateLokasiPelangganFragmentToUpdateFotoLokasiFragment(
                    pelanggan.idPelanggan,
                    pelanggan.nama
                )
                findNavController().navigate(action)
            } else {
                Toast.makeText(context, "ID atau Nama Pelanggan tidak valid.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvPelangganLokasi.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganLokasi.adapter = lokasiAdapter
    }

    private fun setupSearchView() {
        binding.searchViewLokasi.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterPelangganList()
                return true
            }
        })
    }

    private fun fetchWilayahAndPelanggan() {
        apiService.getWilayah().enqueue(object : Callback<WilayahResponse> {
            override fun onResponse(call: Call<WilayahResponse>, response: Response<WilayahResponse>) {
                if (response.isSuccessful) {
                    wilayahList = response.body()?.data ?: emptyList()
                    val wilayahNames = mutableListOf("Semua Wilayah")
                    wilayahNames.addAll(wilayahList.map { it.nama_wilayah })
                    
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wilayahNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerWilayahLokasi.adapter = adapter

                    binding.spinnerWilayahLokasi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                            filterPelangganList()
                        }
                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
                    
                    fetchPelangganData()
                }
            }
            override fun onFailure(call: Call<WilayahResponse>, t: Throwable) {}
        })
    }

    private fun fetchPelangganData() {
        apiService.getDataPelanggan().enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (response.isSuccessful) {
                    originalList = response.body()?.data?.filter { it.latitude == null || it.longitude == null } ?: emptyList()
                    filterPelangganList()
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {}
        })
    }

    private fun filterPelangganList() {
        val selectedWilayahPosition = binding.spinnerWilayahLokasi.selectedItemPosition
        val selectedWilayah = if (selectedWilayahPosition > 0) wilayahList.getOrNull(selectedWilayahPosition - 1) else null
        val searchQuery = binding.searchViewLokasi.query.toString().lowercase()

        var filteredList = originalList

        if (selectedWilayah != null) {
            filteredList = filteredList.filter { it.namaWilayah == selectedWilayah.nama_wilayah }
        }

        if (searchQuery.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.nama.lowercase().contains(searchQuery) ||
                it.alamat?.lowercase()?.contains(searchQuery) == true ||
                it.idPelanggan.lowercase().contains(searchQuery)
            }
        }

        lokasiAdapter.updateList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
