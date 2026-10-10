package com.linkbit.billrt

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.linkbit.billrt.adapter.StatementLunasAdapter
import com.linkbit.billrt.api.RetrofitClient
import com.linkbit.billrt.databinding.FragmentBillStatementBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BillStatementFragment : BaseFragment() {

    private var _binding: FragmentBillStatementBinding? = null
    private val binding get() = _binding!!

    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var lunasAdapter: StatementLunasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillStatementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyWindowInsets(binding.appBarLayout)
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        setupChart()
        fetchData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        lunasAdapter = StatementLunasAdapter()
        binding.rvListLunas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = lunasAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupListeners() {
        binding.btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            fetchData()
        }
        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            fetchData()
        }
        binding.btnExportStatement.setOnClickListener {
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            val bundle = Bundle().apply {
                putInt("bulan", month)
                putInt("tahun", year)
            }
            findNavController().navigate(R.id.action_billStatementFragment_to_exportStatementFragment, bundle)
        }
        binding.btnCollectionStatement.setOnClickListener {
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            val bundle = Bundle().apply {
                putInt("bulan", month)
                putInt("tahun", year)
            }
            findNavController().navigate(R.id.action_billStatementFragment_to_collectionStatementFragment, bundle)
        }
    }

    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            legend.apply {
                isEnabled = true
                textColor = Color.WHITE
                form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                setDrawGridLines(false)
                setDrawAxisLine(true)
                granularity = 1f
            }
            axisLeft.apply {
                textColor = Color.WHITE
                setDrawGridLines(true)
                gridColor = Color.argb(40, 255, 255, 255)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
        }
    }

    private fun fetchData() {
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        showLoading(true)
        
        // 1. Fetch Summary
        RetrofitClient.instance.getBillStatement(month, year).enqueue(object : Callback<com.linkbit.billrt.api.BillStatementResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.BillStatementResponse>, response: Response<com.linkbit.billrt.api.BillStatementResponse>) {
                if (isAdded && _binding != null && response.isSuccessful) {
                    response.body()?.let { updateUIExpense(it) }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.BillStatementResponse>, t: Throwable) {}
        })

        // 2. Fetch Rekap Income
        RetrofitClient.instance.getRekapListLunas(month, year).enqueue(object : Callback<com.linkbit.billrt.api.RekapListLunasResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.RekapListLunasResponse>, response: Response<com.linkbit.billrt.api.RekapListLunasResponse>) {
                if (isAdded && _binding != null && response.isSuccessful) {
                    response.body()?.let { updateUIIncomeSummary(it) }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.RekapListLunasResponse>, t: Throwable) {}
        })

        // 3. Fetch List Data
        RetrofitClient.instance.getListLunas(month, year).enqueue(object : Callback<com.linkbit.billrt.api.ListLunasResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.ListLunasResponse>, response: Response<com.linkbit.billrt.api.ListLunasResponse>) {
                if (isAdded && _binding != null && response.isSuccessful) {
                    response.body()?.let {
                        if (it.status) {
                            lunasAdapter.submitList(it.data)
                            binding.tvListLunasTitle.text = "Paid Bills (${it.data.size})"
                        }
                    }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.ListLunasResponse>, t: Throwable) {}
        })

        // 4. Fetch Grafik Periode (Jumlah Pelanggan & Telat)
        val bulanAwal = "$year-01"
        val bulanAkhir = "$year-12"
        RetrofitClient.instance.getGrafikPembayaranPeriode(bulanAwal, bulanAkhir).enqueue(object : Callback<com.linkbit.billrt.api.GrafikPembayaranResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.GrafikPembayaranResponse>, response: Response<com.linkbit.billrt.api.GrafikPembayaranResponse>) {
                if (!isAdded || _binding == null) return
                showLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.status) {
                            val entriesPelanggan = mutableListOf<Entry>()
                            val entriesTelat = mutableListOf<Entry>()
                            val labels = mutableListOf<String>()

                            it.data.forEachIndexed { index, item ->
                                // item.x format: "2026-01"
                                val label = try {
                                    val parts = item.x.split("-")
                                    val m = parts[1].toInt()
                                    val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(
                                        Calendar.getInstance().apply { set(Calendar.MONTH, m - 1) }.time
                                    )
                                    monthName
                                } catch (e: Exception) { item.x }
                                
                                labels.add(label)
                                val valPelanggan = item.yPelanggan?.toFloat() ?: 0f
                                val valTelat = item.yTelat?.toFloat() ?: 0f
                                
                                entriesPelanggan.add(Entry(index.toFloat(), valPelanggan))
                                entriesTelat.add(Entry(index.toFloat(), valTelat))
                            }
                            updateChartPeriode(entriesPelanggan, entriesTelat, labels)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.GrafikPembayaranResponse>, t: Throwable) {
                if (isAdded && _binding != null) showLoading(false)
            }
        })
    }

    private fun updateUIExpense(data: com.linkbit.billrt.api.BillStatementResponse) {
        val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
        binding.tvMonthName.text = monthName
        val year = calendar.get(Calendar.YEAR)
        binding.tvDateRange.text = "1 ${monthName.take(3)} - ${calendar.getActualMaximum(Calendar.DAY_OF_MONTH)} ${monthName.take(3)} $year"
        binding.tvSummaryExpense.text = data.summary.expenseFormat
    }

    private fun updateUIIncomeSummary(data: com.linkbit.billrt.api.RekapListLunasResponse) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
        binding.tvSummaryIncome.text = formatter.format(data.summary.totalNominal).replace("Rp", "Rp ")
        binding.tvTotalLunas.text = "Total: ${data.summary.totalLunas} Pelanggan"
    }

    private fun updateChartPeriode(entriesPelanggan: List<Entry>, entriesTelat: List<Entry>, labels: List<String>) {
        if (entriesPelanggan.isEmpty() && entriesTelat.isEmpty()) {
            binding.lineChart.clear()
            binding.lineChart.invalidate()
            return
        }

        binding.lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < labels.size) labels[index] else ""
            }
        }

        val dataSets = mutableListOf<ILineDataSet>()

        val dataSetPelanggan = LineDataSet(entriesPelanggan, "Total Pelanggan").apply {
            color = Color.parseColor("#4CAF50") // Green
            setCircleColor(Color.parseColor("#4CAF50"))
            valueTextColor = Color.WHITE
            lineWidth = 3f
            circleRadius = 4f
            setDrawCircleHole(true)
            circleHoleColor = Color.parseColor("#4CAF50")
            valueTextSize = 10f
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        }
        dataSets.add(dataSetPelanggan)

        val dataSetTelat = LineDataSet(entriesTelat, "Pelanggan Telat").apply {
            color = Color.parseColor("#F44336") // Red
            setCircleColor(Color.parseColor("#F44336"))
            valueTextColor = Color.WHITE
            lineWidth = 3f
            circleRadius = 4f
            setDrawCircleHole(true)
            circleHoleColor = Color.parseColor("#F44336")
            valueTextSize = 10f
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        }
        dataSets.add(dataSetTelat)

        val lineData = LineData(dataSets)
        lineData.setValueFormatter(object : ValueFormatter() {
            override fun getPointLabel(entry: Entry?): String {
                return entry?.y?.toInt().toString()
            }
        })

        binding.lineChart.data = lineData
        binding.lineChart.animateX(500)
        binding.lineChart.invalidate()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnPrevMonth.isEnabled = !isLoading
        binding.btnNextMonth.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
