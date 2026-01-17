package com.linkbit.billrt

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentTotalPelangganBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TotalPelangganFragment : Fragment() {

    private var _binding: FragmentTotalPelangganBinding? = null
    private val binding get() = _binding!!
    private val apiService: ApiService by lazy { ApiConfig.getApiService() }

    private var bulan: Int = 0
    private var tahun: Int = 0
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var wilayahList: List<Wilayah> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bulan = it.getInt(ARG_BULAN)
            tahun = it.getInt(ARG_TAHUN)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTotalPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvTotalPelanggan.layoutManager = LinearLayoutManager(context)
        setupSearchView()
        setupWilayahSpinner()
        setupSwipeRefresh()
        fetchWilayahData()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchData(binding.searchView.query.toString(), true)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false 
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    fetchData(newText)
                }
                searchHandler.postDelayed(searchRunnable!!, 300)
                return true
            }
        })
    }

    private fun setupWilayahSpinner() {
        binding.spinnerWilayah.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fetchData(binding.searchView.query.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchWilayahData() {
        apiService.getWilayahRekap().enqueue(object : Callback<WilayahResponse> {
            override fun onResponse(call: Call<WilayahResponse>, response: Response<WilayahResponse>) {
                if (_binding == null) return
                if (response.isSuccessful) {
                    wilayahList = response.body()?.data ?: emptyList()
                    val spinnerData = mutableListOf("Semua Wilayah (Total)")
                    spinnerData.addAll(wilayahList.map { "${it.namaWilayah} (${it.jumlahPelanggan})" })
                    
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerData)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerWilayah.adapter = adapter
                } else {
                    Toast.makeText(context, "Gagal memuat data wilayah: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WilayahResponse>, t: Throwable) {
                if (_binding == null) return
                Log.e("TotalPelangganFragment", "Gagal ambil data wilayah", t)
                Toast.makeText(context, "Koneksi Gagal (Wilayah): ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun fetchData(searchQuery: String? = null, isRefreshing: Boolean = false) {
        if (!isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }

        val selectedWilayahPosition = binding.spinnerWilayah.selectedItemPosition
        val idWilayah = if (selectedWilayahPosition > 0) {
            wilayahList[selectedWilayahPosition - 1].idWilayah
        } else {
            null
        }

        apiService.getPelanggan(null, null, "semua", searchQuery, idWilayah).enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (_binding == null) return
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    val pelangganList = response.body()?.data ?: emptyList()
                    binding.rvTotalPelanggan.adapter = PelangganAdapter(pelangganList) { pelanggan ->
                        openDetailPelangganFragment(pelanggan)
                    }
                    if (pelangganList.isEmpty()) {
                        Toast.makeText(context, "Tidak ada pelanggan ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                Log.e("TotalPelangganFragment", "API Call Failed", t)
                Toast.makeText(context, "Koneksi Gagal", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun openDetailPelangganFragment(pelanggan: PelangganData) {
        val fragment = DetailPelangganFragment.newInstance(pelanggan)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        _binding = null
    }

    companion object {
        private const val ARG_BULAN = "bulan"
        private const val ARG_TAHUN = "tahun"

        @JvmStatic
        fun newInstance(bulan: Int, tahun: Int) =
            TotalPelangganFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_BULAN, bulan)
                    putInt(ARG_TAHUN, tahun)
                }
            }
    }
}
