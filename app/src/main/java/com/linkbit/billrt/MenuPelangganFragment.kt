package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.JadwalTagihanAdapter
import com.linkbit.billrt.databinding.FragmentMenuPelangganBinding
import com.linkbit.billrt.network.PelangganBaruResponse
import com.linkbit.billrt.viewmodel.JadwalTagihanViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class MenuPelangganFragment : BaseFragment() {

    private var _binding: FragmentMenuPelangganBinding? = null
    private val binding get() = _binding!!

    private lateinit var jadwalViewModel: JadwalTagihanViewModel
    private lateinit var jadwalAdapterHariIni: JadwalTagihanAdapter
    private lateinit var jadwalAdapterBesok: JadwalTagihanAdapter

    private var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        jadwalViewModel = ViewModelProvider(this).get(JadwalTagihanViewModel::class.java)

        setupFilterSpinners()
        setupRecyclerViews()
        observeJadwalViewModel()

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchCounts()
            jadwalViewModel.fetchJadwalTagihan("hari_ini")
            jadwalViewModel.fetchJadwalTagihan("besok")
        }

        binding.toolbarMenuPelanggan.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.cardPelangganAktif.setOnClickListener {
            val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganAktifFragment(selectedMonth, selectedYear)
            findNavController().navigate(action)
        }

        binding.cardPelangganNonaktif.setOnClickListener {
            val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganNonaktifFragment(selectedMonth, selectedYear)
            findNavController().navigate(action)
        }

        binding.cardPelangganIsolir.setOnClickListener {
            val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganIsolirFragment(selectedMonth, selectedYear)
            findNavController().navigate(action)
        }

        binding.cardPelangganBaru.setOnClickListener {
            val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganBaruFragment(selectedMonth, selectedYear)
            findNavController().navigate(action)
        }

        binding.cardLunas.setOnClickListener {
            val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganLunasFragment(selectedMonth, selectedYear)
            findNavController().navigate(action)
        }

        binding.cardBelumBayar.setOnClickListener {
            val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganBelumBayarFragment(selectedMonth, selectedYear)
            findNavController().navigate(action)
        }

        binding.cardNunggak.setOnClickListener {
            val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganNunggakFragment(selectedMonth, selectedYear)
            findNavController().navigate(action)
        }

        binding.cardTelat.setOnClickListener {
            val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganTelatFragment(selectedMonth, selectedYear)
            findNavController().navigate(action)
        }
        
        binding.cardTagout.setOnClickListener {
            val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganTagoutFragment(selectedMonth, selectedYear)
            findNavController().navigate(action)
        }

        binding.btnTambahPelanggan.setOnClickListener {
            findNavController().navigate(R.id.action_menuPelangganFragment_to_tambahPelangganFragment)
        }

        fetchCounts()
        jadwalViewModel.fetchJadwalTagihan("hari_ini")
        jadwalViewModel.fetchJadwalTagihan("besok")
    }

    private fun setupRecyclerViews() {
        jadwalAdapterHariIni = JadwalTagihanAdapter { Toast.makeText(context, it.namaPelanggan, Toast.LENGTH_SHORT).show() }
        binding.rvJadwalHariIni.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = jadwalAdapterHariIni
        }

        jadwalAdapterBesok = JadwalTagihanAdapter { Toast.makeText(context, it.namaPelanggan, Toast.LENGTH_SHORT).show() }
        binding.rvJadwalBesok.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = jadwalAdapterBesok
        }
    }

    private fun observeJadwalViewModel() {
        jadwalViewModel.jadwalHariIniList.observe(viewLifecycleOwner) { list ->
            jadwalAdapterHariIni.submitList(list)
        }

        jadwalViewModel.jadwalBesokList.observe(viewLifecycleOwner) { list ->
            jadwalAdapterBesok.submitList(list)
        }

        jadwalViewModel.isLoadingHariIni.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarHariIni.isVisible = isLoading
            binding.rvJadwalHariIni.isVisible = !isLoading
        }

        jadwalViewModel.isLoadingBesok.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarBesok.isVisible = isLoading
            binding.rvJadwalBesok.isVisible = !isLoading
        }
    }

    private fun setupFilterSpinners() {
        // Setup Bulan Spinner
        val bulanArray = resources.getStringArray(R.array.bulan_array)
        val bulanAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bulanArray)
        bulanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBulan.adapter = bulanAdapter
        binding.spinnerBulan.setSelection(selectedMonth - 1)

        // Setup Tahun Spinner
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2020..currentYear).map { it.toString() }.reversed()
        val tahunAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTahun.adapter = tahunAdapter
        binding.spinnerTahun.setSelection(years.indexOf(selectedYear.toString()))

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonth = binding.spinnerBulan.selectedItemPosition + 1
                selectedYear = binding.spinnerTahun.selectedItem.toString().toInt()
                fetchCounts()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerBulan.onItemSelectedListener = listener
        binding.spinnerTahun.onItemSelectedListener = listener
    }

    private fun fetchCounts() {
        binding.swipeRefreshLayout.isRefreshing = true
        loadCount = 0

        // Fetch Rekap Status
        apiService.getRekapStatusPelanggan(selectedMonth, selectedYear).enqueue(object : Callback<RekapStatusPelangganResponse> {
            override fun onResponse(call: Call<RekapStatusPelangganResponse>, response: Response<RekapStatusPelangganResponse>) {
                if (_binding == null || !isAdded) return
                if (response.isSuccessful) {
                    val rekap = response.body()?.data
                    binding.tvCountAktif.text = rekap?.aktif?.toString() ?: "0"
                    binding.tvCountIsolir.text = rekap?.isolir?.toString() ?: "0"
                    binding.tvCountNonaktif.text = rekap?.nonaktif?.toString() ?: "0"
                    binding.tvCountLunas.text = rekap?.lunas?.toString() ?: "0"
                    binding.tvCountBelumBayar.text = rekap?.belumBayar?.toString() ?: "0"
                    binding.tvCountTagout.text = rekap?.tagout?.toString() ?: "0"
                    binding.tvCountTelat.text = rekap?.telat?.toString() ?: "0"
                    binding.tvCountNunggak.text = rekap?.totalNunggak?.toString() ?: "0"
                }
                checkIfAllCountsLoaded()
            }

            override fun onFailure(call: Call<RekapStatusPelangganResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                checkIfAllCountsLoaded()
            }
        })

        // Fetch Baru Count with filter
        apiService.getPelangganBaru(bulan = selectedMonth, tahun = selectedYear).enqueue(object : Callback<PelangganBaruResponse> {
            override fun onResponse(call: Call<PelangganBaruResponse>, response: Response<PelangganBaruResponse>) {
                if (_binding == null || !isAdded) return
                if (response.isSuccessful) {
                    binding.tvCountBaru.text = response.body()?.total?.toString() ?: "0"
                }
                checkIfAllCountsLoaded()
            }
            override fun onFailure(call: Call<PelangganBaruResponse>, t: Throwable) {
                 if (_binding == null || !isAdded) return
                 checkIfAllCountsLoaded()
            }
        })
    }

    private var loadCount = 0
    private val totalLoads = 3 // Increased for the jadwal tagihan call
    private fun checkIfAllCountsLoaded() {
        loadCount++
        if (loadCount >= totalLoads) {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
