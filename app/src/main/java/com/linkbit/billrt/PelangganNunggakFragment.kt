package com.linkbit.billrt

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PelangganNunggakAdapter
import com.linkbit.billrt.databinding.FragmentPelangganNunggakBinding
import com.linkbit.billrt.viewmodel.PelangganNunggakViewModel
import java.text.NumberFormat
import java.util.Locale

class PelangganNunggakFragment : Fragment() {

    private var _binding: FragmentPelangganNunggakBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PelangganNunggakViewModel
    private lateinit var nunggakAdapter: PelangganNunggakAdapter

    private var bulan: Int = 0
    private var tahun: Int = 0

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
        _binding = FragmentPelangganNunggakBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PelangganNunggakViewModel::class.java)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchPelangganNunggak(bulan, tahun)
        }

        viewModel.fetchPelangganNunggak(bulan, tahun)
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Pelanggan Nunggak"
        binding.toolbar.subtitle = null
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Inflate menu search ke toolbar
        binding.toolbar.inflateMenu(R.menu.menu_search)
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.apply {
            queryHint = "Cari pelanggan nunggak..."
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.fetchPelangganNunggak(bulan, tahun, newText.orEmpty())
                    return true
                }
            })
        }
    }

    private fun setupRecyclerView() {
        nunggakAdapter = PelangganNunggakAdapter { pelanggan ->
            // Handle item click
            Toast.makeText(context, "Clicked on ${pelanggan.nama_pelanggan}", Toast.LENGTH_SHORT).show()
        }
        binding.rvPelangganNunggak.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = nunggakAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.pelangganList.observe(viewLifecycleOwner) { pelangganList ->
            nunggakAdapter.submitList(pelangganList)
            binding.swipeRefreshLayout.isRefreshing = false

            val totalTunggakan = pelangganList.sumOf { it.total_tunggakan.toDouble() }
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.tvTotalTunggakan.text = format.format(totalTunggakan)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotBlank()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.responseMessage.observe(viewLifecycleOwner) { message ->
            binding.toolbar.subtitle = message
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
