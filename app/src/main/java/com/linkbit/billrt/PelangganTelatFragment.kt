package com.linkbit.billrt

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PelangganTelatAdapter
import com.linkbit.billrt.databinding.FragmentPelangganTelatBinding
import com.linkbit.billrt.viewmodel.PelangganTelatViewModel

class PelangganTelatFragment : BaseFragment() {

    private var _binding: FragmentPelangganTelatBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PelangganTelatViewModel
    private lateinit var telatAdapter: PelangganTelatAdapter

    private var bulan: Int = 0
    private var tahun: Int = 0
    private var idWilayah: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bulan = it.getInt("bulan")
            tahun = it.getInt("tahun")
            idWilayah = it.getString("id_wilayah")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganTelatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sinkronisasi Insets agar toolbar tidak menabrak status bar
        applyWindowInsets(binding.appBarLayout)

        viewModel = ViewModelProvider(this).get(PelangganTelatViewModel::class.java)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchPelangganTelat(bulan, tahun, null, idWilayah)
        }

        viewModel.fetchPelangganTelat(bulan, tahun, null, idWilayah)
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Pelanggan Telat"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Inflate menu search ke toolbar
        binding.toolbar.inflateMenu(R.menu.menu_search)
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.apply {
            queryHint = "Cari pelanggan telat..."
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.fetchPelangganTelat(bulan, tahun, newText.orEmpty(), idWilayah)
                    return true
                }
            })
        }
    }

    private fun setupRecyclerView() {
        telatAdapter = PelangganTelatAdapter(
            onItemClick = { pelanggan ->
                // Handle item click - Misal buka detail
                val bundle = Bundle().apply {
                    putInt("id_pelanggan", pelanggan.idPelanggan)
                }
                findNavController().navigate(R.id.action_pelangganTelatFragment_to_detailTagihanFragment, bundle)
            },
            onItemLongClick = { pelanggan ->
                // Munculkan Bottom Sheet saat klik lama
                val bottomSheet = PelangganTelatBottomSheetFragment.newInstance(pelanggan)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
        )
        binding.rvPelangganTelat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = telatAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.pelangganList.observe(viewLifecycleOwner) { pelangganList ->
            telatAdapter.submitList(pelangganList)
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
}
