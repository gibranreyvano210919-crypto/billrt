package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.api.BillStatementResponse
import com.linkbit.billrt.api.RetrofitClient
import com.linkbit.billrt.databinding.FragmentPembukuanBinding
import com.linkbit.billrt.model.PengeluaranItem
import com.linkbit.billrt.model.PengeluaranResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.util.*

class PembukuanFragment : BaseFragment() {

    private var _binding: FragmentPembukuanBinding? = null
    private val binding get() = _binding!!

    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPembukuanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        applyWindowInsets(binding.appBarLayout)
        setupToolbar()
        updatePeriodeText()

        val navController = findNavController()

        // Filter Periode Click Listener
        binding.btnPilihPeriode.setOnClickListener {
            showMonthYearPicker()
        }

        // Pemasukkan Click Listeners
        binding.btnTransaksiCash.setOnClickListener {
            val action = PembukuanFragmentDirections.actionPembukuanFragmentToTransaksiPemasukanFragment(
                bulan = currentMonth,
                tahun = currentYear
            )
            navController.navigate(action)
        }
        binding.btnTransaksiOnline.setOnClickListener {
            navController.navigate(R.id.action_pembukuanFragment_to_laporanBulananFragment)
        }
        binding.btnPemasukkanLain.setOnClickListener {
            navController.navigate(R.id.action_pembukuanFragment_to_kasFragment)
        }
        binding.btnSelisih.setOnClickListener {
            // Optional: Tambahkan navigasi jika ada detail selisih
            Toast.makeText(requireContext(), "Detail Selisih", Toast.LENGTH_SHORT).show()
        }

        // Pengeluaran Click Listeners (Matched to Master Kategori)
        binding.btnGajiKaryawan.setOnClickListener { navigateToPengeluaran("Gaji Karyawan") }
        binding.btnPasangBaru.setOnClickListener { navigateToPengeluaran("Pasang Baru") }
        binding.btnPerbaikanAlat.setOnClickListener { navigateToPengeluaran("Perbaikan Alat") }
        binding.btnBayarBandwidth.setOnClickListener { navigateToPengeluaran("Bayar Bandwidth") }
        binding.btnBayarKangTagih.setOnClickListener { navigateToPengeluaran("Bayar Penagihan") }
        binding.btnListrikPdamPulsa.setOnClickListener { navigateToPengeluaran("Listrik/PDAM/Pulsa") }
        binding.btnBayarMarketing.setOnClickListener { navigateToPengeluaran("Bayar Marketing") }
        binding.btnLainLain.setOnClickListener { navigateToPengeluaran(null) }

        loadData()
    }

    private fun showMonthYearPicker() {
        val view = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = view.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker)

        val months = DateFormatSymbols().months
        monthPicker.minValue = 0
        monthPicker.maxValue = 11
        monthPicker.displayedValues = months
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
                updatePeriodeText()
                loadData()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updatePeriodeText() {
        val monthName = DateFormatSymbols().months[currentMonth - 1]
        binding.tvPeriode.text = "$monthName $currentYear"
    }

    private fun loadData() {
        val apiService = RetrofitClient.instance

        // Reset nominal labels to Rp 0 before loading new data
        binding.tvValCash.text = "Rp 0"
        binding.tvValOnline.text = "Rp 0"
        binding.tvValPemasukanLain.text = "Rp 0"
        binding.tvValSelisih.text = "Rp 0"
        
        binding.valGajiKaryawan.text = "Rp 0"
        binding.valPasangBaru.text = "Rp 0"
        binding.valPerbaikanAlat.text = "Rp 0"
        binding.valBayarBandwidth.text = "Rp 0"
        binding.valBayarKangTagih.text = "Rp 0"
        binding.valListrikPdamPulsa.text = "Rp 0"
        binding.valBayarMarketing.text = "Rp 0"
        binding.valLainLain.text = "Rp 0"

        // Load Income Summary
        apiService.getBillStatement(currentMonth, currentYear).enqueue(object : Callback<BillStatementResponse> {
            override fun onResponse(call: Call<BillStatementResponse>, response: Response<BillStatementResponse>) {
                if (_binding == null) return
                if (response.isSuccessful && response.body()?.status == true) {
                    val summary = response.body()?.summary
                    if (summary != null) {
                        binding.tvValCash.text = formatRupiah(summary.totalIncome)
                        binding.tvValOnline.text = formatRupiah(summary.totalOnline)
                        binding.tvValPemasukanLain.text = formatRupiah(summary.totalSetoran)
                        binding.tvValSelisih.text = formatRupiah(summary.totalSelisih)
                    }
                }
            }
            override fun onFailure(call: Call<BillStatementResponse>, t: Throwable) {}
        })

        // Load Expenses and breakdown by category
        apiService.getListPengeluaran(currentMonth, currentYear).enqueue(object : Callback<PengeluaranResponse> {
            override fun onResponse(call: Call<PengeluaranResponse>, response: Response<PengeluaranResponse>) {
                if (_binding == null) return
                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data ?: emptyList()
                    updateExpenseBreakdown(data)
                }
            }
            override fun onFailure(call: Call<PengeluaranResponse>, t: Throwable) {}
        })
    }

    private fun updateExpenseBreakdown(data: List<PengeluaranItem>) {
        // Group by category and sum
        val breakdown = data.groupBy { it.kategori }.mapValues { entry ->
            entry.value.sumOf { it.jumlah }
        }

        binding.valGajiKaryawan.text = formatRupiah(breakdown["Gaji Karyawan"] ?: 0.0)
        binding.valPasangBaru.text = formatRupiah(breakdown["Pasang Baru"] ?: 0.0)
        binding.valPerbaikanAlat.text = formatRupiah(breakdown["Perbaikan Alat"] ?: 0.0)
        binding.valBayarBandwidth.text = formatRupiah(breakdown["Bayar Bandwidth"] ?: 0.0)
        binding.valBayarKangTagih.text = formatRupiah(breakdown["Bayar Penagihan"] ?: 0.0)
        binding.valListrikPdamPulsa.text = formatRupiah(breakdown["Listrik/PDAM/Pulsa"] ?: 0.0)
        binding.valBayarMarketing.text = formatRupiah(breakdown["Bayar Marketing"] ?: 0.0)
        
        // Sum everything else into Lain-lain
        val knownCategories = listOf("Gaji Karyawan", "Pasang Baru", "Perbaikan Alat", "Bayar Bandwidth", "Bayar Penagihan", "Listrik/PDAM/Pulsa", "Bayar Marketing")
        val lainLainTotal = data.filter { it.kategori !in knownCategories }.sumOf { it.jumlah }
        binding.valLainLain.text = formatRupiah(lainLainTotal)
    }

    private fun formatRupiah(number: Double): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).replace(",00", "")
    }

    private fun navigateToPengeluaran(kategori: String?) {
        val action = PembukuanFragmentDirections.actionPembukuanFragmentToPengeluaranListFragment(
            kategori = kategori,
            bulan = currentMonth,
            tahun = currentYear
        )
        findNavController().navigate(action)
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Pembukuan"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
