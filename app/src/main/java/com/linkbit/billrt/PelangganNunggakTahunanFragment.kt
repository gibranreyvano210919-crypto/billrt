package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentPelangganNunggakTahunanBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganNunggakTahunanFragment : BaseFragment() {

    private var _binding: FragmentPelangganNunggakTahunanBinding? = null
    private val binding get() = _binding!!
    private val args: PelangganNunggakTahunanFragmentArgs by navArgs()
    private lateinit var adapter: PelangganNunggakTahunanAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganNunggakTahunanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets(binding.appBarLayout)

        setupRecyclerView()

        binding.toolbar.title = "Nunggak Tahunan ${args.tahun}"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }

        loadData()
    }

    private fun setupRecyclerView() {
        adapter = PelangganNunggakTahunanAdapter(emptyList()) { item ->
            // Navigasi ke detail pelanggan menggunakan action yang sudah diperbarui di nav_graph.xml
            if (findNavController().currentDestination?.id == R.id.pelangganNunggakTahunanFragment) {
                val action = PelangganNunggakTahunanFragmentDirections.actionPelangganNunggakTahunanFragmentToDetailPelangganFragment(item.id.toString())
                findNavController().navigate(action)
            }
        }
        binding.rvNunggakTahunan.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNunggakTahunan.adapter = adapter
    }

    private fun loadData() {
        binding.swipeRefreshLayout.isRefreshing = true
        apiService.getDetailNunggakTahunan(args.tahun).enqueue(object : Callback<NunggakTahunanResponse> {
            override fun onResponse(call: Call<NunggakTahunanResponse>, response: Response<NunggakTahunanResponse>) {
                if (_binding == null || !isAdded) return
                binding.swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    adapter.updateData(data)
                    if (data.isEmpty()) {
                        Toast.makeText(context, "Tidak ada data tunggakan tahunan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NunggakTahunanResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                binding.swipeRefreshLayout.isRefreshing = false
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
