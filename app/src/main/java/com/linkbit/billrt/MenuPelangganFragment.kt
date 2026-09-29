package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentMenuPelangganBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class MenuPelangganFragment : BaseFragment() {

    private var _binding: FragmentMenuPelangganBinding? = null
    private val binding get() = _binding!!

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

        applyWindowInsets(binding.appBarLayout)

        setupFilterSpinners()

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        binding.toolbarMenuPelanggan.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.cardPelangganAktif.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.menuPelangganFragment) {
                val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganAktifFragment(selectedMonth, selectedYear)
                findNavController().navigate(action)
            }
        }

        binding.cardPelangganNonaktif.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.menuPelangganFragment) {
                val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganNonaktifFragment(selectedMonth, selectedYear)
                findNavController().navigate(action)
            }
        }

        binding.cardPelangganIsolir.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.menuPelangganFragment) {
                val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganIsolirFragment(selectedMonth, selectedYear)
                findNavController().navigate(action)
            }
        }

        binding.cardPelangganBaru.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.menuPelangganFragment) {
                val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganBaruFragment(selectedMonth, selectedYear)
                findNavController().navigate(action)
            }
        }

        binding.cardLunas.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.menuPelangganFragment) {
                val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganLunasFragment(selectedMonth, selectedYear)
                findNavController().navigate(action)
            }
        }

        binding.cardBelumBayar.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.menuPelangganFragment) {
                val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganBelumBayarFragment(selectedMonth, selectedYear)
                findNavController().navigate(action)
            }
        }

        binding.cardNunggak.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.menuPelangganFragment) {
                val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganNunggakFragment(selectedMonth, selectedYear)
                findNavController().navigate(action)
            }
        }

        binding.cardTelat.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.menuPelangganFragment) {
                val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganTelatFragment(selectedMonth, selectedYear)
                findNavController().navigate(action)
            }
        }
        
        binding.cardTagout.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.menuPelangganFragment) {
                val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganTagoutFragment(selectedMonth, selectedYear)
                findNavController().navigate(action)
            }
        }

        binding.cardNunggakTahunan.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.menuPelangganFragment) {
                val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganNunggakTahunanFragment(selectedYear)
                findNavController().navigate(action)
            }
        }

        binding.cardTelatTahunan.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.menuPelangganFragment) {
                val action = MenuPelangganFragmentDirections.actionMenuPelangganFragmentToPelangganTelatTahunanFragment(selectedYear)
                findNavController().navigate(action)
            }
        }

        binding.btnTambahPelanggan.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.menuPelangganFragment) {
                findNavController().navigate(R.id.tambahPelangganFragment)
            }
        }

        refreshData()
    }

    private fun setupFilterSpinners() {
        val bulanArray = resources.getStringArray(R.array.bulan_array)
        val bulanAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bulanArray)
        bulanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBulan.adapter = bulanAdapter
        binding.spinnerBulan.setSelection(selectedMonth - 1)

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
                refreshData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerBulan.onItemSelectedListener = listener
        binding.spinnerTahun.onItemSelectedListener = listener
    }

    private fun refreshData() {
        fetchCounts()
    }

    private fun fetchCounts() {
        binding.swipeRefreshLayout.isRefreshing = true

        apiService.getRekapStatusPelanggan(selectedMonth, selectedYear).enqueue(object : Callback<RekapStatusPelangganResponse> {
            override fun onResponse(call: Call<RekapStatusPelangganResponse>, response: Response<RekapStatusPelangganResponse>) {
                if (_binding == null || !isAdded) return
                binding.swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    val rekap = response.body()?.data
                    binding.tvCountAktif.text = rekap?.aktif ?: "0"
                    binding.tvCountIsolir.text = rekap?.isolir?.toString() ?: "0"
                    binding.tvCountNonaktif.text = rekap?.nonaktif?.toString() ?: "0"
                    binding.tvCountLunas.text = rekap?.lunas?.toString() ?: "0"
                    binding.tvCountBelumBayar.text = rekap?.belumBayar?.toString() ?: "0"
                    binding.tvCountTagout.text = rekap?.tagout?.toString() ?: "0"
                    binding.tvCountTelat.text = rekap?.telat?.toString() ?: "0"
                    binding.tvCountNunggak.text = rekap?.totalNunggak?.toString() ?: "0"
                    binding.tvCountNunggakTahunan.text = rekap?.nunggakTahunan?.toString() ?: "0"
                    binding.tvCountTelatTahunan.text = rekap?.telatTahunan?.toString() ?: "0"
                    binding.tvCountBaru.text = rekap?.baru?.toString() ?: "0"
                }
            }

            override fun onFailure(call: Call<RekapStatusPelangganResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                binding.swipeRefreshLayout.isRefreshing = false
                Toast.makeText(context, "Gagal memuat rekap", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
