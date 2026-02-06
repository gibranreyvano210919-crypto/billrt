package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PelangganOnlineAdapter
import com.linkbit.billrt.databinding.FragmentPelangganOnlineBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganOnlineFragment : BaseFragment() {

    private var _binding: FragmentPelangganOnlineBinding? = null
    private val binding get() = _binding!!

    private val args: PelangganOnlineFragmentArgs by navArgs()
    private lateinit var adapter: PelangganOnlineAdapter
    private var allPelanggan = listOf<PelangganOnline>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganOnlineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PelangganOnlineAdapter(emptyList()) { pelanggan ->
            kickUser(pelanggan)
        }
        binding.rvPelangganOnline.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganOnline.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchData()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })

        fetchData()
    }

    private fun filter(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allPelanggan
        } else {
            allPelanggan.filter { it.name.contains(query, ignoreCase = true) }
        }
        adapter.updateData(filteredList)
    }

    private fun fetchData() {
        binding.swipeRefreshLayout.isRefreshing = true

        // Menggunakan ApiConfig agar konsisten
        ApiConfig.apiService.getPelangganStatus(routerId = args.routerId).enqueue(object : Callback<PelangganStatusResponse> {
            override fun onResponse(call: Call<PelangganStatusResponse>, response: Response<PelangganStatusResponse>) {
                if (!isAdded || _binding == null) return
                binding.swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful && response.body()?.status == true) {
                    val body = response.body()
                    val summary = body?.summary
                    val data = body?.data

                    // Update summary TextViews
                    binding.tvSummaryTotal.text = "Total\n${summary?.totalSecret ?: 0}"
                    binding.tvSummaryOnline.text = "Online\n${summary?.online ?: 0}"
                    binding.tvSummaryOffline.text = "Offline\n${summary?.offline ?: 0}"
                    binding.tvSummaryDisabled.text = "Disabled\n${summary?.disabled ?: 0}"

                    allPelanggan = data?.online ?: emptyList()
                    adapter.updateData(allPelanggan)

                    if (allPelanggan.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                    }
                } else {
                    handleFailure("Gagal memuat data: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PelangganStatusResponse>, t: Throwable) {
                handleFailure("Error: ${t.message}")
            }
        })
    }

    private fun kickUser(pelanggan: PelangganOnline) {
        ApiConfig.apiService.removeActive(args.routerId, pelanggan.name).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "User ${pelanggan.name} kicked", Toast.LENGTH_SHORT).show()
                    fetchData() // Refresh the list
                } else {
                    Toast.makeText(context, "Failed to kick user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleFailure(message: String) {
        if (!isAdded || _binding == null) return
        binding.swipeRefreshLayout.isRefreshing = false
        binding.tvEmpty.text = message
        binding.tvEmpty.visibility = View.VISIBLE
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
