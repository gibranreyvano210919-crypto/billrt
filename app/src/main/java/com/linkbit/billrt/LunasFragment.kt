package com.linkbit.billrt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentLunasBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LunasFragment : Fragment() {

    private var _binding: FragmentLunasBinding? = null
    private val binding get() = _binding!!
    private val apiService: ApiService by lazy { ApiConfig.getApiService() }

    private var bulan: Int = 0
    private var tahun: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bulan = it.getInt(ARG_BULAN)
            tahun = it.getInt(ARG_TAHUN)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLunasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvLunas.layoutManager = LinearLayoutManager(context)
        fetchData()
    }

    private fun fetchData() {
        binding.progressBar.visibility = View.VISIBLE
        apiService.getPelanggan(bulan, tahun, "lunas").enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (_binding == null) return // Safety check
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val pelangganList = response.body()?.data ?: emptyList()
                    binding.rvLunas.adapter = PelangganAdapter(pelangganList) { pelanggan ->
                        openDetailPelangganFragment(pelanggan)
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                if (_binding == null) return // Safety check
                binding.progressBar.visibility = View.GONE
                Log.e("LunasFragment", "API Call Failed", t)
                Toast.makeText(context, "Koneksi Gagal", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun openDetailPelangganFragment(pelanggan: PelangganData) {
        val fragment = DetailPelangganFragment.newInstance(pelanggan)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_BULAN = "bulan"
        private const val ARG_TAHUN = "tahun"

        @JvmStatic
        fun newInstance(bulan: Int, tahun: Int) =
            LunasFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_BULAN, bulan)
                    putInt(ARG_TAHUN, tahun)
                }
            }
    }
}
