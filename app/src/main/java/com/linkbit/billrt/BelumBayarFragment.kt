package com.linkbit.billrt

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PelangganBelumBayarAdapter
import com.linkbit.billrt.databinding.FragmentBelumBayarBinding
import com.linkbit.billrt.viewmodel.PelangganBelumBayarViewModel

class BelumBayarFragment : BaseFragment() {

    private var _binding: FragmentBelumBayarBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PelangganBelumBayarViewModel
    private lateinit var adapter: PelangganBelumBayarAdapter

    private var bulan: Int = 0
    private var tahun: Int = 0
    private var idWilayah: Int? = null

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

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
        _binding = FragmentBelumBayarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets(binding.toolbar)
        
        // Setup ViewModel menggunakan Activity Scope
        viewModel = ViewModelProvider(requireActivity()).get(PelangganBelumBayarViewModel::class.java)

        // Setup UI
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        setupFragmentResultListeners()

        binding.swipeRefreshLayout.setOnRefreshListener {
            val searchView = binding.toolbar.menu.findItem(R.id.action_search).actionView as? SearchView
            viewModel.fetchPelangganBelumBayar(bulan, tahun, idWilayah, searchView?.query?.toString())
        }

        // Ambil data pertama kali
        viewModel.fetchPelangganBelumBayar(bulan, tahun, idWilayah)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = "Belum Bayar"
            subtitle = "$bulan/$tahun"
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener { findNavController().navigateUp() }
            
            menu.clear()
            inflateMenu(R.menu.search_menu)
            
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_reset_filter -> {
                        idWilayah = null
                        subtitle = "$bulan/$tahun"
                        viewModel.resetFilter()
                        Toast.makeText(context, "Filter direset", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_filter -> {
                        showWilayahFilter()
                        true
                    }
                    else -> false
                }
            }

            val searchItem = menu.findItem(R.id.action_search)
            val searchView = searchItem.actionView as? SearchView
            searchView?.queryHint = "Cari pelanggan..."
            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchRunnable?.let { searchHandler.removeCallbacks(it) }
                    viewModel.fetchPelangganBelumBayar(bulan, tahun, idWilayah, query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    searchRunnable?.let { searchHandler.removeCallbacks(it) }
                    searchRunnable = Runnable {
                        viewModel.fetchPelangganBelumBayar(bulan, tahun, idWilayah, newText)
                    }
                    searchHandler.postDelayed(searchRunnable!!, 500)
                    return true
                }
            })
        }
    }

    private fun setupRecyclerView() {
        adapter = PelangganBelumBayarAdapter { item ->
            val bottomSheet = BayarBottomSheetFragment.newInstance(item)
            bottomSheet.show(parentFragmentManager, "BayarBottomSheet")
        }
        binding.rvBelumBayar.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@BelumBayarFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.pelangganList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }
    }

    private fun setupFragmentResultListeners() {
        // Listener dari BottomSheet Filter
        parentFragmentManager.setFragmentResultListener("filter_wilayah", viewLifecycleOwner) { _, bundle ->
            val selectedId = bundle.get("id_wilayah") as? Int
            val namaWilayah = bundle.getString("nama_wilayah") ?: "Semua Wilayah"
            
            idWilayah = selectedId
            binding.toolbar.subtitle = if (idWilayah == null) "$bulan/$tahun" else "Wilayah: $namaWilayah"
            
            Log.d("SearchDebug", "Filter diterapkan: $namaWilayah ($idWilayah)")
            
            // Re-fetch data dengan filter yang dipilih
            val searchView = binding.toolbar.menu.findItem(R.id.action_search).actionView as? SearchView
            viewModel.fetchPelangganBelumBayar(bulan, tahun, idWilayah, searchView?.query?.toString())
        }

        parentFragmentManager.setFragmentResultListener("payment_successful", viewLifecycleOwner) { _, bundle ->
            if (bundle.getBoolean("refresh")) viewModel.fetchPelangganBelumBayar(bulan, tahun, idWilayah)
        }
    }

    private fun showWilayahFilter() {
        BelumBayarFilterBottomSheet.newInstance().show(parentFragmentManager, BelumBayarFilterBottomSheet.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(bulan: Int, tahun: Int): BelumBayarFragment {
            val fragment = BelumBayarFragment()
            val args = Bundle()
            args.putInt("bulan", bulan)
            args.putInt("tahun", tahun)
            fragment.arguments = args
            return fragment
        }
    }
}
