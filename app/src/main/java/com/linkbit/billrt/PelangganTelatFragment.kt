package com.linkbit.billrt

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PelangganTelatAdapter
import com.linkbit.billrt.databinding.FragmentPelangganTelatBinding
import com.linkbit.billrt.viewmodel.PelangganTelatViewModel

class PelangganTelatFragment : Fragment() {

    private var _binding: FragmentPelangganTelatBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PelangganTelatViewModel
    private lateinit var telatAdapter: PelangganTelatAdapter

    private var bulan: Int = 0
    private var tahun: Int = 0
    private var idWilayah: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.title = "Pelanggan Telat"
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        telatAdapter = PelangganTelatAdapter { pelanggan ->
            // Handle item click
            Toast.makeText(context, "Clicked on ${pelanggan.namaPelanggan}", Toast.LENGTH_SHORT).show()
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = "Cari Pelanggan..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.fetchPelangganTelat(bulan, tahun, newText.orEmpty(), idWilayah)
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            activity?.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
