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
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.linkbit.billrt.adapter.StatementLunasGroupedAdapter
import com.linkbit.billrt.api.RetrofitClient
import com.linkbit.billrt.databinding.FragmentExportCollectionBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExportCollectionFragment : BaseFragment() {

    private var _binding: FragmentExportCollectionBinding? = null
    private val binding get() = _binding!!

    private val args: ExportCollectionFragmentArgs by navArgs()
    private val calendar: Calendar = Calendar.getInstance()
    
    private lateinit var lunasAdapter: StatementLunasGroupedAdapter
    private var allGroupedLunasData: List<com.linkbit.billrt.api.GroupedLunasTanggal> = emptyList()
    private var allPencatatData: List<com.linkbit.billrt.api.GrafikPembayaranPencatatItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportCollectionBinding.inflate(inflater, container, false)
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
        setupBarChart()
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

    private fun setupBarChart() {
        binding.barChartPencatat.apply {
            description.isEnabled = false
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setMaxVisibleValueCount(60)
            setPinchZoom(false)
            setTouchEnabled(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                setDrawGridLines(false)
                granularity = 1f
                labelCount = 7
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                textColor = Color.WHITE
                gridColor = Color.argb(40, 255, 255, 255)
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun setupListeners() {
        binding.btnPrevDay.setOnClickListener {
            val oldMonth = calendar.get(Calendar.MONTH)
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            if (calendar.get(Calendar.MONTH) != oldMonth) {
                fetchData()
            } else {
                updateUIForSelectedDate()
            }
        }
        
        binding.btnNextDay.setOnClickListener {
            val oldMonth = calendar.get(Calendar.MONTH)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            if (calendar.get(Calendar.MONTH) != oldMonth) {
                fetchData()
            } else {
                updateUIForSelectedDate()
            }
        }
    }

    private fun fetchData() {
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        // Fetch Summary Bulanan
        RetrofitClient.instance.getBillStatement(month, year).enqueue(object : Callback<com.linkbit.billrt.api.BillStatementResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.BillStatementResponse>, response: Response<com.linkbit.billrt.api.BillStatementResponse>) {
                if (isAdded && _binding != null && response.isSuccessful) {
                    response.body()?.let { binding.tvSummaryExpense.text = it.summary.expenseFormat }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.BillStatementResponse>, t: Throwable) {}
        })

        // Fetch Grouped List Data (list harian)
        RetrofitClient.instance.getListLunasTanggal(month, year).enqueue(object : Callback<com.linkbit.billrt.api.ListLunasTanggalResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.ListLunasTanggalResponse>, response: Response<com.linkbit.billrt.api.ListLunasTanggalResponse>) {
                if (isAdded && _binding != null && response.isSuccessful) {
                    response.body()?.let {
                        if (it.status) {
                            allGroupedLunasData = it.data
                            updateUIForSelectedDate()
                        }
                    }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.ListLunasTanggalResponse>, t: Throwable) {}
        })

        // Fetch Grafik Pencatat Harian
        val startDate = String.format("%04d-%02d-%02d", year, month, 1)
        val endDate = String.format("%04d-%02d-%02d", year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

        RetrofitClient.instance.getGrafikPembayaranPencatatHarian(startDate, endDate).enqueue(object : Callback<com.linkbit.billrt.api.GrafikPembayaranPencatatResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.api.GrafikPembayaranPencatatResponse>, response: Response<com.linkbit.billrt.api.GrafikPembayaranPencatatResponse>) {
                if (isAdded && _binding != null && response.isSuccessful) {
                    response.body()?.let {
                        if (it.status) {
                            allPencatatData = it.data
                            updateBarChartForSelectedDate()
                        }
                    }
                }
            }
            override fun onFailure(call: Call<com.linkbit.billrt.api.GrafikPembayaranPencatatResponse>, t: Throwable) {}
        })
    }

    private fun updateUIForSelectedDate() {
        val locale = Locale("id", "ID")
        binding.tvMonthName.text = SimpleDateFormat("MMMM yyyy", locale).format(calendar.time)
        binding.tvDateRange.text = SimpleDateFormat("EEEE, dd MMM yyyy", locale).format(calendar.time)
        
        filterDataBySelectedDate()
        updateBarChartForSelectedDate()
    }

    private fun filterDataBySelectedDate() {
        val selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val dayGroup = allGroupedLunasData.find { it.tanggal == selectedDateStr }
        val filteredList = dayGroup?.list ?: emptyList()
        val totalHarian = dayGroup?.totalHarian ?: 0.0
        
        lunasAdapter.submitList(filteredList)
        binding.tvListLunasTitle.text = "Daftar Pembayaran (${filteredList.size})"
        
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
        binding.tvSummaryIncome.text = formatter.format(totalHarian).replace("Rp", "Rp ")
        binding.tvTotalLunas.text = "Total: ${filteredList.size} Pelanggan"
    }

    private fun updateBarChartForSelectedDate() {
        val selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val dayData = allPencatatData.find { it.x == selectedDateStr }

        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        if (dayData == null || dayData.usersData.isEmpty()) {
            binding.barChartPencatat.clear()
            binding.barChartPencatat.invalidate()
            return
        }

        val sortedUsers = dayData.usersData.sortedByDescending { it.totalPelanggan }
        sortedUsers.take(8).forEachIndexed { index, user ->
            entries.add(BarEntry(index.toFloat(), user.totalPelanggan.toFloat()))
            labels.add(user.namaPencatat)
        }

        val dataSet = BarDataSet(entries, "Daily Collection").apply {
            colors = listOf(
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#E0FFFFFF"),
                Color.parseColor("#C0FFFFFF"),
                Color.parseColor("#A0FFFFFF")
            )
            valueTextColor = Color.WHITE
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = value.toInt().toString()
            }
        }

        binding.barChartPencatat.apply {
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < labels.size) {
                        val label = labels[index]
                        if (label.length > 8) label.take(6) + ".." else label
                    } else ""
                }
            }
            
            data = BarData(dataSet).apply { barWidth = 0.5f }
            animateY(500)
            invalidate()
        }
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
        canvas.drawText("COLLECTION REPORT", margin, yPos, titlePaint)
        
        yPos += 25f
        paint.textSize = 12f
        val displayDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(calendar.time)
        canvas.drawText("Tanggal: $displayDate", margin, yPos, paint)

        yPos += 20f
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
        canvas.drawText("Total Setoran Harian: ${formatter.format(dayGroup?.totalHarian ?: 0.0)}", margin, yPos, paint)

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

        val fileName = "Collection_${selectedDateStr.replace("-", "")}.pdf"
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
        startActivity(Intent.createChooser(intent, "Bagikan Collection PDF"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
