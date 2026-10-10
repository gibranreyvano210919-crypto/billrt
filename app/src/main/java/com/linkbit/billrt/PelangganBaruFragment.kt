package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentPelangganBaruBinding
import com.linkbit.billrt.network.PelangganBaru
import com.linkbit.billrt.network.PelangganBaruResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganBaruFragment : BaseFragment() {

    private var _binding: FragmentPelangganBaruBinding? = null
    private val binding get() = _binding!!

    private lateinit var pelangganBaruAdapter: PelangganBaruAdapter
    private var pelangganBaruList: List<PelangganBaru> = emptyList()
    private val args: PelangganBaruFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPelangganBaruBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sinkronisasi Insets agar toolbar tidak menabrak status bar
        applyWindowInsets(binding.appBarLayout)
        
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        fetchPelangganBaru()
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Pelanggan Baru"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Inflate menu search ke toolbar
        binding.toolbar.inflateMenu(R.menu.menu_search)
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.apply {
            queryHint = "Cari pelanggan baru..."
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    filter(newText)
                    return true
                }
            })
        }
    }

    private fun setupRecyclerView() {
        pelangganBaruAdapter = PelangganBaruAdapter(emptyList())
        binding.rvPelangganBaru.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganBaru.adapter = pelangganBaruAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchPelangganBaru()
        }
    }

    private fun fetchPelangganBaru() {
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }
        apiService.getPelangganBaru(bulan = args.bulan, tahun = args.tahun).enqueue(object : Callback<PelangganBaruResponse> {
            override fun onResponse(call: Call<PelangganBaruResponse>, response: Response<PelangganBaruResponse>) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    val body = response.body()
                    pelangganBaruList = body?.data ?: emptyList()
                    pelangganBaruAdapter.updateData(pelangganBaruList)
                    
                    // Update toolbar title with total count from response
                    binding.toolbar.title = "Pelanggan Baru (${body?.total ?: 0})"
                } else {
                    Toast.makeText(context, "Gagal memuat data pelanggan baru", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PelangganBaruResponse>, t: Throwable) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filter(query: String?) {
        val filtered = if (query.isNullOrEmpty()) {
            pelangganBaruList
        } else {
            pelangganBaruList.filter {
                it.namaPelanggan?.contains(query, ignoreCase = true) == true || 
                it.idPelanggan.contains(query, ignoreCase = true)
            }
        }
        pelangganBaruAdapter.updateData(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
