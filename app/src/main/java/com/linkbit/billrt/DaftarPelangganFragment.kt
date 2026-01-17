package com.linkbit.billrt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentDaftarPelangganBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DaftarPelangganFragment : Fragment() {

    private var _binding: FragmentDaftarPelangganBinding? = null
    private val binding get() = _binding!!
    private val apiService: ApiService by lazy { ApiConfig.getApiService() }

    private var filterTipe: String = "semua"
    private var bulan: Int = 0
    private var tahun: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filterTipe = it.getString(ARG_FILTER_TIPE, "semua")
            bulan = it.getInt(ARG_BULAN)
            tahun = it.getInt(ARG_TAHUN)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDaftarPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvPelanggan.layoutManager = LinearLayoutManager(context)
        fetchDaftarPelanggan()
    }

    private fun fetchDaftarPelanggan() {
        binding.progressBar.visibility = View.VISIBLE
        apiService.getPelanggan(bulan, tahun, filterTipe).enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val pelangganList = response.body()?.data ?: emptyList()
                    if (pelangganList.isNotEmpty()) {
                        binding.rvPelanggan.adapter = PelangganAdapter(pelangganList) { pelanggan ->
                            openDetailPelangganFragment(pelanggan)
                        }
                    } else {
                        Toast.makeText(context, "Tidak ada data pelanggan untuk ditampilkan.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil data: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Log.e("DaftarPelanggan", "API Call Failed", t)
                Toast.makeText(context, "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun openDetailPelangganFragment(pelanggan: PelangganData) {
        val fragment = DetailPelangganFragment.newInstance(pelanggan)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(DETAIL_PELANGGAN_BACKSTACK_NAME)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val DETAIL_PELANGGAN_BACKSTACK_NAME = "detail_pelanggan_transaction"
        private const val ARG_FILTER_TIPE = "filter_tipe"
        private const val ARG_BULAN = "bulan"
        private const val ARG_TAHUN = "tahun"

        @JvmStatic
        fun newInstance(filterTipe: String, bulan: Int, tahun: Int) =
            DaftarPelangganFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FILTER_TIPE, filterTipe)
                    putInt(ARG_BULAN, bulan)
                    putInt(ARG_TAHUN, tahun)
                }
            }
    }
}
