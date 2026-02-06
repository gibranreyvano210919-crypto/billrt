package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentPelangganIsolirBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganIsolirFragment : BaseFragment() {

    private var _binding: FragmentPelangganIsolirBinding? = null
    private val binding get() = _binding!!

    private lateinit var pelangganAdapter: PelangganAdapter
    private var pelangganList: List<PelangganData> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPelangganIsolirBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        fetchPelangganIsolir()
    }

    private fun setupRecyclerView() {
        pelangganAdapter = PelangganAdapter(emptyList(),
            onDetailClick = { pelanggan ->
                val bundle = bundleOf("pelangganId" to pelanggan.idPelanggan)
                findNavController().navigate(R.id.action_global_detailPelangganFragment, bundle)
            },
            onMenuClick = { pelanggan, view ->
                showStatusMenu(pelanggan, view)
            }
        )
        binding.rvPelangganIsolir.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganIsolir.adapter = pelangganAdapter
    }

    private fun showStatusMenu(pelanggan: PelangganData, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_pelanggan_isolir, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_set_aktif_from_isolir -> {
                    updateStatus(pelanggan, "aktif")
                    true
                }
                R.id.menu_set_nonaktif_from_isolir -> {
                    updateStatus(pelanggan, "nonaktif")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun updateStatus(pelanggan: PelangganData, newStatus: String) {
        val request = UpdateStatusRequest(idPelanggan = pelanggan.idPelanggan, statusAktif = newStatus)
        apiService.updateStatusPelanggan(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Status berhasil diubah ke $newStatus", Toast.LENGTH_SHORT).show()
                    fetchPelangganIsolir() // Refresh the list
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
        binding.searchViewPelangganIsolir.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })
    }

    private fun fetchPelangganIsolir() {
        binding.progressBar.visibility = View.VISIBLE
        apiService.getDataPelanggan(status = "isolir").enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
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