package com.linkbit.billrt

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentTransaksiPemasukanBinding
import com.linkbit.billrt.model.AdminItem
import com.linkbit.billrt.model.AdminResponse
import com.linkbit.billrt.model.PemasukanResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormatSymbols
import java.util.*

class TransaksiPemasukanFragment : BaseFragment() {

    private var _binding: FragmentTransaksiPemasukanBinding? = null
    private val binding get() = _binding!!
    
    private val args: TransaksiPemasukanFragmentArgs by navArgs()
    
    private lateinit var adapter: PemasukanAdapter
    private lateinit var sessionManager: SessionManager
    
    private var currentMonth = 1
    private var currentYear = 2024
    private var currentSearch = ""
    private var idUserPencatat = 0 
    private var userName = ""
    private var userLevel: Int = -1
    private var listAdmin = mutableListOf<AdminItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransaksiPemasukanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        currentMonth = args.bulan
        currentYear = args.tahun
        
        setupUserFilter()
        applyWindowInsets(binding.appBarLayout)
        setupToolbar()
        setupRecyclerView()
        setupMenu()
        
        updateFilterText()
        loadData()
        
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }

        binding.layoutFilterPencatat.setOnClickListener {
            // Level 1 dianggap sebagai superadmin
            if (userLevel == 1) {
                showAdminFilterDialog()
            }
        }
    }

    /**
     * Mengatur filter pencatat berdasarkan level user yang login.
     * Jika superadmin (Level 1): default menampilkan semua data (idUserPencatat = 0) dan dropdown aktif.
     * Jika admin biasa: hanya menampilkan data miliknya sendiri.
     */
    private fun setupUserFilter() {
        userLevel = sessionManager.getUserLevel()
        userName = sessionManager.getUserName()
        val userIdString = sessionManager.getUserId()
        val userId = userIdString?.toIntOrNull() ?: 0

        // Logika: Jika superadmin (Level 1), idUserPencatat diset 0 untuk memunculkan semua data
        if (userLevel == 1) {
            idUserPencatat = 0
            binding.tvNamaPencatatFilter.text = "Pencatat: Semua"
            binding.ivDropdownPencatat.visibility = View.VISIBLE
            binding.layoutFilterPencatat.isClickable = true
            fetchAdmins()
        } else {
            // Jika bukan superadmin, kunci filter ke ID user yang sedang login
            idUserPencatat = userId
            binding.tvNamaPencatatFilter.text = "Pencatat: $userName"
            binding.ivDropdownPencatat.visibility = View.GONE
            binding.layoutFilterPencatat.isClickable = false
        }
    }

    private fun fetchAdmins() {
        apiService.getListAdmin().enqueue(object : Callback<AdminResponse> {
            override fun onResponse(call: Call<AdminResponse>, response: Response<AdminResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    listAdmin.clear()
                    listAdmin.add(AdminItem(0, "Semua"))
                    response.body()?.data?.let { listAdmin.addAll(it) }
                }
            }
            override fun onFailure(call: Call<AdminResponse>, t: Throwable) {}
        })
    }

    private fun showAdminFilterDialog() {
        if (listAdmin.isEmpty()) {
            fetchAdmins()
            Toast.makeText(context, "Memuat daftar admin...", Toast.LENGTH_SHORT).show()
            return
        }

        val items = listAdmin.map { it.namaLengkap }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Pencatat")
            .setItems(items) { _, which ->
                val selected = listAdmin[which]
                idUserPencatat = selected.idUser
                binding.tvNamaPencatatFilter.text = "Pencatat: ${selected.namaLengkap}"
                loadData()
            }
            .show()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Transaksi Pemasukan"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_search, menu)
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                
                searchView.queryHint = "Cari pelanggan..."
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        currentSearch = newText ?: ""
                        loadData()
                        return true
                    }
                })
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        adapter = PemasukanAdapter(emptyList())
        binding.rvPemasukan.layoutManager = LinearLayoutManager(context)
        binding.rvPemasukan.adapter = adapter
    }

    private fun updateFilterText() {
        val months = DateFormatSymbols().months
        if (currentMonth in 1..12) {
            val monthName = months[currentMonth - 1]
            binding.tvPeriode.text = "$monthName $currentYear"
        }
    }

    private fun loadData() {
        if (_binding == null) return
        binding.progressBar.visibility = View.VISIBLE
        
        apiService.getPemasukan(currentMonth, currentYear, idUserPencatat, currentSearch).enqueue(object : Callback<PemasukanResponse> {
            override fun onResponse(call: Call<PemasukanResponse>, response: Response<PemasukanResponse>) {
                if (_binding == null) return
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                
                if (response.isSuccessful && response.body()?.status == true) {
                    val body = response.body()!!
                    adapter.updateData(body.data)
                    
                    binding.tvTotalTransaksi.text = body.summary.totalTransaksi.toString()
                    binding.tvJumlahPelanggan.text = body.summary.jumlahPelanggan.toString()
                    binding.tvTotalNominal.text = formatRupiah(body.summary.totalPemasukan)
                } else {
                    Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PemasukanResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatRupiah(number: Double): String {
        val localeID = Locale("in", "ID")
        val numberFormat = java.text.NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).replace(",00", "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
