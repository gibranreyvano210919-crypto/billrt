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
                                // Dynamically find the power value by looking for a value with a '-'
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

    private fun filterOnuList(query: String?) {
        if (query.isNullOrBlank()) {
            onuAdapter.updateData(allOnuList)
            if (allOnuList.isEmpty()) {
                binding.tvEmptyOlt.text = "Tidak ada ONU yang tersedia."
                binding.tvEmptyOlt.visibility = View.VISIBLE
                binding.rvOltList.visibility = View.GONE
            } else {
                binding.tvEmptyOlt.text = "Total OLT: $totalOltCount, Total ONU: ${allOnuList.size}"
                binding.tvEmptyOlt.visibility = View.VISIBLE
                binding.rvOltList.visibility = View.VISIBLE
            }
            return
        }

        val macsFromMatchingNames = allOnuList
            .filter { it.name?.contains(query, ignoreCase = true) == true }
            .mapNotNull { it.macAddress }
            .toSet()

        val filteredList = allOnuList.filter {
            it.oltName.contains(query, ignoreCase = true) ||
            it.name?.contains(query, ignoreCase = true) == true ||
            it.macAddress?.contains(query, ignoreCase = true) == true ||
            (it.macAddress != null && macsFromMatchingNames.contains(it.macAddress))
        }
        
        onuAdapter.updateData(filteredList)

        if (filteredList.isEmpty()) {
            binding.tvEmptyOlt.text = "Tidak ada hasil untuk: $query"
            binding.tvEmptyOlt.visibility = View.VISIBLE
            binding.rvOltList.visibility = View.GONE
        } else {
            binding.tvEmptyOlt.text = "Menampilkan ${filteredList.size} dari ${allOnuList.size} ONU (Total OLT: $totalOltCount)"
            binding.tvEmptyOlt.visibility = View.VISIBLE
            binding.rvOltList.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
