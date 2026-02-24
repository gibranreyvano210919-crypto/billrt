package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.TransaksiAdapter
import com.linkbit.billrt.api.RetrofitClient
import com.linkbit.billrt.databinding.FragmentTransaksiBinding
import com.linkbit.billrt.model.Transaksi
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
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransaksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupFilterUI()
        fetchTransaksiData()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarTransaksi)
        binding.toolbarTransaksi.setupWithNavController(findNavController())
    }

    private fun setupRecyclerView() {
        transaksiAdapter = TransaksiAdapter(emptyList())
        binding.rvTransaksi.layoutManager = LinearLayoutManager(context)
        binding.rvTransaksi.adapter = transaksiAdapter
    }

    private fun setupFilterUI() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.tietTglMulai.setText(sdf.format(calendar.time))
        binding.tietTglAkhir.setText(sdf.format(calendar.time))

        binding.tietTglMulai.setOnClickListener { showDatePickerDialog(isStartDate = true) }
        binding.tietTglAkhir.setOnClickListener { showDatePickerDialog(isStartDate = false) }
        
        // Dummy data for admin spinner, replace with actual data from API if available
        val admins = arrayOf("Semua Admin", "Admin 1", "Admin 2")
        val adminAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, admins)
        adminAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAdmin.adapter = adminAdapter

        binding.btnFilterTransaksi.setOnClickListener { fetchTransaksiData() }

        binding.svTransaksi.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                fetchTransaksiData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            if (isStartDate) {
                binding.tietTglMulai.setText(sdf.format(calendar.time))
            } else {
                binding.tietTglAkhir.setText(sdf.format(calendar.time))
            }
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
        binding.pbTransaksi.visibility = View.VISIBLE
        binding.rvTransaksi.visibility = View.GONE
        binding.tvEmptyTransaksi.visibility = View.GONE

        val tglMulai = binding.tietTglMulai.text.toString()
        val tglAkhir = binding.tietTglAkhir.text.toString()
        val search = binding.svTransaksi.query.toString()
        val adminId = binding.spinnerAdmin.selectedItemPosition // Assuming 0 is for all admins

        RetrofitClient.instance.getTransaksi(tglMulai, tglAkhir, adminId, search).enqueue(object : Callback<TransaksiResponse> {
            override fun onResponse(call: Call<TransaksiResponse>, response: Response<TransaksiResponse>) {
                if (!isAdded) return
                binding.pbTransaksi.visibility = View.GONE

                if (response.isSuccessful) {
                    val transaksiResponse = response.body()
                    if (transaksiResponse != null && transaksiResponse.status) {
                        updateSummary(transaksiResponse.summary.totalTransaksi, transaksiResponse.summary.totalNominal)
                        transaksiAdapter.updateData(transaksiResponse.data)
                        binding.rvTransaksi.visibility = View.VISIBLE
                        binding.tvEmptyTransaksi.visibility = if (transaksiResponse.data.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        binding.tvEmptyTransaksi.text = "Gagal memuat data: ${transaksiResponse?.toString()}"
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
        numberFormat.minimumFractionDigits = 0

        binding.tvTotalTransaksi.text = "Total Transaksi: $totalTransaksi"
        binding.tvTotalNominal.text = "Total Nominal: ${numberFormat.format(totalNominal)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}