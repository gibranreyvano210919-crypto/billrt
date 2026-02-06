package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentMasterWilayahBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MasterWilayahFragment : BaseFragment() {

    private var _binding: FragmentMasterWilayahBinding? = null
    private val binding get() = _binding!!
    private lateinit var wilayahAdapter: MasterWilayahAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMasterWilayahBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchWilayah()

        // Menyembunyikan FAB untuk sementara sesuai permintaan
        binding.fabAddMasterWilayah.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        // Listener untuk edit dan delete dikosongkan untuk menonaktifkan fungsionalitas
        wilayahAdapter = MasterWilayahAdapter(emptyList(), onEditClick = {}, onDeleteClick = {})
        binding.rvMasterWilayah.layoutManager = LinearLayoutManager(context)
        binding.rvMasterWilayah.adapter = wilayahAdapter
    }

    private fun fetchWilayah() {
        binding.progressBar.visibility = View.VISIBLE
        apiService.getWilayah().enqueue(object : Callback<WilayahResponse> {
            override fun onResponse(call: Call<WilayahResponse>, response: Response<WilayahResponse>) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val wilayahList = response.body()?.data ?: emptyList()
                    wilayahAdapter.updateData(wilayahList)
                } else {
                    Toast.makeText(context, "Gagal mengambil data wilayah", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WilayahResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}