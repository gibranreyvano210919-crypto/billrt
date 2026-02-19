package com.linkbit.billrt

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PelangganBelumBayarAdapter
import com.linkbit.billrt.databinding.FragmentBelumBayarBinding
import com.linkbit.billrt.viewmodel.PelangganBelumBayarViewModel

class PelangganBelumBayarFragment : Fragment() {

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
        setHasOptionsMenu(true)
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
        _binding = FragmentBelumBayarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Belum Bayar (${getNamaBulan(bulan)} $tahun)"


        viewModel = ViewModelProvider(this).get(PelangganBelumBayarViewModel::class.java)

        setupRecyclerView()
        observeViewModel()

        setFragmentResultListener("payment_successful") { _, _ ->
            viewModel.fetchPelangganBelumBayar(bulan, tahun, idWilayah)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchPelangganBelumBayar(bulan, tahun, idWilayah)
        }

        viewModel.fetchPelangganBelumBayar(bulan, tahun, idWilayah)
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

    private fun setupRecyclerView() {
        adapter = PelangganBelumBayarAdapter { item ->
            val bottomSheet = BayarBottomSheetFragment.newInstance(item)
            bottomSheet.show(parentFragmentManager, "BayarBottomSheet")
        }
        binding.rvBelumBayar.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@PelangganBelumBayarFragment.adapter
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("SearchDebug", "Submit query: '$query'")
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                viewModel.fetchPelangganBelumBayar(bulan, tahun, idWilayah, query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("SearchDebug", "Query text change: '$newText'")
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    viewModel.fetchPelangganBelumBayar(bulan, tahun, idWilayah, newText)
                }
                searchHandler.postDelayed(searchRunnable!!, 500) // 500ms debounce
                return true
            }
        })

        searchView.setOnCloseListener {
            viewModel.fetchPelangganBelumBayar(bulan, tahun, idWilayah, null)
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(bulan: Int, tahun: Int, idWilayah: Int? = null): PelangganBelumBayarFragment {
            val fragment = PelangganBelumBayarFragment()
            val args = Bundle()
            args.putInt("bulan", bulan)
            args.putInt("tahun", tahun)
            idWilayah?.let { args.putInt("id_wilayah", it) }
            fragment.arguments = args
            return fragment
        }
    }
}
