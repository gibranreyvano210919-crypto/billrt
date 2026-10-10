package com.linkbit.billrt

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PelangganTelatTahunanAdapter
import com.linkbit.billrt.databinding.FragmentPelangganTelatBinding
import com.linkbit.billrt.viewmodel.PelangganTelatViewModel

class PelangganTelatTahunanFragment : BaseFragment() {

    private var _binding: FragmentPelangganTelatBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PelangganTelatViewModel
    private lateinit var telatAdapter: PelangganTelatTahunanAdapter

    private var tahun: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tahun = it.getInt("tahun")
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
            loadData()
        }

        loadData()
    }

    private fun loadData() {
        viewModel.fetchRekapTelatTahunan(tahun)
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Telat Tahunan ($tahun)"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.inflateMenu(R.menu.menu_search)
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.apply {
            queryHint = "Cari pelanggan..."
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    filterList(newText.orEmpty())
                    return true
                }
            })
        }
    }

    private fun filterList(query: String) {
        val originalList = viewModel.pelangganTahunanList.value ?: return
        if (query.isEmpty()) {
            telatAdapter.submitList(originalList)
        } else {
            val filtered = originalList.filter {
                it.namaPelanggan.contains(query, ignoreCase = true) ||
                it.wilayah.contains(query, ignoreCase = true)
            }
            telatAdapter.submitList(filtered)
        }
    }

    private fun setupRecyclerView() {
        telatAdapter = PelangganTelatTahunanAdapter(
            onItemClick = { pelanggan ->
                val bundle = Bundle().apply {
                    putString("pelangganId", pelanggan.idPelanggan.toString())
                }
                // Update action to match nav_graph.xml
                findNavController().navigate(R.id.action_pelangganTelatTahunanFragment_to_detailPelangganFragment, bundle)
            }
        )
        binding.rvPelangganTelat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = telatAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.pelangganTahunanList.observe(viewLifecycleOwner) { pelangganList ->
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
