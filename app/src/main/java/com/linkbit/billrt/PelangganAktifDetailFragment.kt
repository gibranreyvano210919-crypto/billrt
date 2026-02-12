package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.linkbit.billrt.databinding.FragmentPelangganAktifDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganAktifDetailFragment : BaseFragment() {

    private var _binding: FragmentPelangganAktifDetailBinding? = null
    private val binding get() = _binding!!
    private val args: PelangganAktifDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganAktifDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchPelangganDetails()
    }

    private fun fetchPelangganDetails() {
        val pelangganId = args.pelangganId
        val request = GetPelangganByIdRequest(id_pelanggan = pelangganId)

        // Tampilkan progress bar saat memuat
        // binding.progressBar.visibility = View.VISIBLE

        apiService.getPelangganById(request).enqueue(object : Callback<GetPelangganByIdResponse> {
            override fun onResponse(call: Call<GetPelangganByIdResponse>, response: Response<GetPelangganByIdResponse>) {
                if (!isAdded || _binding == null) return
                // binding.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.data != null) {
                    val pelanggan = response.body()!!.data!!
                    populateUI(pelanggan)
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal memuat detail pelanggan"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetPelangganByIdResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                // binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateUI(pelanggan: PelangganData) {
        binding.tvNamaPelanggan.text = pelanggan.nama
        binding.tvIdPelanggan.text = "ID: ${pelanggan.idPelanggan}"
        binding.tvAlamat.text = pelanggan.alamat ?: "Alamat tidak tersedia"
        binding.tvTelepon.text = pelanggan.telepon ?: "Telepon tidak tersedia"
        // Anda bisa menambahkan field lainnya di sini sesuai kebutuhan
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}