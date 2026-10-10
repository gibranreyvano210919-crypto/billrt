package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentRiwayatCatatanBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormatSymbols
import java.util.*

class RiwayatCatatanFragment : BaseFragment() {

    private var _binding: FragmentRiwayatCatatanBinding? = null
    private val binding get() = _binding!!
    private lateinit var riwayatCatatanAdapter: RiwayatCatatanAdapter
    
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRiwayatCatatanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        updatePeriodeUI()
        fetchRiwayatCatatan()
    }

    private fun setupRecyclerView() {
        riwayatCatatanAdapter = RiwayatCatatanAdapter(emptyList())
        binding.rvRiwayatCatatan.layoutManager = LinearLayoutManager(context)
        binding.rvRiwayatCatatan.adapter = riwayatCatatanAdapter
    }

    private fun setupListeners() {
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
                fetchRiwayatCatatan()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updatePeriodeUI() {
        val monthName = DateFormatSymbols().months[currentMonth - 1]
        binding.tvFilterPeriode.text = "$monthName $currentYear"
    }

    private fun fetchRiwayatCatatan() {
        binding.progressBarRiwayat.visibility = View.VISIBLE
        binding.tvEmptyRiwayatCatatan.visibility = View.GONE
        binding.cardSummary.visibility = View.GONE

        apiService.getRiwayatCatatan("", currentMonth, currentYear).enqueue(object : Callback<RiwayatCatatanResponse> {
            override fun onResponse(call: Call<RiwayatCatatanResponse>, response: Response<RiwayatCatatanResponse>) {
                if (!isAdded || _binding == null) return
                binding.progressBarRiwayat.visibility = View.GONE

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status) {
                        binding.cardSummary.visibility = View.VISIBLE
                        binding.tvTotalCatatan.text = "Total Catatan: ${body.totalGlobal}"
                        binding.tvTotalDuplikat.text = "Duplikat: ${body.totalDuplikatGlobal}"

                        val allItems = body.data.flatMap { group ->
                            group.list.map { item ->
                                item.tanggalCatat = group.tanggalCatat
                                item
                            }
                        }

                        if (allItems.isNotEmpty()) {
                            riwayatCatatanAdapter.updateData(allItems)
                        } else {
                            binding.tvEmptyRiwayatCatatan.visibility = View.VISIBLE
                        }
                    } else {
                        handleFailure(body?.message ?: "Gagal memuat data")
                    }
                } else {
                    handleFailure()
                }
            }

            override fun onFailure(call: Call<RiwayatCatatanResponse>, t: Throwable) {
                handleFailure(t.message)
            }
        })
    }

    private fun handleFailure(message: String? = "Gagal mengambil riwayat") {
        if (!isAdded || _binding == null) return
        binding.progressBarRiwayat.visibility = View.GONE
        binding.tvEmptyRiwayatCatatan.visibility = View.VISIBLE
        riwayatCatatanAdapter.updateData(emptyList())
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
