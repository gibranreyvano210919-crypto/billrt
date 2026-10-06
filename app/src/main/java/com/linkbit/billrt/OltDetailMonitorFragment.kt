package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentOltDetailMonitorBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OltDetailMonitorFragment : BaseFragment() {

    private var _binding: FragmentOltDetailMonitorBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OnuMonitorAdapter
    private var oltId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOltDetailMonitorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets(binding.appBarLayout)

        oltId = arguments?.getInt("oltId") ?: -1

        setupRecyclerView()

        binding.toolbarOltDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchDetail()
        }

        fetchDetail()
    }

    private fun setupRecyclerView() {
        adapter = OnuMonitorAdapter(emptyList())
        binding.rvOnuList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOnuList.adapter = adapter
    }

    private fun fetchDetail() {
        if (oltId == -1) {
            Toast.makeText(context, "ID OLT tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        binding.swipeRefreshLayout.isRefreshing = true

        apiService.getOltDetail(oltId).enqueue(object : Callback<OltDetailResponse> {
            override fun onResponse(call: Call<OltDetailResponse>, response: Response<OltDetailResponse>) {
                if (_binding == null || !isAdded) return
                binding.swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null && data.status) {
                        updateUI(data)
                    } else {
                        Toast.makeText(context, "Gagal memuat detail ONU", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Gagal memuat detail ONU", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OltDetailResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                binding.swipeRefreshLayout.isRefreshing = false
                Toast.makeText(context, "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI(data: OltDetailResponse) {
        binding.apply {
            tvOltNameHeader.text = data.infoOlt.nama
            tvOltHostHeader.text = data.infoOlt.host
            tvTotalOnu.text = "Total ONU: ${data.totalOnu}"
            
            adapter.updateData(data.results)
            layoutEmpty.visibility = if (data.results.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
