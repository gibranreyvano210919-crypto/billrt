package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentPelangganAktifBinding
import com.linkbit.billrt.model.StandardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganAktifFragment : BaseFragment() {

    private var _binding: FragmentPelangganAktifBinding? = null
    private val binding get() = _binding!!

    private lateinit var pelangganAdapter: PelangganAdapter
    private var pelangganList: List<PelangganData> = emptyList()
    private val args: PelangganAktifFragmentArgs by navArgs()

    private var selectedWilayahId: Int? = null
    private var summaryWilayahList: List<SummaryWilayahItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener("edit_result") { _, bundle ->
            val updated = bundle.getBoolean("updated")
            if (updated) {
                fetchPelangganAktif()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPelangganAktifBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets(binding.appBarLayout)
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        fetchPelangganAktif()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Inflate menu baru yang ada search dan filter
        binding.toolbar.inflateMenu(R.menu.menu_pelanggan_aktif)
        
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.apply {
            queryHint = "Cari pelanggan aktif..."
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false

                override fun onQueryTextChange(newText: String?): Boolean {
                    filter(newText)
                    return true
                }
            })
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_filter -> {
                    showWilayahFilterBottomSheet()
                    true
                }
                else -> false
            }
        }
    }

    private fun showWilayahFilterBottomSheet() {
        if (summaryWilayahList.isEmpty()) {
            Toast.makeText(context, "Data wilayah belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val bottomSheet = PelangganAktifFilterBottomSheet.newInstance(
            summaryWilayahList,
            selectedWilayahId
        )
        
        bottomSheet.setOnWilayahSelectedListener { id, name ->
            selectedWilayahId = id
            fetchPelangganAktif()
            val filterName = name ?: "Semua Wilayah"
            Toast.makeText(context, "Filter: $filterName", Toast.LENGTH_SHORT).show()
        }
        
        bottomSheet.show(childFragmentManager, "PelangganAktifFilterBottomSheet")
    }

    private fun setupRecyclerView() {
        pelangganAdapter = PelangganAdapter(emptyList(),
            onDetailClick = { pelanggan ->
                val action = PelangganAktifFragmentDirections.actionPelangganAktifFragmentToDetailPelangganFragment(pelanggan.idPelanggan)
                findNavController().navigate(action)
            },
            onItemLongClick = { pelanggan ->
                showBottomSheetMenu(pelanggan)
            }
        )
        binding.rvPelangganAktif.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganAktif.adapter = pelangganAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchPelangganAktif()
        }
    }

    private fun showBottomSheetMenu(pelanggan: PelangganData) {
        val bottomSheet = PelangganBottomSheetFragment.newInstance(pelanggan.idPelanggan, pelanggan.nama).apply {
            setOnEditClickListener { pelangganId ->
                val action = PelangganAktifFragmentDirections.actionPelangganAktifFragmentToEditPelangganFragment(pelangganId)
                findNavController().navigate(action)
            }
            setOnStatusUpdateSuccessfulListener { 
                fetchPelangganAktif()
            }
        }
        bottomSheet.show(childFragmentManager, "PelangganBottomSheet")
    }

    private fun fetchPelangganAktif() {
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }
        apiService.getDataPelanggan(status = "aktif", idWilayah = selectedWilayahId).enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    val body = response.body()
                    pelangganList = body?.data ?: emptyList()
                    summaryWilayahList = body?.summaryWilayah ?: emptyList()
                    pelangganAdapter.updateData(pelangganList)
                } else {
                    Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filter(query: String?) {
        val filtered = if (query.isNullOrEmpty()) {
            pelangganList
        } else {
            pelangganList.filter {
                (it.nama?.contains(query, ignoreCase = true) ?: false) || 
                (it.idPelanggan?.contains(query, ignoreCase = true) ?: false) ||
                (it.alamat?.contains(query, ignoreCase = true) ?: false)
            }
        }
        pelangganAdapter.updateData(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
