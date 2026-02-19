package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentDashboardTagihanBinding
import com.linkbit.billrt.viewmodel.DashboardTagihanViewModel
import java.text.NumberFormat
import java.util.Locale

class DashboardTagihanFragment : Fragment() {

    private var _binding: FragmentDashboardTagihanBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardTagihanViewModel

    private var bulan: Int = 0
    private var tahun: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            bulan = it.getInt("bulan")
            tahun = it.getInt("tahun")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardTagihanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(DashboardTagihanViewModel::class.java)

        setupToolbar()
        observeViewModel()
        setupClickListeners()

        viewModel.fetchAllDashboardData(bulan, tahun)
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.title = "Rekap Tagihan - ${getNamaBulan(bulan)} $tahun"
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun getNamaBulan(bulan: Int): String {
        return when (bulan) {
            1 -> "Januari"
            2 -> "Februari"
            3 -> "Maret"
            4 -> "April"
            5 -> "Mei"
            6 -> "Juni"
            7 -> "Juli"
            8 -> "Agustus"
            9 -> "September"
            10 -> "Oktober"
            11 -> "November"
            12 -> "Desember"
            else -> ""
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            if(isLoading) {
                binding.contentView.isVisible = false
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.rekapData.observe(viewLifecycleOwner) { rekap ->
            if(rekap != null) {
                binding.contentView.isVisible = true
            }
            rekap?.let { data ->
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

                val lunasUang = data.lunas.totalUang ?: 0f
                val belumBayarUang = data.belumBayar.totalUang ?: 0f
                val totalTagihan = lunasUang + belumBayarUang

                binding.tvTotalTagihan.text = "Total Tagihan: ${formatRupiah.format(totalTagihan)}"

                binding.tvLunasNominal.text = "Nominal: ${formatRupiah.format(lunasUang)}"
                binding.tvBelumBayarNominal.text = "Nominal: ${formatRupiah.format(belumBayarUang)}"
            }
        }

        viewModel.rekapJumlah.observe(viewLifecycleOwner) { rekapJumlah ->
            if(rekapJumlah != null) {
                binding.contentView.isVisible = true
            }
            rekapJumlah?.let {
                binding.tvTotalPelanggan.text = "Pelanggan Aktif: ${it.totalPelangganAktif}\nInvoice Dibuat: ${it.sudahGenerate}\nBelum Dibuat: ${it.belumGenerate}"
                binding.tvLunasJumlah.text = "Jumlah: ${it.lunas} orang"
                binding.tvBelumBayarJumlah.text = "Jumlah: ${it.belumBayar} orang"
            }
        }

        viewModel.generateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.fetchAllDashboardData(bulan, tahun)
            }.onFailure { 
                Toast.makeText(requireContext(), "Generate tagihan gagal: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.cardLunas.setOnClickListener {
            val bundle = bundleOf("bulan" to bulan, "tahun" to tahun)
            findNavController().navigate(R.id.action_dashboardTagihanFragment_to_pelangganLunasFragment, bundle)
        }

        binding.cardBelumBayar.setOnClickListener {
            val bundle = bundleOf("bulan" to bulan, "tahun" to tahun)
            findNavController().navigate(R.id.action_dashboardTagihanFragment_to_pelangganBelumBayarFragment, bundle)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_tagihan_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_generate_tagihan -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah Anda yakin ingin generate tagihan untuk periode ${getNamaBulan(bulan)} $tahun?")
                    .setPositiveButton("Ya") { _, _ ->
                        viewModel.generateTagihan(bulan, tahun)
                    }
                    .setNegativeButton("Tidak", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
