package com.linkbit.billrt

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PembayaranHariIniAdapter
import com.linkbit.billrt.adapter.UserSummaryAdapter
import com.linkbit.billrt.api.RetrofitClient
import com.linkbit.billrt.databinding.FragmentPembayaranHariIniBinding
import com.linkbit.billrt.model.PembayaranHariIni
import com.linkbit.billrt.model.PembayaranHariIniResponse
import com.linkbit.billrt.model.PerUserPencatat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PembayaranHariIniFragment : BaseFragment() {

    private var _binding: FragmentPembayaranHariIniBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PembayaranHariIniAdapter
    private val dataList = mutableListOf<PembayaranHariIni>()

    private lateinit var userAdapter: UserSummaryAdapter
    private val userList = mutableListOf<PerUserPencatat>()

    private var currentTanggal: String? = null
    private var currentSearch: String? = null
    private var currentUserId: Int? = null

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPembayaranHariIniBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyWindowInsets(binding.appBarLayout)

        setupToolbar()
        setupRecyclerView()

        binding.swipeRefresh.setOnRefreshListener {
            fetchData()
        }

        fetchData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.inflateMenu(R.menu.menu_pelanggan_lunas)
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.apply {
            queryHint = "Cari nama pelanggan..."
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    currentSearch = query
                    fetchData()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrBlank()) {
                        currentSearch = null
                        fetchData()
                    }
                    return true
                }
            })
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_filter -> {
                    showDatePicker()
                    true
                }
                R.id.action_reset_filter -> {
                    currentTanggal = null
                    currentSearch = null
                    currentUserId = null
                    userAdapter.resetSelection()
                    fetchData()
                    Toast.makeText(context, "Filter direset", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                currentTanggal = dateFormat.format(calendar.time)
                fetchData()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupRecyclerView() {
        // Get user ID for adapter
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("USER_ID", -1)

        // Main Adapter
        adapter = PembayaranHariIniAdapter(dataList, userId)
        binding.rvPembayaran.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPembayaran.adapter = adapter

        // User Filter Adapter
        userAdapter = UserSummaryAdapter(userList) { selectedId ->
            currentUserId = selectedId
            fetchData()
        }
        binding.rvUserSummary.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvUserSummary.adapter = userAdapter
    }

    private fun fetchData() {
        binding.swipeRefresh.isRefreshing = true
        
        RetrofitClient.instance.getPembayaranHariIni(currentTanggal, currentSearch, currentUserId)
            .enqueue(object : Callback<PembayaranHariIniResponse> {
                override fun onResponse(
                    call: Call<PembayaranHariIniResponse>,
                    response: Response<PembayaranHariIniResponse>
                ) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful && response.body()?.status == true) {
                        val res = response.body()!!
                        
                        // Update Main Data
                        dataList.clear()
                        dataList.addAll(res.data)
                        adapter.notifyDataSetChanged()

                        // Update User List (Summary)
                        val summary = res.summary
                        val users = summary?.perUserPencatat ?: emptyList()
                        
                        // Only update user list if we are not currently filtered by user, 
                        // or if the list was empty (first load)
                        if (currentUserId == null || userList.isEmpty()) {
                            userList.clear()
                            userList.addAll(users)
                            userAdapter.notifyDataSetChanged()
                        }

                        updateUI(summary?.totalNominal ?: 0.0, summary?.totalItem ?: 0)
                    } else {
                        showEmptyState()
                    }
                }

                override fun onFailure(call: Call<PembayaranHariIniResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    if (isAdded) {
                        Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        showEmptyState()
                    }
                }
            })
    }

    private fun updateUI(totalNominal: Double, totalItem: Int) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("USER_ID", -1)

        if (userId != 1) {
            binding.tvTotalNominal.text = "Rp ••••••"
        } else {
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvTotalNominal.text = formatter.format(totalNominal).replace("Rp", "Rp ")
        }
        
        binding.tvTotalItem.text = "$totalItem Transaksi"

        if (dataList.isEmpty()) {
            showEmptyState()
        } else {
            binding.rvPembayaran.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
        }

        if (userList.isEmpty()) {
            binding.rvUserSummary.visibility = View.GONE
        } else {
            binding.rvUserSummary.visibility = View.VISIBLE
        }
    }

    private fun showEmptyState() {
        dataList.clear()
        adapter.notifyDataSetChanged()
        binding.rvPembayaran.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
        
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("USER_ID", -1)
        
        if (userId != 1) {
            binding.tvTotalNominal.text = "Rp ••••••"
        } else {
            binding.tvTotalNominal.text = "Rp 0"
        }
        
        binding.tvTotalItem.text = "0 Transaksi"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
