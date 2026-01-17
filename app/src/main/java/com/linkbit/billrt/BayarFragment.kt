package com.linkbit.billrt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentBayarBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BayarFragment : Fragment() {

    private var _binding: FragmentBayarBinding? = null
    private val binding get() = _binding!!
    private val apiService: ApiService by lazy { ApiConfig.getApiService() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBayarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvBayar.layoutManager = LinearLayoutManager(context)
        fetchRiwayatPembayaran()
    }

    private fun fetchRiwayatPembayaran() {
        apiService.getRiwayatPembayaran().enqueue(object : Callback<PembayaranResponse> {
            override fun onResponse(call: Call<PembayaranResponse>, response: Response<PembayaranResponse>) {
                if (response.isSuccessful) {
                    val pembayaranList = response.body()?.data ?: emptyList()
                    binding.rvBayar.adapter = BayarAdapter(pembayaranList)
                } else {
                    Toast.makeText(context, "Gagal mengambil riwayat pembayaran", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PembayaranResponse>, t: Throwable) {
                Log.e("BayarFragment", "API Call Failed", t)
                Toast.makeText(context, "Koneksi Gagal", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(pelanggan: PelangganData) =
            BayarFragment().apply {
                // Pelanggan data can be used in the future if needed
            }
    }
}
