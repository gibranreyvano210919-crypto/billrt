package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.LunasAdapter
import com.linkbit.billrt.databinding.FragmentPelangganLunasBinding
import com.linkbit.billrt.model.PelangganLunasItem
import com.linkbit.billrt.viewmodel.LunasViewModel

class PelangganLunasFragment : Fragment(), LunasBottomSheetFragment.ItemClickListener {

    private var _binding: FragmentPelangganLunasBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LunasViewModel
    private lateinit var lunasAdapter: LunasAdapter

    private var bulan: Int = 0
    private var tahun: Int = 0

    private var selectedPelanggan: PelangganLunasItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bulan = it.getInt("bulan")
            tahun = it.getInt("tahun")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganLunasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(LunasViewModel::class.java)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        setupSearchView()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchPelangganLunas(bulan, tahun)
        }

        // Initial data fetch
        viewModel.fetchPelangganLunas(bulan, tahun)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.fetchPelangganLunas(bulan, tahun, newText.orEmpty())
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        lunasAdapter = LunasAdapter { pelanggan ->
            selectedPelanggan = pelanggan
            val bottomSheet = LunasBottomSheetFragment.newInstance()
            bottomSheet.show(childFragmentManager, LunasBottomSheetFragment.TAG)
        }
        binding.rvPelangganLunas.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = lunasAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.pelangganList.observe(viewLifecycleOwner) { pelangganList ->
            lunasAdapter.submitList(pelangganList)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotBlank()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(item: String) {
        when (item) {
            "batalkan" -> {
                selectedPelanggan?.let { pelanggan ->
                    viewModel.batalPembayaran(pelanggan.idTagihan, bulan, tahun)
                } ?: run {
                    Toast.makeText(context, "Silakan pilih pelanggan terlebih dahulu", Toast.LENGTH_SHORT).show()
                }
            }
            "cetak" -> {
                Toast.makeText(context, "Cetak pembayaran untuk ${selectedPelanggan?.namaPelanggan}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(bulan: Int, tahun: Int): PelangganLunasFragment {
            val fragment = PelangganLunasFragment()
            val args = Bundle()
            args.putInt("bulan", bulan)
            args.putInt("tahun", tahun)
            fragment.arguments = args
            return fragment
        }
    }
}
