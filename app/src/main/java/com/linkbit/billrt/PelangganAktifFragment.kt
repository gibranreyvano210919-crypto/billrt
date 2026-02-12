package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentPelangganAktifBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganAktifFragment : BaseFragment() {

    private var _binding: FragmentPelangganAktifBinding? = null
    private val binding get() = _binding!!

    private lateinit var pelangganAdapter: PelangganAdapter
    private var pelangganList: List<PelangganData> = emptyList()
    private val args: PelangganAktifFragmentArgs by navArgs()

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

        setupRecyclerView()
        setupSearchView()
        setupSwipeRefresh()
        fetchPelangganAktif()
    }

    private fun setupRecyclerView() {
        pelangganAdapter = PelangganAdapter(emptyList(),
            onDetailClick = { pelanggan ->
                val action = PelangganAktifFragmentDirections.actionPelangganAktifFragmentToPelangganAktifDetailFragment(pelanggan.idPelanggan)
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
            setOnIsolirClickListener { pelangganId ->
                val pelangganToUpdate = pelangganList.find { it.idPelanggan == pelangganId }
                pelangganToUpdate?.let { 
                    updateStatus(it, "isolir") 
                }
            }
        }
        bottomSheet.show(childFragmentManager, "PelangganBottomSheet")
    }

    private fun updateStatus(pelanggan: PelangganData, newStatus: String) {
        val request = UpdateStatusRequest(idPelanggan = pelanggan.idPelanggan, statusAktif = newStatus)
        apiService.updateStatusPelanggan(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Status berhasil diubah ke $newStatus", Toast.LENGTH_SHORT).show()
                    fetchPelangganAktif() // Refresh the list
                } else {
                    Toast.makeText(context, "Gagal mengubah status", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSearchView() {
        binding.searchViewPelangganAktif.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })
    }

    private fun fetchPelangganAktif() {
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }
        apiService.getDataPelanggan(status = "aktif").enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    pelangganList = response.body()?.data ?: emptyList()
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
                it.nama.contains(query, ignoreCase = true) || it.idPelanggan.contains(query, ignoreCase = true)
            }
        }
        pelangganAdapter.updateData(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
