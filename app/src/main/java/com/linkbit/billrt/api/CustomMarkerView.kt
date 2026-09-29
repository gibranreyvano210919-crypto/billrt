package com.linkbit.billrt.api

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.linkbit.billrt.R
import java.text.SimpleDateFormat
import java.util.Locale

class CustomMarkerView(context: Context, layoutResource: Int, private val dataList: List<GrafikPembayaranItem>) : MarkerView(context, layoutResource) {

    private val tvDate: TextView = findViewById(R.id.tvDate)
    private val tvTepat: TextView = findViewById(R.id.tvTepat)
    private val tvTelat: TextView = findViewById(R.id.tvTelat)
    private val tvTotal: TextView = findViewById(R.id.tvTotal)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val index = e?.x?.toInt() ?: -1
        if (index >= 0 && index < dataList.size) {
            val item = dataList[index]
            
            // Format date
            val displayDate = try {
                val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdfOut = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                val date = sdfIn.parse(item.x)
                date?.let { sdfOut.format(it) } ?: item.x
            } catch (ex: Exception) {
                item.x
            }

            val total = item.y?.toInt() ?: 0
            val telat = item.yTelat ?: 0
            val tepat = total - telat

            tvDate.text = displayDate
            tvTepat.text = "Tepat Waktu: $tepat"
            tvTelat.text = "Telat: $telat"
            tvTotal.text = "Total: $total Pelanggan"
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }
}