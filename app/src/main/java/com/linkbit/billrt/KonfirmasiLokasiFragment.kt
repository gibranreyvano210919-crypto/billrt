package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.linkbit.billrt.databinding.FragmentKonfirmasiLokasiBinding
import com.linkbit.billrt.model.StandardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KonfirmasiLokasiFragment : BaseFragment() { // Diubah dari Fragment ke BaseFragment

    private var _binding: FragmentKonfirmasiLokasiBinding? = null
    private val binding get() = _binding!!
    private lateinit var pelanggan: PelangganData

    companion object {
        private const val ARG_PELANGGAN = "pelanggan"

        fun newInstance(pelanggan: PelangganData): KonfirmasiLokasiFragment {
            val fragment = KonfirmasiLokasiFragment()
            val args = Bundle()
            args.putSerializable(ARG_PELANGGAN, pelanggan)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pelanggan = it.getSerializable(ARG_PELANGGAN) as PelangganData
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKonfirmasiLokasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.namaPelanggan.text = pelanggan.nama

        binding.btnTambah.setOnClickListener {
            Toast.makeText(context, "Tambah diklik", Toast.LENGTH_SHORT).show()
        }

        binding.btnEdit.setOnClickListener {
            Toast.makeText(context, "Edit diklik", Toast.LENGTH_SHORT).show()
        }

        binding.btnHapus.setOnClickListener {
            Toast.makeText(context, "Hapus diklik", Toast.LENGTH_SHORT).show()
        }

        binding.btnKonfirmasi.setOnClickListener {
            // Dummy latitude and longitude, you need to get the real ones
            val latitude = -6.200000
            val longitude = 106.816666

            val request = SimpanPelangganRequest(
                idPelanggan = pelanggan.idPelanggan,
                namaPelanggan = pelanggan.nama,
                alamatPelanggan = pelanggan.alamat ?: "-",
                teleponPelanggan = pelanggan.telepon,
                idPaket = 0, // You may need to fetch this from the pelanggan object if available
                idWilayah = 0, // You may need to fetch this from the pelanggan object if available
                mikrotikUsername = pelanggan.mikrotikUsername ?: "",
                mikrotikPassword = "", // Not available in PelangganData
                macAddress = pelanggan.macAddress,
                latitude = latitude,
                longitude = longitude,
                tglDaftar = pelanggan.tglDaftar,
                installationDate = null
            )

            // Langsung gunakan apiService dari BaseFragment
            apiService.simpanPelanggan(request = request).enqueue(object : Callback<StandardResponse> {
                override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Lokasi berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Gagal memperbarui lokasi", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
