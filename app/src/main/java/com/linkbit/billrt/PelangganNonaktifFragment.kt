package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentPelangganNonaktifBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganNonaktifFragment : BaseFragment() {

    private var _binding: FragmentPelangganNonaktifBinding? = null
    private val binding get() = _binding!!

    private lateinit var pelangganAdapter: PelangganAdapter
    private var pelangganList: List<PelangganData> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPelangganNonaktifBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        fetchPelangganNonaktif()
    }

    private fun setupRecyclerView() {
        pelangganAdapter = PelangganAdapter(emptyList(),
            onDetailClick = { pelanggan ->
                val bundle = bundleOf("pelangganId" to pelanggan.idPelanggan)
                findNavController().navigate(R.id.action_global_detailPelangganFragment, bundle)
            },
            onMenuClick = { pelanggan, view ->
                showDeleteMenu(pelanggan, view)
            }
        )
        binding.rvPelangganNonaktif.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganNonaktif.adapter = pelangganAdapter
    }

    private fun showDeleteMenu(pelanggan: PelangganData, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_pelanggan_nonaktif, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_delete_pelanggan -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Hapus Pelanggan")
                        .setMessage("Apakah Anda yakin ingin menghapus ${pelanggan.nama} secara permanen?")
                        .setPositiveButton("Hapus") { _, _ ->
                            deletePelanggan(pelanggan)
                        }
                        .setNegativeButton("Batal", null)
                        .show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun deletePelanggan(pelanggan: PelangganData) {
        val request = PelangganIdRequest(id_pelanggan = pelanggan.idPelanggan)
        apiService.hapusPelanggan(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Pelanggan berhasil dihapus", Toast.LENGTH_SHORT).show()
                    fetchPelangganNonaktif() // Refresh the list
                } else {
                    Toast.makeText(context, "Gagal menghapus pelanggan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSearchView() {
        binding.searchViewPelangganNonaktif.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })
    }

    private fun fetchPelangganNonaktif() {
        binding.progressBar.visibility = View.VISIBLE
        apiService.getDataPelanggan(status = "nonaktif").enqueue(object : Callback<PelangganResponse> {
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