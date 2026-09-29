package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.linkbit.billrt.databinding.FragmentPelangganIsolirBinding
import com.linkbit.billrt.model.StandardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganIsolirFragment : BaseFragment() {

    private var _binding: FragmentPelangganIsolirBinding? = null
    private val binding get() = _binding!!

    private lateinit var pelangganAdapter: PelangganIsolirAdapter
    private var pelangganList: List<PelangganIsolir> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPelangganIsolirBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sinkronisasi Insets agar toolbar tidak menabrak status bar
        applyWindowInsets(binding.appBarLayout)
        
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        fetchPelangganIsolir()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Inflate menu search ke toolbar
        binding.toolbar.inflateMenu(R.menu.menu_search)
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.apply {
            queryHint = "Cari pelanggan isolir..."
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
        pelangganAdapter = PelangganIsolirAdapter(emptyList(),
            onDetailClick = { pelanggan ->
                Toast.makeText(context, "Clicked on ${pelanggan.namaPelanggan}", Toast.LENGTH_SHORT).show()
            },
            onLongClick = { pelanggan ->
                showStatusBottomSheet(pelanggan)
            }
        )
        binding.rvPelangganIsolir.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganIsolir.adapter = pelangganAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchPelangganIsolir()
        }
    }

    private fun showStatusBottomSheet(pelanggan: PelangganIsolir) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_pelanggan_isolir_menu, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        // Prevent accidental dismissal
        bottomSheetDialog.setCanceledOnTouchOutside(false)

        // Disable drag and swipe to dismiss
        bottomSheetDialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.isHideable = false
            behavior.isDraggable = false
        }

        val customerName = bottomSheetView.findViewById<TextView>(R.id.tv_customer_name)
        val mikrotikUsername = bottomSheetView.findViewById<TextView>(R.id.tv_mikrotik_username)
        val batalkanIsolir = bottomSheetView.findViewById<TextView>(R.id.option_batalkan_isolir)
        val optionNonaktifkan = bottomSheetView.findViewById<TextView>(R.id.option_nonaktifkan_pelanggan)

        customerName.text = pelanggan.namaPelanggan
        mikrotikUsername.text = "Username: ${pelanggan.mikrotikUsername ?: "-"}"

        batalkanIsolir.setOnClickListener {
            updateStatus(pelanggan.idPelanggan, "aktif", bottomSheetDialog)
        }

        optionNonaktifkan.setOnClickListener {
            nonaktifkanStatus(pelanggan.idPelanggan, bottomSheetDialog)
        }

        bottomSheetDialog.show()
    }


    private fun updateStatus(idPelanggan: String, newStatus: String, dialog: BottomSheetDialog) {
        val request = UpdateStatusRequest(idPelanggan, newStatus)
        apiService.updateStatusPelanggan(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Status berhasil diubah", Toast.LENGTH_SHORT).show()
                    fetchPelangganIsolir() // Refresh the list
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Gagal mengubah status", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun nonaktifkanStatus(idPelanggan: String, dialog: BottomSheetDialog) {
        ApiConfig.apiService.nonaktifPelanggan(idPelanggan).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, response.body()?.message ?: "Pelanggan dinonaktifkan", Toast.LENGTH_SHORT).show()
                    fetchPelangganIsolir() // Refresh the list
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, response.body()?.message ?: "Gagal menonaktifkan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPelangganIsolir() {
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }
        apiService.getPelangganIsolir(null, null).enqueue(object : Callback<PelangganIsolirResponse> {
            override fun onResponse(call: Call<PelangganIsolirResponse>, response: Response<PelangganIsolirResponse>) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    pelangganList = responseBody?.data ?: emptyList()
                    pelangganAdapter.updateData(pelangganList)
                } else {
                    Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PelangganIsolirResponse>, t: Throwable) {
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
                it.namaPelanggan?.contains(query, ignoreCase = true) == true || 
                it.idPelanggan.contains(query, ignoreCase = true)
            }
        }
        pelangganAdapter.updateData(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
