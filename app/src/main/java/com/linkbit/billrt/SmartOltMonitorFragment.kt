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
    private var totalOltCount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSmartoltBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        setupSwipeRefresh()
        fetchOltData()
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
            fetchOltData()
        }
    }

    private fun fetchOltData() {
        binding.swipeRefreshSmartolt.isRefreshing = true
        binding.progressBarSmartolt.visibility = View.VISIBLE
        binding.rvOltList.visibility = View.GONE
        binding.tvEmptyOlt.visibility = View.VISIBLE
        binding.tvEmptyOlt.text = "Memuat data..."

        apiService.getOltData().enqueue(object : Callback<OltApiResponse> {
            override fun onResponse(call: Call<OltApiResponse>, response: Response<OltApiResponse>) {
                if (!isAdded) return
                binding.progressBarSmartolt.visibility = View.GONE
                binding.swipeRefreshSmartolt.isRefreshing = false
                if (response.isSuccessful) {
                    val oltResponse = response.body()
                    if (oltResponse != null) {
                        totalOltCount = oltResponse.detailOlt?.size ?: 0
                        allOnuList = oltResponse.detailOlt?.flatMap { olt ->
                            olt.onu_list?.map { onuData ->
                                val powerValue = onuData.values.find { it.contains("-") && it.replace(".", "").replace("-", "").all(Char::isDigit) }

                                SmartOnuItem(
                                    oltName = olt.olt_name,
                                    onuIndex = onuData["col_0"],
                                    name = onuData["col_1"],
                                    sn = onuData["col_2"],
                                    macAddress = onuData["col_2"], // sn is macAddress
                                    status = onuData["col_3"],
                                    power = powerValue
                                )
                            } ?: emptyList()
                        } ?: emptyList()

                        filterOnuList(binding.searchView.query.toString())

                    } else {
                        binding.tvEmptyOlt.text = "Gagal memuat: Respons tidak valid."
                        binding.tvEmptyOlt.visibility = View.VISIBLE
                    }
                } else {
                    binding.tvEmptyOlt.text = "Gagal memuat: Error ${response.code()}"
                    binding.tvEmptyOlt.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<OltApiResponse>, t: Throwable) {
                if (!isAdded) return
                binding.progressBarSmartolt.visibility = View.GONE
                binding.swipeRefreshSmartolt.isRefreshing = false
                binding.tvEmptyOlt.text = "Gagal memuat: ${t.message}"
                binding.tvEmptyOlt.visibility = View.VISIBLE
            }
        })
    }

    private fun normalizeMac(mac: String?): String {
        return mac?.replace(Regex("[^A-Za-z0-9]"), "")?.toLowerCase(Locale.ROOT) ?: ""
    }

    private fun updateAdapterAndViews(filteredList: List<SmartOnuItem>, query: String?) {
        onuAdapter.updateData(filteredList)

        if (filteredList.isEmpty()) {
            val emptyText = if (query.isNullOrBlank()) "Tidak ada ONU yang tersedia." else "Tidak ada hasil untuk: $query"
            binding.tvEmptyOlt.text = emptyText
            binding.tvEmptyOlt.visibility = View.VISIBLE
            binding.rvOltList.visibility = View.GONE
        } else {
            val statusText = if (query.isNullOrBlank()) {
                "Total OLT: $totalOltCount, Total ONU: ${allOnuList.size}"
            } else {
                "Menampilkan ${filteredList.size} dari ${allOnuList.size} ONU (Total OLT: $totalOltCount)"
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
        val directResults = allOnuList.filter {
            it.oltName.contains(trimmedQuery, ignoreCase = true) ||
                    (it.macAddress != null && normalizeMac(it.macAddress).contains(normalizedQuery))
        }

        // Update UI immediately with direct search results
        updateAdapterAndViews(directResults, trimmedQuery)

        // Perform API search for customer name to get MAC addresses
        apiService.getDataPelanggan(search = trimmedQuery).enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (!isAdded || !response.isSuccessful) return

                // Make sure the query hasn't changed while the API call was in flight
                val currentQuery = binding.searchView.query.toString().trim()
                if (currentQuery != trimmedQuery) {
                    return
                }

                val macsFromApi = response.body()?.data
                    ?.mapNotNull { it.macAddress }
                    ?.map { normalizeMac(it) }
                    ?.filter { it.isNotBlank() }
                    ?.toSet() ?: emptySet()

                if (macsFromApi.isNotEmpty()) {
                    val apiResults = allOnuList.filter { onu ->
                        onu.macAddress != null && normalizeMac(onu.macAddress) in macsFromApi
                    }

                    val combinedResults = (directResults + apiResults).distinctBy { it.sn }
                    updateAdapterAndViews(combinedResults, trimmedQuery)
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                // If API fails, we just stick with the direct results that are already displayed.
                // You could add logging here.
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        allOnuList = emptyList()
        totalOltCount = 0
        _binding = null
    }
}