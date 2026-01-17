package com.linkbit.billrt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.linkbit.billrt.databinding.FragmentPelangganBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class PelangganFragment : Fragment() {

    private var _binding: FragmentPelangganBinding? = null
    private val binding get() = _binding!!
    private val apiService: ApiService by lazy { ApiConfig.getApiService() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFilterSpinners()
        setupFilterListeners()
        setupDashboardCardListeners()
        binding.btnTambahPelanggan.setOnClickListener { 
            openFragment(TambahPelangganWizardFragment())
        }
        fetchDashboardData()
    }

    private fun setupFilterSpinners() {
        val monthNames = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, monthNames)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonthPelanggan.adapter = monthAdapter

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2020..currentYear).toList().map { it.toString() }
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYearPelanggan.adapter = yearAdapter

        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        binding.spinnerMonthPelanggan.setSelection(currentMonth)
        binding.spinnerYearPelanggan.setSelection(years.indexOf(currentYear.toString()))
    }

    private fun setupFilterListeners() {
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fetchDashboardData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.spinnerMonthPelanggan.onItemSelectedListener = listener
        binding.spinnerYearPelanggan.onItemSelectedListener = listener
    }

    private fun setupDashboardCardListeners() {
        binding.cardTotalPelanggan.setOnClickListener { 
            val selectedMonth = binding.spinnerMonthPelanggan.selectedItemPosition + 1
            val selectedYear = binding.spinnerYearPelanggan.selectedItem.toString().toInt()
            openFragment(TotalPelangganFragment.newInstance(selectedMonth, selectedYear))
        }
        binding.cardPelangganBaru.setOnClickListener { 
            val selectedMonth = binding.spinnerMonthPelanggan.selectedItemPosition + 1
            val selectedYear = binding.spinnerYearPelanggan.selectedItem.toString().toInt()
            openFragment(PelangganBaruFragment.newInstance(selectedMonth, selectedYear))
        }
        binding.cardLunas.setOnClickListener { 
            val selectedMonth = binding.spinnerMonthPelanggan.selectedItemPosition + 1
            val selectedYear = binding.spinnerYearPelanggan.selectedItem.toString().toInt()
            openFragment(LunasFragment.newInstance(selectedMonth, selectedYear)) 
        }
        binding.cardBelumBayar.setOnClickListener { 
            val selectedMonth = binding.spinnerMonthPelanggan.selectedItemPosition + 1
            val selectedYear = binding.spinnerYearPelanggan.selectedItem.toString().toInt()
            openFragment(BelumBayarFragment.newInstance(selectedMonth, selectedYear)) 
        }
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun fetchDashboardData() {
        binding.progressBarPelanggan.visibility = View.VISIBLE

        val selectedMonth = binding.spinnerMonthPelanggan.selectedItemPosition + 1
        val selectedYear = binding.spinnerYearPelanggan.selectedItem.toString().toInt()

        apiService.getPelanggan(selectedMonth, selectedYear).enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (_binding == null) return
                binding.progressBarPelanggan.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.rekap?.let { rekap ->
                        updateDashboard(rekap)
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil data: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progressBarPelanggan.visibility = View.GONE
                Log.e("PelangganFragment", "API Call Failed", t)
                Toast.makeText(context, "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateDashboard(rekap: RekapData) {
        binding.tvTotalPelanggan.text = rekap.totalPelanggan.toString()
        binding.tvPelangganBaru.text = rekap.pelangganBaru.toString()
        binding.tvPelangganLama.text = rekap.pelangganLama.toString()
        binding.tvLunas.text = rekap.totalLunas.toString()
        binding.tvBelumBayar.text = rekap.totalBelumBayar.toString()

        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        binding.tvTotalPendapatan.text = format.format(rekap.totalPendapatan)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
