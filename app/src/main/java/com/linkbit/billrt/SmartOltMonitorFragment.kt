package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentSmartoltBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class SmartOltMonitorFragment : BaseFragment() {

    private var _binding: FragmentSmartoltBinding? = null
    private val binding get() = _binding!!

    private lateinit var onuAdapter: OnuAdapter
    private var allOnuList: List<SmartOnuItem> = emptyList()
    
    private var lamaProses: String? = null
    private var totalOnline: Int = 0
    private var totalOffline: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSmartoltBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Sinkronisasi Insets agar toolbar tidak menabrak status bar
        applyWindowInsets(binding.appBarLayout)
        
        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        setupSwipeRefresh()
        
        fetchHighSpeedOltData()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarSmartolt)
        binding.toolbarSmartolt.setupWithNavController(findNavController())
    }

    private fun setupRecyclerView() {
        onuAdapter = OnuAdapter(emptyList())
        binding.rvOltList.layoutManager = LinearLayoutManager(context)
        binding.rvOltList.adapter = onuAdapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterOnuList(newText)
                return true
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshSmartolt.setOnRefreshListener {
            fetchHighSpeedOltData()
        }
    }

    private fun fetchHighSpeedOltData() {
        binding.swipeRefreshSmartolt.isRefreshing = true
        binding.progressBarSmartolt.visibility = View.VISIBLE
        binding.tvEmptyOlt.visibility = View.VISIBLE
        binding.tvEmptyOlt.text = "Memuat data OLT (High Speed)..."

        apiService.getHighSpeedOltData().enqueue(object : Callback<HighSpeedOltResponse> {
            override fun onResponse(call: Call<HighSpeedOltResponse>, response: Response<HighSpeedOltResponse>) {
                if (!isAdded) return
                binding.progressBarSmartolt.visibility = View.GONE
                binding.swipeRefreshSmartolt.isRefreshing = false
                
                if (response.isSuccessful) {
                    val highSpeedResponse = response.body()
                    if (highSpeedResponse != null && highSpeedResponse.status) {
                        lamaProses = highSpeedResponse.lamaProses
                        totalOnline = highSpeedResponse.totalOnline
                        totalOffline = highSpeedResponse.totalOffline
                        
                        allOnuList = highSpeedResponse.results.flatMap { result ->
                            result.onus.map { onu ->
                                SmartOnuItem(
                                    oltName = result.oltHost,
                                    onuIndex = null,
                                    name = null,
                                    sn = onu.macAddress,
                                    macAddress = onu.macAddress,
                                    status = onu.status, 
                                    power = onu.signalRx,
                                    customerName = onu.namaPelanggan,
                                    idPelanggan = null
                                )
                            }
                        }

                        filterOnuList(binding.searchView.query.toString())

                    } else {
                        binding.tvEmptyOlt.text = "Gagal memuat: ${highSpeedResponse?.message ?: "Unknown error"}"
                    }
                } else {
                    binding.tvEmptyOlt.text = "Gagal memuat: Error ${response.code()}"
                }
            }

            override fun onFailure(call: Call<HighSpeedOltResponse>, t: Throwable) {
                if (!isAdded) return
                binding.progressBarSmartolt.visibility = View.GONE
                binding.swipeRefreshSmartolt.isRefreshing = false
                binding.tvEmptyOlt.text = "Gagal memuat: ${t.message}"
            }
        })
    }

    private fun normalizeMac(mac: String?): String {
        return mac?.replace(Regex("[^A-Za-z0-9]"), "")?.lowercase(Locale.ROOT) ?: ""
    }

    private fun updateAdapterAndViews(filteredList: List<SmartOnuItem>, query: String?) {
        onuAdapter.updateData(filteredList)

        if (filteredList.isEmpty()) {
            val emptyText = if (query.isNullOrBlank()) "Tidak ada ONU yang ditemukan." else "Tidak ada hasil untuk: $query"
            binding.tvEmptyOlt.text = emptyText
            binding.tvEmptyOlt.visibility = View.VISIBLE
            binding.rvOltList.visibility = View.GONE
        } else {
            val statusText = if (query.isNullOrBlank()) {
                "Online: $totalOnline | Offline: $totalOffline | Proses: $lamaProses"
            } else {
                "Menampilkan ${filteredList.size} ONU"
            }
            binding.tvEmptyOlt.text = statusText
            binding.tvEmptyOlt.visibility = View.VISIBLE
            binding.rvOltList.visibility = View.VISIBLE
        }
    }

    private fun filterOnuList(query: String?) {
        val trimmedQuery = query?.trim()

        if (trimmedQuery.isNullOrBlank()) {
            updateAdapterAndViews(allOnuList, null)
            return
        }

        val normalizedQuery = normalizeMac(trimmedQuery)
        
        val filteredResults = allOnuList.filter { onu ->
            val normOnuMac = normalizeMac(onu.macAddress)
            
            val matchesDirectly = 
                onu.oltName.contains(trimmedQuery, ignoreCase = true) ||
                (normOnuMac.isNotEmpty() && normOnuMac.contains(normalizedQuery)) ||
                (onu.customerName?.contains(trimmedQuery, ignoreCase = true) == true)

            matchesDirectly
        }

        updateAdapterAndViews(filteredResults, trimmedQuery)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        allOnuList = emptyList()
        lamaProses = null
        totalOnline = 0
        totalOffline = 0
        _binding = null
    }
}
