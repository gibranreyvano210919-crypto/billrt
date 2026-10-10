package com.linkbit.billrt

import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import com.linkbit.billrt.adapter.DetailCollectionAdapter
import com.linkbit.billrt.adapter.DetailListItem
import com.linkbit.billrt.api.GrafikPembayaranResponse
import com.linkbit.billrt.api.RetrofitClient
import com.linkbit.billrt.databinding.FragmentDetailCollectionBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class SundayXAxisRenderer(viewPortHandler: ViewPortHandler, xAxis: XAxis, trans: Transformer) : XAxisRenderer(viewPortHandler, xAxis, trans) {
    override fun drawLabel(c: Canvas?, formattedLabel: String?, x: Float, y: Float, anchor: MPPointF?, angleDegrees: Float) {
        val originalColor = mAxisLabelPaint.color
        if (formattedLabel?.contains("(M)") == true) {
            mAxisLabelPaint.color = Color.RED
        } else {
            mAxisLabelPaint.color = mXAxis.textColor
        }
        super.drawLabel(c, formattedLabel, x, y, anchor, angleDegrees)
        mAxisLabelPaint.color = originalColor
    }
}

class DetailCollectionFragment : BaseFragment() {

    private var _binding: FragmentDetailCollectionBinding? = null
    private val binding get() = _binding!!

    private val args: DetailCollectionFragmentArgs by navArgs()
    private lateinit var detailAdapter: DetailCollectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyWindowInsets(binding.appBarLayout)

