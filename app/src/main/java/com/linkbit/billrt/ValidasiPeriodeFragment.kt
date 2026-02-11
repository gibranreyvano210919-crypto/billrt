package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.linkbit.billrt.adapter.RekapTunggakanLanjutanAdapter
import com.linkbit.billrt.databinding.FragmentValidasiPeriodeBinding
import com.linkbit.billrt.model.Teknisi
import com.linkbit.billrt.model.Wilayah
import com.linkbit.billrt.network.ApiResult
import com.linkbit.billrt.viewmodel.ValidasiPeriodeViewModel
import java.util.Calendar

class ValidasiPeriodeFragment : Fragment() {

    private var _binding: FragmentValidasiPeriodeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ValidasiPeriodeViewModel by viewModels()

    private var selectedWilayahId: Int? = 0
    private var selectedTeknisiId: Int? = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentValidasiPeriodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        observeViewModel()
        viewModel.fetchWilayah()
        viewModel.fetchTeknisi()

        binding.btnTampilkan.setOnClickListener {
            val bulan = binding.actvBulan.text.toString()
            val tahun = binding.actvTahun.text.toString()
            if (bulan.isNotEmpty() && tahun.isNotEmpty()) {
                viewModel.fetchRekapTunggakan(getMonthNumber(bulan), tahun.toInt(), selectedWilayahId, selectedTeknisiId)
            } else {
                Toast.makeText(requireContext(), "Pilih bulan dan tahun", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDropdowns() {
        // Wilayah
        binding.actvWilayah.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.adapter.getItem(position) as Wilayah
            selectedWilayahId = selected.id_wilayah
        }

        // Teknisi
        binding.actvTeknisi.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.adapter.getItem(position) as Teknisi
            selectedTeknisiId = selected.id
        }

        // Bulan
        val months = resources.getStringArray(R.array.months)
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, months)
        binding.actvBulan.setAdapter(monthAdapter)

        // Tahun
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear + 5).map { it.toString() }
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, years)
        binding.actvTahun.setAdapter(yearAdapter)
    }

    private fun observeViewModel() {
        viewModel.wilayahList.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> { /* No-op */ }
                is ApiResult.Success -> {
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, it.data)
                    binding.actvWilayah.setAdapter(adapter)
                }
                is ApiResult.Error -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.teknisiList.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> { /* No-op */ }
                is ApiResult.Success -> {
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, it.data)
                    binding.actvTeknisi.setAdapter(adapter)
                }
                is ApiResult.Error -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.rekapTunggakan.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvRekapTunggakan.visibility = View.GONE
                    binding.tvPeriodeTarget.visibility = View.GONE
                }
                is ApiResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvRekapTunggakan.visibility = View.VISIBLE
                    binding.rvRekapTunggakan.adapter = RekapTunggakanLanjutanAdapter(it.data)
                }
                is ApiResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvPeriodeTarget.visibility = View.GONE
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.periodeTarget.observe(viewLifecycleOwner) {
            binding.tvPeriodeTarget.text = "Periode Target: $it"
            binding.tvPeriodeTarget.visibility = View.VISIBLE
        }
    }

    private fun getMonthNumber(monthName: String): Int {
        val months = resources.getStringArray(R.array.months)
        return months.indexOf(monthName) + 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}