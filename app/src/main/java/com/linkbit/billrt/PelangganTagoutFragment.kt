package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PelangganTagoutAdapter
import com.linkbit.billrt.databinding.FragmentPelangganTagoutBinding
import com.linkbit.billrt.model.PelangganBelumBayarItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganTagoutFragment : BaseFragment() {

    private var _binding: FragmentPelangganTagoutBinding? = null
    private val binding get() = _binding!!
    
    private val args: PelangganTagoutFragmentArgs by navArgs()
    private lateinit var adapter: PelangganTagoutAdapter
    private var tagoutList: List<TagoutItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganTagoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sinkronisasi Insets agar toolbar tidak menabrak status bar
        applyWindowInsets(binding.appBarLayout)
        
        setupToolbar()
        setupRecyclerView()

        // Listener untuk mendeteksi jika pembayaran di BottomSheet berhasil
        parentFragmentManager.setFragmentResultListener("payment_successful", viewLifecycleOwner) { _, _ ->
            fetchTagoutData() // Refresh data otomatis
        }
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchTagoutData()
        }

        fetchTagoutData()
    }

    private fun setupToolbar() {
        binding.toolbarTagout.title = "Tagout (${args.bulan}/${args.tahun})"
        binding.toolbarTagout.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Inflate menu search ke toolbar
        binding.toolbarTagout.inflateMenu(R.menu.menu_search)
        val searchItem = binding.toolbarTagout.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.apply {
            queryHint = "Cari pelanggan tagout..."
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    filter(newText)
                    return true
                }
            })
        }
    }

    private fun setupRecyclerView() {
        adapter = PelangganTagoutAdapter { item ->
            val pelangganItem = PelangganBelumBayarItem(
                id_pelanggan = item.idPelanggan.toIntOrNull() ?: 0,
                namaPelanggan = item.namaPelanggan,
                mikrotikUsername = null,
                teleponPelanggan = null,
                wilayah = null,
                invoice = item.idTagihan,
                nominal = 0f,
                statusPembayaran = 0,
                statusText = item.statusTagihan,
                catatanTagout = item.catatanTagout,
                bulanTagihan = item.bulanTagihan.toString(),
                tahunTagihan = item.tahunTagihan,
                tglBayarTerakhir = null,
                namaPencatat = null
            )

            val menuSheet = TagoutMenuBottomSheetFragment.newInstance(pelangganItem)
            menuSheet.show(parentFragmentManager, TagoutMenuBottomSheetFragment.TAG)
        }
        binding.rvPelangganTagout.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPelangganTagout.adapter = adapter
    }

    private fun fetchTagoutData() {
        binding.progressBar.isVisible = true
        binding.tvEmpty.isVisible = false
        
        apiService.getTagout(args.bulan, args.tahun).enqueue(object : Callback<TagoutResponse> {
            override fun onResponse(call: Call<TagoutResponse>, response: Response<TagoutResponse>) {
                if (_binding == null) return
                binding.progressBar.isVisible = false
                binding.swipeRefreshLayout.isRefreshing = false
                
                if (response.isSuccessful) {
                    tagoutList = response.body()?.data ?: emptyList()
                    adapter.submitList(tagoutList)
                    binding.tvEmpty.isVisible = tagoutList.isEmpty()
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TagoutResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progressBar.isVisible = false
                binding.swipeRefreshLayout.isRefreshing = false
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filter(query: String?) {
        val filtered = if (query.isNullOrEmpty()) {
            tagoutList
        } else {
            tagoutList.filter {
                it.namaPelanggan.contains(query, ignoreCase = true) || 
                it.idPelanggan.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
        binding.tvEmpty.isVisible = filtered.isEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
