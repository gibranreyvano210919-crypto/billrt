package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PelangganTagoutAdapter
import com.linkbit.billrt.databinding.FragmentPelangganTagoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganTagoutFragment : BaseFragment() {

    private var _binding: FragmentPelangganTagoutBinding? = null
    private val binding get() = _binding!!
    
    private val args: PelangganTagoutFragmentArgs by navArgs()
    private lateinit var adapter: PelangganTagoutAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganTagoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchTagoutData()
        }

        fetchTagoutData()
    }

    private fun setupToolbar() {
        binding.toolbarTagout.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbarTagout.title = "Tagout (${args.bulan}/${args.tahun})"
    }

    private fun setupRecyclerView() {
        adapter = PelangganTagoutAdapter { item ->
            // Placeholder click action
            Toast.makeText(requireContext(), "Pelanggan: ${item.namaPelanggan}", Toast.LENGTH_SHORT).show()
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
                    val list = response.body()?.data ?: emptyList()
                    adapter.submitList(list)
                    binding.tvEmpty.isVisible = list.isEmpty()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
