package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PengeluaranAdapter
import com.linkbit.billrt.api.RetrofitClient
import com.linkbit.billrt.databinding.FragmentPengeluaranListBinding
import com.linkbit.billrt.model.PengeluaranResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.util.*

class PengeluaranListFragment : Fragment() {

    private var _binding: FragmentPengeluaranListBinding? = null
    private val binding get() = _binding!!
    private val args: PengeluaranListFragmentArgs by navArgs()
    
    private lateinit var adapter: PengeluaranAdapter
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPengeluaranListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Sync with arguments passed from PembukuanFragment
        if (args.bulan != 0) currentMonth = args.bulan
        if (args.tahun != 0) currentYear = args.tahun
        
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        updatePeriodeUI() // Set initial text from args
        loadData()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = args.kategori ?: "Daftar Pengeluaran"
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = PengeluaranAdapter { item ->
            val action = PengeluaranListFragmentDirections.actionPengeluaranListFragmentToPengeluaranFormFragment(item)
            findNavController().navigate(action)
        }
        binding.rvPengeluaran.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PengeluaranListFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            val action = PengeluaranListFragmentDirections.actionPengeluaranListFragmentToPengeluaranFormFragment(
                pengeluaranItem = null,
                bulan = currentMonth,
                tahun = currentYear
            )
            findNavController().navigate(action)
        }
        
        binding.tvFilterPeriode.setOnClickListener {
            showMonthYearPicker()
        }
    }

    private fun showMonthYearPicker() {
        val view = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = view.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker)

        monthPicker.minValue = 0
        monthPicker.maxValue = 11
        monthPicker.displayedValues = DateFormatSymbols().months
        monthPicker.value = currentMonth - 1

        val year = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = year - 5
        yearPicker.maxValue = year + 5
        yearPicker.value = currentYear

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Periode")
            .setView(view)
            .setPositiveButton("Pilih") { _, _ ->
                currentMonth = monthPicker.value + 1
                currentYear = yearPicker.value
                updatePeriodeUI()
                loadData()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updatePeriodeUI() {
        val monthName = DateFormatSymbols().months[currentMonth - 1]
        binding.tvFilterPeriode.text = "$monthName $currentYear"
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        
        val apiService = RetrofitClient.instance
        apiService.getListPengeluaran(currentMonth, currentYear).enqueue(object : Callback<PengeluaranResponse> {
            override fun onResponse(call: Call<PengeluaranResponse>, response: Response<PengeluaranResponse>) {
                if (_binding == null) return
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.status == true) {
                    val allData = response.body()?.data ?: emptyList()
                    val filteredData = if (args.kategori != null) {
                        allData.filter { it.kategori.contains(args.kategori!!, ignoreCase = true) }
                    } else {
                        allData
                    }
                    
                    adapter.submitList(filteredData)
                    
                    // Update total nominal based on filtered results
                    val totalFiltered = filteredData.sumOf { it.jumlah }
                    binding.tvTotalNominal.text = formatRupiah(totalFiltered)
                } else {
                    adapter.submitList(emptyList())
                    binding.tvTotalNominal.text = "Rp 0"
                    Toast.makeText(requireContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PengeluaranResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatRupiah(number: Double): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).replace(",00", "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
