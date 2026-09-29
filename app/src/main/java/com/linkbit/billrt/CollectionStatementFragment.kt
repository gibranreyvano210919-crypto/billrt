package com.linkbit.billrt

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.linkbit.billrt.adapter.CollectionPencatatAdapter
import com.linkbit.billrt.api.RetrofitClient
import com.linkbit.billrt.databinding.FragmentCollectionStatementBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CollectionStatementFragment : BaseFragment() {

    private var _binding: FragmentCollectionStatementBinding? = null
    private val binding get() = _binding!!

    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var pencatatAdapter: CollectionPencatatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionStatementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyWindowInsets(binding.appBarLayout)
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        setupBarChartPencatat()
        
        // Load initial date from arguments if available
        arguments?.let {
            val m = it.getInt("bulan", -1)
            val y = it.getInt("tahun", -1)
            if (m != -1 && y != -1) {
                calendar.set(Calendar.MONTH, m - 1)
                calendar.set(Calendar.YEAR, y)
            }
        }
        
        fetchData()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        pencatatAdapter = CollectionPencatatAdapter { item ->
            val action = CollectionStatementFragmentDirections.actionCollectionStatementFragmentToDetailCollectionFragment(
                idUserPencatat = item.idUserPencatat,
                namaCollector = item.namaPencatat,
                bulan = calendar.get(Calendar.MONTH) + 1,
                tahun = calendar.get(Calendar.YEAR)
            )
            findNavController().navigate(action)
        }
        binding.rvListLunas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pencatatAdapter
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
        binding.btnExportCollection.setOnClickListener {
            val action = CollectionStatementFragmentDirections.actionCollectionStatementFragmentToExportCollectionFragment(
                bulan = calendar.get(Calendar.MONTH) + 1,
                tahun = calendar.get(Calendar.YEAR)
            )
            findNavController().navigate(action)
        }
    }

    private fun setupBarChartPencatat() {
        binding.barChartPencatat.apply {
            description.isEnabled = false
            setDrawBarShadow(false)
            setDrawValueAboveBar(true) 
            setMaxVisibleValueCount(60)
            setPinchZoom(false)
            setTouchEnabled(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = 7
                textColor = Color.WHITE
                // Adjusting to center the group
                setCenterAxisLabels(true)
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                textColor = Color.WHITE
                gridColor = Color.argb(40, 255, 255, 255)
            }
            
            axisRight.isEnabled = false
            
            legend.apply {
                isEnabled = true
                textColor = Color.WHITE
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(true)
            }
        }
    }

    private fun fetchData() {
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        showLoading(true)
        
        RetrofitClient.instance.getBillStatement(month, year).enqueue(object : Callback<com.linkbit.billrt.api.BillStatementResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.BillStatementResponse>, response: Response<com.linkbit.billrt.api.BillStatementResponse>) {
                if (isAdded && _binding != null && response.isSuccessful) {
                    response.body()?.let { updateUISummary(it) }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.BillStatementResponse>, t: Throwable) {}
        })

        RetrofitClient.instance.getRekapListLunas(month, year).enqueue(object : Callback<com.linkbit.billrt.api.RekapListLunasResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.RekapListLunasResponse>, response: Response<com.linkbit.billrt.api.RekapListLunasResponse>) {
                if (isAdded && _binding != null && response.isSuccessful) {
                    response.body()?.let { updateUIIncomeSummary(it) }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.RekapListLunasResponse>, t: Throwable) {}
        })

        RetrofitClient.instance.getListPencatat(month, year).enqueue(object : Callback<com.linkbit.billrt.api.ListPencatatResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.ListPencatatResponse>, response: Response<com.linkbit.billrt.api.ListPencatatResponse>) {
                if (isAdded && _binding != null && response.isSuccessful) {
                    response.body()?.let {
                        if (it.status) {
                            pencatatAdapter.submitList(it.data)
                            binding.tvListLunasTitle.text = "Collector Performance (${it.data.size})"
                        }
                    }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.ListPencatatResponse>, t: Throwable) {}
        })

        val bulanAwal = "$year-01"
        val bulanAkhir = "$year-12"
        
        // Bar Chart for Collector Performance
        RetrofitClient.instance.getGrafikPembayaranPencatat(bulanAwal, bulanAkhir).enqueue(object : Callback<com.linkbit.billrt.api.GrafikPembayaranPencatatResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.GrafikPembayaranPencatatResponse>, response: Response<com.linkbit.billrt.api.GrafikPembayaranPencatatResponse>) {
                if (!isAdded || _binding == null) return
                showLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.status) {
                            updateBarChartPencatat(it.data)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.GrafikPembayaranPencatatResponse>, t: Throwable) {
                if (isAdded && _binding != null) showLoading(false)
            }
        })
    }

    private fun updateBarChartPencatat(data: List<com.linkbit.billrt.api.GrafikPembayaranPencatatItem>) {
        val selectedMonthStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        val monthData = data.find { it.x == selectedMonthStr }

        if (monthData == null || monthData.usersData.isEmpty()) {
            binding.barChartPencatat.clear()
            binding.barChartPencatat.invalidate()
            return
        }

        val entriesTepat = mutableListOf<BarEntry>()
        val entriesTelat = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        val sortedUsers = monthData.usersData.sortedByDescending { it.totalPelanggan }
        
        sortedUsers.take(6).forEachIndexed { index, user ->
            entriesTepat.add(BarEntry(index.toFloat(), user.tepatWaktu.toFloat()))
            entriesTelat.add(BarEntry(index.toFloat(), user.totalTelat.toFloat()))
            labels.add(user.namaPencatat)
        }

        val dataSetTepat = BarDataSet(entriesTepat, "Tepat Waktu").apply {
            color = Color.parseColor("#4CAF50")
            valueTextColor = Color.WHITE
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = if (value > 0) value.toInt().toString() else ""
            }
        }

        val dataSetTelat = BarDataSet(entriesTelat, "Telat Bayar").apply {
            color = Color.parseColor("#F44336")
            valueTextColor = Color.WHITE
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = if (value > 0) value.toInt().toString() else ""
            }
        }

        val groupSpace = 0.3f
        val barSpace = 0.05f
        val barWidth = 0.3f
        // (barWidth + barSpace) * 2 + groupSpace = 1.0

        val barData = BarData(dataSetTepat, dataSetTelat)
        barData.barWidth = barWidth

        binding.barChartPencatat.apply {
            this.data = barData
            
            xAxis.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < labels.size) {
                            val label = labels[index]
                            if (label.length > 8) label.take(6) + ".." else label
                        } else ""
                    }
                }
                axisMinimum = 0f
                axisMaximum = labels.size.toFloat()
            }

            groupBars(0f, groupSpace, barSpace)
            animateY(1000)
            invalidate()
        }
    }

    private fun updateUISummary(data: com.linkbit.billrt.api.BillStatementResponse) {
        val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
        binding.tvMonthName.text = monthName
        val year = calendar.get(Calendar.YEAR)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val shortMonth = monthName.take(3)
        binding.tvDateRange.text = "1 $shortMonth - $daysInMonth $shortMonth $year"
        binding.tvSummaryExpense.text = data.summary.expenseFormat
    }

    private fun updateUIIncomeSummary(data: com.linkbit.billrt.api.RekapListLunasResponse) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
        binding.tvSummaryIncome.text = formatter.format(data.summary.totalNominal).replace("Rp", "Rp ")
        binding.tvTotalLunas.text = "Total: ${data.summary.totalLunas} Collections"
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
