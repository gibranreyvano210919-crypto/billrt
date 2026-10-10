package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.LunasAdapter
import com.linkbit.billrt.databinding.FragmentPelangganLunasBinding
import com.linkbit.billrt.model.PelangganLunasItem
import com.linkbit.billrt.viewmodel.LunasViewModel

class PelangganLunasFragment : BaseFragment(), LunasBottomSheetFragment.ItemClickListener {

    private var _binding: FragmentPelangganLunasBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LunasViewModel
    private lateinit var lunasAdapter: LunasAdapter

    private var bulan: Int = 0
    private var tahun: Int = 0
    private var idWilayah: Int? = null

    private var selectedPelanggan: PelangganLunasItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bulan = it.getInt("bulan")
            tahun = it.getInt("tahun")
            idWilayah = if (it.containsKey("id_wilayah")) it.getInt("id_wilayah") else null
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

        // Sinkronisasi Insets agar toolbar tidak menabrak status bar
        applyWindowInsets(binding.appBarLayout)

        viewModel = ViewModelProvider(this).get(LunasViewModel::class.java)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchPelangganLunas(bulan, tahun, idWilayah)
        }

        // Initial data fetch
        viewModel.fetchPelangganLunas(bulan, tahun, idWilayah)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
            inflateMenu(R.menu.menu_pelanggan_lunas)

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_reset_filter -> {
                        viewModel.resetFilter()
                        Toast.makeText(context, "Filter direset", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_filter -> {
                        showFilterBottomSheet()
                        true
                    }
                    else -> false
                }
            }

            val searchItem = menu.findItem(R.id.action_search)
            val searchView = searchItem.actionView as? SearchView

            searchView?.apply {
                queryHint = "Cari pelanggan..."
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.setSearchQuery(newText.orEmpty())
                        return true
                    }
                })
            }
        }
    }

    private fun showFilterBottomSheet() {
        val summary = viewModel.summary.value
        val items = viewModel.pelangganList.value ?: emptyList()
        val filterSheet = LunasFilterBottomSheet.newInstance(summary, items)
        filterSheet.setFilterListeners(
            onApply = { idWil, idPencatat, start, end ->
                viewModel.applyFilter(idWil, idPencatat, start, end)
                Toast.makeText(context, "Filter diterapkan", Toast.LENGTH_SHORT).show()
            },
            onReset = {
                viewModel.resetFilter()
                Toast.makeText(context, "Filter direset", Toast.LENGTH_SHORT).show()
            }
        )
        filterSheet.show(childFragmentManager, LunasFilterBottomSheet.TAG)
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
            if (!message.isNullOrBlank()) {
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
                    viewModel.batalPembayaran(pelanggan.idTagihan, bulan, tahun, idWilayah)
                } ?: run {
                    Toast.makeText(context, "Silakan pilih pelanggan terlebih dahulu", Toast.LENGTH_SHORT).show()
                }
            }
            "cetak" -> {
                selectedPelanggan?.let { pelanggan ->
                    val bundle = Bundle().apply {
                        putSerializable("pelanggan_item", pelanggan)
                    }
                    findNavController().navigate(R.id.action_global_cetakNotaFragment, bundle)
                } ?: run {
                    Toast.makeText(context, "Silakan pilih pelanggan terlebih dahulu", Toast.LENGTH_SHORT).show()
                }
            }
            "riwayat" -> {
                selectedPelanggan?.let { pelanggan ->
                    val historyBottomSheet = HistoryPembayaranBottomSheetFragment.newInstance(pelanggan.id_pelanggan)
                    historyBottomSheet.show(childFragmentManager, HistoryPembayaranBottomSheetFragment.TAG)
                }
            }
        }
    }

    companion object {
        fun newInstance(bulan: Int, tahun: Int, idWilayah: Int? = null): PelangganLunasFragment {
            val fragment = PelangganLunasFragment()
            val args = Bundle()
            args.putInt("bulan", bulan)
            args.putInt("tahun", tahun)
            idWilayah?.let { args.putInt("id_wilayah", it) }
            fragment.arguments = args
            return fragment
        }
    }
}
