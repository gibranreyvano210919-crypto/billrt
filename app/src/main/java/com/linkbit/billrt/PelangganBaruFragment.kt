package com.linkbit.billrt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentPelangganBaruBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganBaruFragment : Fragment() {

    private var _binding: FragmentPelangganBaruBinding? = null
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
        _binding = FragmentPelangganBaruBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvPelangganBaru.layoutManager = LinearLayoutManager(context)
        fetchData()
    }

    private fun fetchData() {
        binding.progressBar.visibility = View.VISIBLE
        apiService.getPelanggan(bulan, tahun, "semua").enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (_binding == null) return // Safety check
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val allPelanggan = response.body()?.data ?: emptyList()
                    val filteredList = allPelanggan.filter { pelanggan ->
                        pelanggan.installationDate?.split("-")?.let {
                            parts -> parts.size >= 2 &&
                            parts[0].toIntOrNull() == tahun &&
                            parts[1].toIntOrNull() == bulan
                        } ?: false
                    }
                    binding.rvPelangganBaru.adapter = PelangganAdapter(filteredList) { pelanggan ->
                        openDetailPelangganFragment(pelanggan)
                    }
                    if (filteredList.isEmpty()) {
                        Toast.makeText(context, "Tidak ada pelanggan baru di periode ini", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                if (_binding == null) return // Safety check
                binding.progressBar.visibility = View.GONE
                Log.e("PelangganBaruFragment", "API Call Failed", t)
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
            PelangganBaruFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_BULAN, bulan)
                    putInt(ARG_TAHUN, tahun)
                }
            }
    }
}
