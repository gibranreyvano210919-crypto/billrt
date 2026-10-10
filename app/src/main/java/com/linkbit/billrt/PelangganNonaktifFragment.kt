package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.linkbit.billrt.databinding.FragmentPelangganNonaktifBinding
import com.linkbit.billrt.model.StandardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganNonaktifFragment : BaseFragment() {

    private var _binding: FragmentPelangganNonaktifBinding? = null
    private val binding get() = _binding!!

    private lateinit var pelangganAdapter: PelangganNonaktifAdapter
    private var pelangganList: List<PelangganNonaktif> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPelangganNonaktifBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets(binding.appBarLayout)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        
        fetchData()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            inflateMenu(R.menu.menu_search)
            val searchItem = menu.findItem(R.id.action_search)
            val searchView = searchItem.actionView as? SearchView

            searchView?.apply {
                queryHint = "Cari pelanggan nonaktif..."
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        filter(newText)
                        return true
                    }
                })
            }
        }
    }

    private fun setupRecyclerView() {
        pelangganAdapter = PelangganNonaktifAdapter(
            emptyList(),
            onDetailClick = { pelanggan ->
                val action = PelangganNonaktifFragmentDirections
                    .actionPelangganNonaktifFragmentToDetailPelangganFragment(pelanggan.idPelanggan)
                findNavController().navigate(action)
            },
            onItemLongClick = { pelanggan ->
                showBottomSheetNonaktif(pelanggan)
            }
        )
        binding.rvPelangganNonaktif.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganNonaktif.adapter = pelangganAdapter
    }

    private fun showBottomSheetNonaktif(pelanggan: PelangganNonaktif) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_nonaktif, null)

        view.findViewById<TextView>(R.id.tv_customer_name).text = pelanggan.namaPelanggan
        view.findViewById<TextView>(R.id.tv_id_pelanggan).text = "ID: ${pelanggan.idPelanggan}"

        view.findViewById<TextView>(R.id.option_aktifkan_pelanggan).setOnClickListener {
            dialog.dismiss()
            confirmAktifkanPelanggan(pelanggan)
        }

        view.findViewById<TextView>(R.id.option_hapus_pelanggan).setOnClickListener {
            dialog.dismiss()
            confirmHapusPelanggan(pelanggan)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun confirmAktifkanPelanggan(pelanggan: PelangganNonaktif) {
        AlertDialog.Builder(requireContext())
            .setTitle("Aktifkan Pelanggan")
            .setMessage("Apakah Anda yakin ingin mengaktifkan kembali pelanggan ${pelanggan.namaPelanggan}?")
            .setPositiveButton("Ya") { _, _ ->
                aktifkanPelanggan(pelanggan.idPelanggan)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmHapusPelanggan(pelanggan: PelangganNonaktif) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pelanggan")
            .setMessage("Apakah Anda yakin ingin menghapus pelanggan ${pelanggan.namaPelanggan} secara permanen?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusPelanggan(pelanggan.idPelanggan)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun aktifkanPelanggan(idPelanggan: String) {
        binding.progressBar.visibility = View.VISIBLE
        // Pastikan apiService memiliki metode aktifkanPelanggan(id_pelanggan: String)
        apiService.aktifkanPelanggan(idPelanggan).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, response.body()?.message ?: "Pelanggan berhasil diaktifkan", Toast.LENGTH_SHORT).show()
                    fetchData()
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal mengaktifkan pelanggan"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hapusPelanggan(idPelanggan: String) {
        binding.progressBar.visibility = View.VISIBLE
        // Perbaikan: Bungkus id_pelanggan ke dalam objek PelangganIdRequest sesuai kebutuhan ApiService
        val request = PelangganIdRequest(id_pelanggan = idPelanggan)
        apiService.hapusPelanggan(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, response.body()?.message ?: "Pelanggan berhasil dihapus", Toast.LENGTH_SHORT).show()
                    fetchData()
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal menghapus pelanggan"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchData()
        }
    }

    private fun fetchData() {
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }
        
        apiService.getPelangganNonaktif(null, null).enqueue(object : Callback<PelangganNonaktifResponse> {
            override fun onResponse(call: Call<PelangganNonaktifResponse>, response: Response<PelangganNonaktifResponse>) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    pelangganList = response.body()?.data ?: emptyList()
                    pelangganAdapter.updateData(pelangganList)
                } else {
                    Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PelangganNonaktifResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
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
                it.namaPelanggan?.contains(query, ignoreCase = true) == true || 
                it.idPelanggan.contains(query, ignoreCase = true) ||
                it.mikrotikUsername?.contains(query, ignoreCase = true) == true
            }
        }
        pelangganAdapter.updateData(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
