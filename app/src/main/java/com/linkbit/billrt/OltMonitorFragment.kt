package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentMonitoringOltBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OltMonitorFragment : BaseFragment() {

    private var _binding: FragmentMonitoringOltBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OltMonitorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonitoringOltBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets(binding.toolbarMonitoringOlt)

        setupRecyclerView()

        binding.toolbarMonitoringOlt.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchData()
        }

        fetchData()
    }

    private fun setupRecyclerView() {
        adapter = OltMonitorAdapter(emptyList()) { olt ->
            // Menggunakan safe call (?.) secara langsung untuk menghindari NullPointerException
            // Jika id null, maka toIntOrNull tidak akan dipanggil dan default 0 akan diambil.
            val oltIdInt = olt.id?.toIntOrNull() ?: 0
            
            if (oltIdInt != 0) {
                val bundle = Bundle().apply {
                    putInt("oltId", oltIdInt)
                }
                findNavController().navigate(R.id.action_oltMonitorFragment_to_oltDetailMonitorFragment, bundle)
            } else {
                Toast.makeText(context, "ID OLT tidak valid: ${olt.id}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvMonitoringOlt.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMonitoringOlt.adapter = adapter
    }

    private fun fetchData() {
        binding.swipeRefreshLayout.isRefreshing = true

        apiService.getMasterOlt().enqueue(object : Callback<OltResponse> {
            override fun onResponse(call: Call<OltResponse>, response: Response<OltResponse>) {
                if (_binding == null || !isAdded) return
                binding.swipeRefreshLayout.isRefreshing = false
                
                if (response.isSuccessful) {
                    val oltList = response.body()?.results ?: emptyList()
                    adapter.updateData(oltList)
                    binding.layoutEmpty.visibility = if (oltList.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(context, "Gagal memuat data OLT", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OltResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                binding.swipeRefreshLayout.isRefreshing = false
                Toast.makeText(context, "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
