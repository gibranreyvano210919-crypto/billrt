package com.linkbit.billrt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.linkbit.billrt.databinding.FragmentDetailPelangganBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailPelangganFragment : BaseFragment() {

    private var _binding: FragmentDetailPelangganBinding? = null
    private val binding get() = _binding!!

    private var pelangganId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pelangganId = it.getString("pelangganId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pelangganId?.let {
            fetchDetailPelanggan(it)
        } ?: run {
            Toast.makeText(context, "ID Pelanggan tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchDetailPelanggan(id: String) {
        setLoading(true)
        apiService.getDataPelanggan(idPelanggan = id).enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (!isAdded || _binding == null) return
                setLoading(false)
                if (response.isSuccessful) {
                    val pelanggan = response.body()?.data?.firstOrNull()
                    if (pelanggan != null) {
                        bindData(pelanggan)
                    } else {
                        Toast.makeText(context, "Data pelanggan tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMsg = "Gagal memuat detail (Error ${response.code()})"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                setLoading(false)
                val errorMsg = "Gagal memuat detail. Periksa koneksi Anda."
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun bindData(pelanggan: PelangganData) {
        binding.tvDetailNamaPelanggan.text = pelanggan.nama
        binding.tvDetailIdPelanggan.text = "ID: ${pelanggan.idPelanggan}"
        binding.tvDetailAlamat.text = "Alamat: ${pelanggan.alamat}"
        binding.tvDetailTelepon.text = "Telepon: ${pelanggan.telepon ?: "-"}"
        binding.tvDetailWilayah.text = "Wilayah: ${pelanggan.namaWilayah ?: "-"}"
        binding.tvDetailUsernameMikrotik.text = "Username: ${pelanggan.mikrotikUsername ?: "-"}"
        binding.tvDetailIpStatic.text = "IP Statis: ${pelanggan.staticIp ?: "-"}"
        binding.tvDetailMacAddress.text = "MAC: ${pelanggan.macAddress ?: "-"}"
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}