        setupToolbar()
        setupRecyclerView()
        setupUI()
        setupBarChart()
        fetchData()
        fetchGrafikData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        detailAdapter = DetailCollectionAdapter()
        binding.rvDetailCollection.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = detailAdapter
        }
    }

    private fun setupUI() {
        binding.tvCollectorName.text = args.namaCollector
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, args.bulan - 1)
        calendar.set(Calendar.YEAR, args.tahun)
        val monthName = android.text.format.DateFormat.format("MMMM yyyy", calendar.time)
        binding.tvPeriode.text = "Periode: $monthName"
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            marker = null
            
            legend.apply {
                isEnabled = true
                textColor = Color.WHITE
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                setDrawGridLines(false)
                granularity = 1f
            }

            // Set custom renderer to handle Sunday color
            setXAxisRenderer(SundayXAxisRenderer(viewPortHandler, xAxis, getTransformer(YAxis.AxisDependency.LEFT)))

            axisLeft.apply {
                textColor = Color.WHITE
                setDrawGridLines(true)
                gridColor = Color.parseColor("#33FFFFFF")
                axisMinimum = 0f
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()
                    }
                }
            }

            axisRight.isEnabled = false
        }
    }

    private fun fetchData() {
        binding.progressBar.visibility = View.VISIBLE
        
        RetrofitClient.instance.getListPencatatDetail(args.idUserPencatat, args.bulan, args.tahun)
            .enqueue(object : Callback<com.linkbit.billrt.api.ListPencatatDetailResponse> {
                override fun onResponse(
                    call: Call<com.linkbit.billrt.api.ListPencatatDetailResponse>,
                    response: Response<com.linkbit.billrt.api.ListPencatatDetailResponse>
                ) {
                    if (!isAdded || _binding == null) return
                    binding.progressBar.visibility = View.GONE
                    
                    if (response.isSuccessful) {
                        response.body()?.let { res ->
                            if (res.status && res.data != null) {
                                val listWithHeaders = mutableListOf<DetailListItem>()
                                var totalNominalGlobal = 0.0
                                var totalPelangganGlobal = 0
                                var totalBaruGlobal = 0
                                
                                res.data.forEach { group ->
                                    // 1. Add Header for Wilayah
                                    listWithHeaders.add(
                                        DetailListItem.Header(
                                            wilayah = group.wilayah,
                                            totalPelanggan = group.detailList.size,
                                            totalNominal = group.totalNominal,
                                            baru = group.pelangganBaru,
                                            lama = group.pelangganLama
                                        )
                                    )
                                    
                                    // 2. Add Items
                                    group.detailList.forEach { item ->
                                        listWithHeaders.add(DetailListItem.Item(item))
                                        totalNominalGlobal += item.nominal
                                        totalPelangganGlobal++
                                    }
                                    
                                    totalBaruGlobal += group.pelangganBaru
                                }
                                
                                detailAdapter.submitList(listWithHeaders)
                                updateSummary(totalNominalGlobal, totalPelangganGlobal, totalBaruGlobal)
                            } else {
                                detailAdapter.submitList(emptyList())
                                updateSummary(0.0, 0, 0)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<com.linkbit.billrt.api.ListPencatatDetailResponse>, t: Throwable) {
                    if (!isAdded || _binding == null) return
                    binding.progressBar.visibility = View.GONE
                }
            })
    }

    private fun updateSummary(totalNominal: Double, totalPelanggan: Int, totalBaru: Int) {
        val locale = Locale.forLanguageTag("id-ID")
        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            maximumFractionDigits = 0
        }
        
        binding.tvTotalNominal.text = formatter.format(totalNominal).replace("Rp", "Rp ")
        binding.tvTotalPelanggan.text = totalPelanggan.toString()
        binding.tvListTitle.text = "Daftar Pelanggan ($totalPelanggan) • Baru: $totalBaru"
    }

    private fun fetchGrafikData() {
        val bulanStr = String.format("%02d", args.bulan)
        RetrofitClient.instance.getGrafikHarianDetail(args.idUserPencatat, bulanStr, args.tahun)
            .enqueue(object : Callback<GrafikPembayaranResponse> {
                override fun onResponse(call: Call<GrafikPembayaranResponse>, response: Response<GrafikPembayaranResponse>) {
                    if (!isAdded || _binding == null) return
                    if (response.isSuccessful) {
                        response.body()?.let { res ->
                            if (res.status && res.data.isNotEmpty()) {
                                binding.cardGrafik.visibility = View.VISIBLE
                                updateBarChart(res.data)
                            } else {
                                binding.cardGrafik.visibility = View.GONE
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<GrafikPembayaranResponse>, t: Throwable) {
                    if (!isAdded || _binding == null) return
                    binding.cardGrafik.visibility = View.GONE
                }
            })
    }

    private fun updateBarChart(data: List<com.linkbit.billrt.api.GrafikPembayaranItem>) {
        val entriesTepat = ArrayList<BarEntry>()
        val entriesTelat = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        
        val sundayIndices = ArrayList<Int>()

        data.forEachIndexed { index, item ->
            val total = item.y ?: 0f
            val telat = item.yTelat?.toFloat() ?: 0f
            val tepatWaktu = total - telat
            
            entriesTepat.add(BarEntry(index.toFloat(), tepatWaktu))
            entriesTelat.add(BarEntry(index.toFloat(), telat))
            
            val day = item.x.split("-").lastOrNull() ?: ""
            if (item.isSunday == 1) {
                labels.add("$day(M)") 
                sundayIndices.add(index)
            } else {
                labels.add(day)
            }
        }

        val barValueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value > 0) value.toInt().toString() else ""
            }
        }

        val dataSetTepat = BarDataSet(entriesTepat, "Tepat Waktu").apply {
            color = Color.parseColor("#4CAF50") // Green
            valueTextColor = Color.WHITE
            valueTextSize = 10f
            setDrawValues(true)
            valueFormatter = barValueFormatter
        }

        val dataSetTelat = BarDataSet(entriesTelat, "Telat").apply {
            color = Color.RED // Red
            valueTextColor = Color.WHITE
            valueTextSize = 10f
            setDrawValues(true)
            valueFormatter = barValueFormatter
        }

        val groupSpace = 0.3f
        val barSpace = 0.05f
        val barWidth = 0.3f

        val barData = BarData(dataSetTepat, dataSetTelat)
        barData.barWidth = barWidth

        binding.barChart.apply {
            this.data = barData
            
            xAxis.apply {
                removeAllLimitLines()
                sundayIndices.forEach { idx ->
                    val ll = LimitLine(idx.toFloat(), "")
                    ll.lineColor = Color.RED
                    ll.lineWidth = 0.5f
                    ll.enableDashedLine(10f, 10f, 0f)
                    addLimitLine(ll)
                }

                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < labels.size) labels[index] else ""
                    }
                }
                axisMinimum = 0f
                axisMaximum = data.size.toFloat()
                granularity = 1f
                setCenterAxisLabels(true)
            }

            groupBars(0f, groupSpace, barSpace)
            
            setVisibleXRangeMaximum(10f)
            moveViewToX(0f)

            invalidate()
            animateY(1000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
