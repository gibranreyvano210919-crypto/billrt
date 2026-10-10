package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.TransaksiAdapter
import com.linkbit.billrt.api.RetrofitClient
import com.linkbit.billrt.databinding.FragmentTransaksiBinding
import com.linkbit.billrt.model.SummaryUserPencatat
import com.linkbit.billrt.model.SummaryWilayahTransaksi
import com.linkbit.billrt.model.TransaksiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransaksiFragment : BaseFragment() {

    private var _binding: FragmentTransaksiBinding? = null
    private val binding get() = _binding!!

    private lateinit var transaksiAdapter: TransaksiAdapter
    private lateinit var sessionManager: SessionManager
    private val calendar: Calendar = Calendar.getInstance()
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displaySdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    private var summaryAdminList: List<SummaryUserPencatat> = emptyList()
    private var summaryWilayahList: List<SummaryWilayahTransaksi> = emptyList()
    private var searchQuery: String = ""
    private var isDateSelected: Boolean = false 
    
    private var selectedAdminId: Int? = null
    private var selectedWilayahId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransaksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        
        val appBarLayout = binding.toolbarTransaksi.parent as? View
        appBarLayout?.let { applyWindowInsets(it) }
        
        setupToolbar()
        setupRecyclerView()
        setupMenu()
        
        binding.cvSummary.setOnClickListener {
            showDatePickerDialog()
        }
        
        // Secara default, muat data hari ini tanpa memunculkan kalender
        isDateSelected = true
        updateToolbarSubtitle()
        fetchTransaksiData()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarTransaksi)
        binding.toolbarTransaksi.setupWithNavController(findNavController())
        binding.toolbarTransaksi.title = "Riwayat Transaksi"
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_transaksi, menu)
                
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem?.actionView as? SearchView
                
                searchView?.queryHint = "Cari Nama/Username..."
                searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        searchQuery = query ?: ""
                        fetchTransaksiData()
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        searchQuery = newText ?: ""
                        return true
                    }
                })
                
                searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(item: MenuItem): Boolean = true
                    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                        searchQuery = ""
                        fetchTransaksiData()
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_filter_date -> {
                        showDatePickerDialog()
                        true
                    }
                    R.id.action_filter_more -> {
                        showFilterBottomSheet()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showFilterBottomSheet() {
        if (!isDateSelected) {
            Toast.makeText(context, "Silakan pilih tanggal terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val bottomSheet = TransaksiFilterBottomSheet.newInstance(
            summaryAdminList,
            summaryWilayahList,
            selectedAdminId,
            selectedWilayahId
        )
        bottomSheet.setOnFilterAppliedListener { adminId: Int?, wilayahId: Int? ->
            selectedAdminId = adminId
            selectedWilayahId = wilayahId
            fetchTransaksiData()
        }
        bottomSheet.show(childFragmentManager, "TransaksiFilterBottomSheet")
    }

    private fun updateToolbarSubtitle() {
        val dateString = displaySdf.format(calendar.time)
        binding.toolbarTransaksi.subtitle = "Tanggal: $dateString"
        binding.tvTanggalPilih.text = dateString
    }

    private fun setupRecyclerView() {
        transaksiAdapter = TransaksiAdapter(emptyList())
        binding.rvTransaksi.layoutManager = LinearLayoutManager(context)
        binding.rvTransaksi.adapter = transaksiAdapter
    }

    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            
            isDateSelected = true 
            updateToolbarSubtitle()
            // Reset filters when date changes agar data konsisten
            selectedAdminId = null
            selectedWilayahId = null
            fetchTransaksiData()
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun fetchTransaksiData() {
        if (!isAdded || !isDateSelected) return 
        
        binding.pbTransaksi.visibility = View.VISIBLE
        binding.rvTransaksi.visibility = View.GONE
        binding.tvEmptyTransaksi.visibility = View.GONE

        val tanggal = sdf.format(calendar.time)
        
        RetrofitClient.instance.getTransaksiHariIni(
            tanggal, 
            selectedAdminId, 
            selectedWilayahId, 
            searchQuery
        ).enqueue(object : Callback<TransaksiResponse> {
            override fun onResponse(call: Call<TransaksiResponse>, response: Response<TransaksiResponse>) {
                if (!isAdded) return
                binding.pbTransaksi.visibility = View.GONE

                if (response.isSuccessful) {
                    val transaksiResponse = response.body()
                    if (transaksiResponse != null && transaksiResponse.status) {
                        updateSummary(transaksiResponse.summary.totalTransaksi, transaksiResponse.summary.totalNominal)
                        
                        // Store summaries for filter bottom sheet
                        summaryAdminList = transaksiResponse.summary.perUserPencatat ?: emptyList()
                        summaryWilayahList = transaksiResponse.summary.perWilayah ?: emptyList()
                        
                        transaksiAdapter.updateData(transaksiResponse.data)
                        binding.rvTransaksi.visibility = View.VISIBLE
                        binding.tvEmptyTransaksi.text = "Tidak ada data transaksi."
                        binding.tvEmptyTransaksi.visibility = if (transaksiResponse.data.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        binding.tvEmptyTransaksi.text = "Gagal memuat data."
                        binding.tvEmptyTransaksi.visibility = View.VISIBLE
                    }
                } else {
                    binding.tvEmptyTransaksi.text = "Gagal memuat: Error ${response.code()}"
                    binding.tvEmptyTransaksi.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<TransaksiResponse>, t: Throwable) {
                if (!isAdded) return
                binding.pbTransaksi.visibility = View.GONE
                binding.tvEmptyTransaksi.text = "Gagal memuat: ${t.message}"
                binding.tvEmptyTransaksi.visibility = View.VISIBLE
            }
        })
    }

    private fun updateSummary(totalTransaksi: Int, totalNominal: Float) {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0

        binding.tvTotalTransaksi.text = "Total Transaksi: $totalTransaksi"
        binding.tvTotalNominal.text = "Total Nominal: ${numberFormat.format(totalNominal)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
