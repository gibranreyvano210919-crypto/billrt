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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPelangganAktifBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = binding.fabTambahPelanggan.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = systemBars.bottom + resources.getDimensionPixelSize(R.dimen.fab_margin)
            binding.fabTambahPelanggan.layoutParams = params
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupSearchView()
        fetchPelangganAktif()

        binding.fabTambahPelanggan.setOnClickListener {
            // Menggunakan ID navigasi yang baru dan benar
            findNavController().navigate(R.id.action_pelangganAktifFragment_to_tambahPelangganFragment)
        }
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
        binding.rvPelangganAktif.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganAktif.adapter = pelangganAdapter
    }

    private fun showStatusMenu(pelanggan: PelangganData, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_pelanggan_aktif, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit_pelanggan -> {
                    // Arahkan juga menu edit ke halaman form yang baru
                    val bundle = bundleOf("pelangganIdToEdit" to pelanggan.idPelanggan)
                    findNavController().navigate(R.id.action_pelangganAktifFragment_to_tambahPelangganFragment, bundle)
                    true
                }
                R.id.menu_set_isolir_from_aktif -> {
                    updateStatus(pelanggan, "isolir")
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
        binding.progressBar.visibility = View.VISIBLE
        apiService.getDataPelanggan(status = "aktif").enqueue(object : Callback<PelangganResponse> {
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