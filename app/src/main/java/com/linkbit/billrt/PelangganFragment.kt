package com.linkbit.billrt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentPelangganBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganFragment : BaseFragment() {

    private var _binding: FragmentPelangganBinding? = null
    private val binding get() = _binding!!

    private lateinit var pelangganAdapter: PelangganAdapter
    private var allPelanggan: List<PelangganData> = emptyList()
    private var showActive: Boolean = true // Default to show active customers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            showActive = it.getBoolean("show_active", true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        fetchPelanggan()

        // Dynamically change the title and FAB visibility
        if (showActive) {
            binding.fabTambahPelanggan.visibility = View.VISIBLE
            binding.fabTambahPelanggan.setOnClickListener {
                 Toast.makeText(context, "Fitur tambah pelanggan dinonaktifkan sementara.", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.fabTambahPelanggan.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        pelangganAdapter = PelangganAdapter(
            emptyList(),
            onDetailClick = { pelanggan ->
                val action = PelangganFragmentDirections.actionPelangganFragmentToDetailPelangganFragment(pelanggan.idPelanggan)
                findNavController().navigate(action)
            },
            onMenuClick = { pelanggan, view ->
                showPopupMenu(pelanggan, view)
            }
        )
        binding.recyclerViewPelanggan.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pelangganAdapter
        }
    }

    private fun showPopupMenu(pelanggan: PelangganData, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_pelanggan_item, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_detail -> {
                    val action = PelangganFragmentDirections.actionPelangganFragmentToDetailPelangganFragment(pelanggan.idPelanggan)
                    findNavController().navigate(action)
                    true
                }
                R.id.menu_edit -> {
                    Toast.makeText(context, "Edit: ${pelanggan.nama}", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_hapus -> {
                    Toast.makeText(context, "Hapus: ${pelanggan.nama}", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setupSearchView() {
        binding.searchViewPelanggan.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPelanggan(newText)
                return true
            }
        })
    }

    private fun fetchPelanggan() {
        setLoadingState(true)
        val statusToFetch = if (showActive) "aktif" else "nonaktif"
        apiService.getDataPelanggan(status = statusToFetch).enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (_binding == null || !isAdded) return
                setLoadingState(false)
                if (response.isSuccessful) {
                    allPelanggan = response.body()?.data ?: emptyList()
                    pelangganAdapter.updateData(allPelanggan)
                    if (allPelanggan.isEmpty()) {
                        Toast.makeText(context, "Tidak ada data pelanggan.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMsg = "Gagal memuat data pelanggan (Error ${response.code()})"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                setLoadingState(false)
                val errorMsg = "Gagal memuat data. Periksa koneksi Anda."
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (_binding == null) return
        binding.progressBarPelanggan.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            binding.fabTambahPelanggan.hide()
        } else {
            if (showActive) binding.fabTambahPelanggan.show()
        }
    }

    private fun filterPelanggan(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allPelanggan
        } else {
            allPelanggan.filter {
                it.nama.contains(query, ignoreCase = true) ||
                it.alamat?.contains(query, ignoreCase = true) == true ||
                it.idPelanggan.contains(query, ignoreCase = true)
            }
        }
        pelangganAdapter.updateData(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
