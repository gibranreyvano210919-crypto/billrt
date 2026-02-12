package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentPelangganBaruBinding
import com.linkbit.billrt.network.PelangganBaru
import com.linkbit.billrt.network.PelangganBaruResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganBaruFragment : BaseFragment() {

    private var _binding: FragmentPelangganBaruBinding? = null
    private val binding get() = _binding!!

    private lateinit var pelangganBaruAdapter: PelangganBaruAdapter
    private var pelangganBaruList: List<PelangganBaru> = emptyList()
    private val args: PelangganBaruFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPelangganBaruBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchPelangganBaru()
    }

    private fun setupRecyclerView() {
        pelangganBaruAdapter = PelangganBaruAdapter(emptyList())
        binding.rvPelangganBaru.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganBaru.adapter = pelangganBaruAdapter
    }

    private fun fetchPelangganBaru() {
        binding.progressBar.visibility = View.VISIBLE
        apiService.getPelangganBaru(bulan = args.bulan, tahun = args.tahun).enqueue(object : Callback<PelangganBaruResponse> {
            override fun onResponse(call: Call<PelangganBaruResponse>, response: Response<PelangganBaruResponse>) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    pelangganBaruList = response.body()?.data ?: emptyList()
                    pelangganBaruAdapter.updateData(pelangganBaruList)
                } else {
                    Toast.makeText(context, "Gagal memuat data pelanggan baru", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PelangganBaruResponse>, t: Throwable) {
                if (!isAdded) return
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
