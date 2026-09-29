package com.linkbit.billrt

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.linkbit.billrt.adapter.StatementLunasGroupedAdapter
import com.linkbit.billrt.api.RetrofitClient
import com.linkbit.billrt.databinding.FragmentExportStatementBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExportStatementFragment : BaseFragment() {

    private var _binding: FragmentExportStatementBinding? = null
    private val binding get() = _binding!!

    private val args: ExportStatementFragmentArgs by navArgs()
    private val calendar: Calendar = Calendar.getInstance()
    
    private lateinit var lunasAdapter: StatementLunasGroupedAdapter
    private var allGroupedLunasData: List<com.linkbit.billrt.api.GroupedLunasTanggal> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportStatementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets(binding.appBarLayout)

        // Set periode awal dari navigasi
        calendar.set(Calendar.YEAR, args.tahun)
        calendar.set(Calendar.MONTH, args.bulan - 1)
        
        // Default ke hari ini jika bulan yang dipilih adalah bulan sekarang
        val today = Calendar.getInstance()
        if (today.get(Calendar.YEAR) == args.tahun && today.get(Calendar.MONTH) == args.bulan - 1) {
            calendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
        } else {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
        }

        setupToolbar()
        setupRecyclerView()
        setupChart()
        setupListeners()
        
        fetchData()

        binding.btnGeneratePdf.setOnClickListener {
            generatePdf()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() {
        lunasAdapter = StatementLunasGroupedAdapter()
        binding.rvListLunas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = lunasAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                setDrawGridLines(false)
                granularity = 1f
                axisMinimum = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String = value.toInt().toString()
                }
            }
            
            axisLeft.apply {
                textColor = Color.WHITE
                setDrawGridLines(true)
                gridColor = Color.argb(40, 255, 255, 255)
                granularity = 1f
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            legend.apply {
                isEnabled = true
                textColor = Color.WHITE
                form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
            }

            // Klik titik grafik untuk melihat nominal harian
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        calendar.set(Calendar.DAY_OF_MONTH, it.x.toInt())
                        updateDateUI()
                        filterDataBySelectedDate()
                    }
                }
                override fun onNothingSelected() {}
            })
        }
    }

    private fun setupListeners() {
        binding.btnPrevDay.setOnClickListener {
            val oldMonth = calendar.get(Calendar.MONTH)
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            if (calendar.get(Calendar.MONTH) != oldMonth) fetchData()
            else {
                updateDateUI()
                filterDataBySelectedDate()
                highlightChartDate()
            }
        }
        
        binding.btnNextDay.setOnClickListener {
            val oldMonth = calendar.get(Calendar.MONTH)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            if (calendar.get(Calendar.MONTH) != oldMonth) fetchData()
            else {
                updateDateUI()
                filterDataBySelectedDate()
                highlightChartDate()
            }
        }
    }

    private fun fetchData() {
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        // 1. Fetch Summary Bulanan (Expense)
        RetrofitClient.instance.getBillStatement(month, year).enqueue(object : Callback<com.linkbit.billrt.api.BillStatementResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.BillStatementResponse>, response: Response<com.linkbit.billrt.api.BillStatementResponse>) {
                if (isAdded && _binding != null && response.isSuccessful) {
                    response.body()?.let { binding.tvSummaryExpense.text = it.summary.expenseFormat }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.BillStatementResponse>, t: Throwable) {}
        })

        // 2. Fetch Grouped List Data (listlunastanggal)
        RetrofitClient.instance.getListLunasTanggal(month, year).enqueue(object : Callback<com.linkbit.billrt.api.ListLunasTanggalResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.ListLunasTanggalResponse>, response: Response<com.linkbit.billrt.api.ListLunasTanggalResponse>) {
                if (isAdded && _binding != null && response.isSuccessful) {
                    response.body()?.let {
                        if (it.status) {
                            allGroupedLunasData = it.data
                            updateDateUI()
                            filterDataBySelectedDate()
                        }
                    }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.ListLunasTanggalResponse>, t: Throwable) {}
        })

        // 3. Fetch Grafik Pembayaran Tanggal (Volume Pelanggan)
        RetrofitClient.instance.getGrafikPembayaranTanggal(month, year).enqueue(object : Callback<com.linkbit.billrt.api.GrafikPembayaranResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.GrafikPembayaranResponse>, response: Response<com.linkbit.billrt.api.GrafikPembayaranResponse>) {
                if (isAdded && _binding != null && response.isSuccessful) {
                    response.body()?.let {
                        if (it.status) {
                            val entriesTepat = mutableListOf<Entry>()
                            val entriesTelat = mutableListOf<Entry>()
                            
                            it.data.forEach { item ->
                                val day = item.label?.toFloat() ?: (item.x.split("-").lastOrNull()?.toFloat() ?: 0f)
                                if (day > 0) {
                                    val valTepat = (item.yPelangganTepat ?: 0).toFloat()
                                    val valTelat = (item.yPelangganTelat ?: 0).toFloat()
                                    
                                    if (valTepat > 0) entriesTepat.add(Entry(day, valTepat))
                                    if (valTelat > 0) entriesTelat.add(Entry(day, valTelat))
                                }
                            }
                            updateChartSplit(entriesTepat, entriesTelat)
                            highlightChartDate()
                        }
                    }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.GrafikPembayaranResponse>, t: Throwable) {}
        })
    }

    private fun updateDateUI() {
        val locale = Locale("id", "ID")
        binding.tvMonthName.text = SimpleDateFormat("MMMM yyyy", locale).format(calendar.time)
        binding.tvDateRange.text = SimpleDateFormat("EEEE, dd MMM yyyy", locale).format(calendar.time)
    }

    private fun filterDataBySelectedDate() {
        val selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        // Temukan grup data untuk tanggal yang dipilih
        val dayGroup = allGroupedLunasData.find { it.tanggal == selectedDateStr }
        val filteredList = dayGroup?.list ?: emptyList()
        val totalHarian = dayGroup?.totalHarian ?: 0.0
        
        lunasAdapter.submitList(filteredList)
        binding.tvListLunasTitle.text = "Daftar Pembayaran (${filteredList.size})"
        
        // Update Nominal Rupiah di kartu ringkasan
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
        binding.tvSummaryIncome.text = formatter.format(totalHarian).replace("Rp", "Rp ")
        binding.tvTotalLunas.text = "Total: ${filteredList.size} Pelanggan"
    }

    private fun highlightChartDate() {
        if (binding.lineChart.data == null) return
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH).toFloat()
        if (binding.lineChart.data.dataSetCount > 0) {
            binding.lineChart.highlightValue(Highlight(currentDay, 0f, 0), false)
        }
    }

    private fun updateChartSplit(entriesTepat: List<Entry>, entriesTelat: List<Entry>) {
        if (entriesTepat.isEmpty() && entriesTelat.isEmpty()) {
            binding.lineChart.clear()
            return
        }
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH).toFloat()
        binding.lineChart.xAxis.axisMaximum = maxDays

        val dataSets = mutableListOf<ILineDataSet>()

        if (entriesTepat.isNotEmpty()) {
            val dataSetTepat = LineDataSet(entriesTepat, "Tepat Waktu").apply {
                color = Color.parseColor("#4CAF50") // Green
                setCircleColor(Color.parseColor("#4CAF50"))
                valueTextColor = Color.WHITE
                lineWidth = 2f
                circleRadius = 3f
                setDrawCircleHole(true)
                circleHoleColor = Color.parseColor("#4CAF50")
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawHorizontalHighlightIndicator(false)
                setHighlightLineWidth(2f)
                highLightColor = Color.YELLOW
                setDrawHighlightIndicators(true)
            }
            dataSets.add(dataSetTepat)
        }

        if (entriesTelat.isNotEmpty()) {
            val dataSetTelat = LineDataSet(entriesTelat, "Telat").apply {
                color = Color.parseColor("#F44336") // Red
                setCircleColor(Color.parseColor("#F44336"))
                valueTextColor = Color.WHITE
                lineWidth = 2f
                circleRadius = 3f
                setDrawCircleHole(true)
                circleHoleColor = Color.parseColor("#F44336")
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawHorizontalHighlightIndicator(false)
                setHighlightLineWidth(2f)
                highLightColor = Color.YELLOW
                setDrawHighlightIndicators(true)
            }
            dataSets.add(dataSetTelat)
        }

        binding.lineChart.data = LineData(dataSets)
        binding.lineChart.invalidate()
    }

    private fun generatePdf() {
        val selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val dayGroup = allGroupedLunasData.find { it.tanggal == selectedDateStr }
        val listData = dayGroup?.list ?: emptyList()

        if (listData.isEmpty()) {
            Toast.makeText(context, "Tidak ada data untuk tanggal ini", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var yPos = 40f
        val margin = 40f

        // Header
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 18f
        canvas.drawText("BILLING STATEMENT", margin, yPos, titlePaint)
        
        yPos += 25f
        paint.textSize = 12f
        val displayDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(calendar.time)
        canvas.drawText("Tanggal: $displayDate", margin, yPos, paint)

        yPos += 20f
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
        canvas.drawText("Total Pendapatan Harian: ${formatter.format(dayGroup?.totalHarian ?: 0.0)}", margin, yPos, paint)

        yPos += 30f
        // Table Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("No", margin, yPos, paint)
        canvas.drawText("Pelanggan", margin + 30, yPos, paint)
        canvas.drawText("Nominal", margin + 200, yPos, paint)
        canvas.drawText("Collector", margin + 320, yPos, paint)
        canvas.drawText("Status", margin + 450, yPos, paint)

        yPos += 10f
        canvas.drawLine(margin, yPos, 555f, yPos, paint)
        
        yPos += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f

        listData.forEachIndexed { index, item ->
            if (yPos > 780) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 40f
            }

            canvas.drawText("${index + 1}", margin, yPos, paint)
            
            val nama = if (item.nama.length > 25) item.nama.substring(0, 22) + ".." else item.nama
            canvas.drawText(nama, margin + 30, yPos, paint)
            
            canvas.drawText(formatter.format(item.nominal), margin + 200, yPos, paint)
            
            val pencatat = item.namaPencatat ?: "System"
            canvas.drawText(pencatat, margin + 320, yPos, paint)
            
            canvas.drawText(item.statusPeriode, margin + 450, yPos, paint)

            yPos += 20f
        }

        pdfDocument.finishPage(page)

        val fileName = "Statement_${selectedDateStr.replace("-", "")}.pdf"
        val file = File(requireContext().getExternalFilesDir(null), fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            sharePdf(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun sharePdf(file: File) {
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Bagikan Statement PDF"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
