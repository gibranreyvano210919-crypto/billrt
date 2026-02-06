package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
// import androidx.navigation.fragment.navArgs
import com.linkbit.billrt.databinding.FragmentEditPelangganBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPelangganFragment : Fragment() {

    private var _binding: FragmentEditPelangganBinding? = null
    private val binding get() = _binding!!

    // private val args: EditPelangganFragmentArgs by navArgs()
    private var paketList = listOf<Paket>()
    private var wilayahList = listOf<Wilayah>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // populateFields()
        // fetchPaket()
        // fetchWilayah()

        binding.btnSimpan.setOnClickListener { /* saveChanges() */ }
    }

    /*
    private fun populateFields() {
        val pelanggan = args.pelanggan
        binding.etNamaPelanggan.setText(pelanggan.nama)
        binding.etAlamat.setText(pelanggan.alamat)
        binding.etTelepon.setText(pelanggan.telepon)
        binding.etMikrotikUsername.setText(pelanggan.mikrotikUsername)
        binding.etMacAddress.setText(pelanggan.macAddress)
        binding.tvTglDaftar.text = "Tanggal Daftar: ${pelanggan.tglDaftar}"
    }

    private fun fetchPaket() {
        setLoading(true)
        ApiConfig.getApiService().getPaket().enqueue(object : Callback<PaketResponse> {
            override fun onResponse(call: Call<PaketResponse>, response: Response<PaketResponse>) {
                if (_binding == null) return
                setLoading(false)
                if (response.isSuccessful) {
                    paketList = response.body()?.data ?: emptyList()
                    val paketNames = paketList.map { it.nama_paket }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, paketNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerPaket.adapter = adapter

                    val position = paketList.indexOfFirst { it.id_paket == args.pelanggan.idPaket }
                    if (position >= 0) {
                        binding.spinnerPaket.setSelection(position)
                    }
                } else {
                     Toast.makeText(context, "Gagal mengambil daftar paket", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PaketResponse>, t: Throwable) {
                if (_binding == null) return
                setLoading(false)
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchWilayah() {
        setLoading(true)
        ApiConfig.getApiService().getListWilayahSaja().enqueue(object : Callback<WilayahResponse> {
            override fun onResponse(call: Call<WilayahResponse>, response: Response<WilayahResponse>) {
                if (_binding == null) return
                setLoading(false)
                if (response.isSuccessful) {
                    wilayahList = response.body()?.data ?: emptyList()
                    val wilayahNames = wilayahList.map { it.nama_wilayah }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wilayahNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerWilayah.adapter = adapter

                    val position = wilayahList.indexOfFirst { it.id_wilayah == args.pelanggan.idWilayah }
                    if (position >= 0) {
                        binding.spinnerWilayah.setSelection(position)
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil daftar wilayah", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WilayahResponse>, t: Throwable) {
                if (_binding == null) return
                setLoading(false)
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveChanges() {
        val selectedPaketPosition = binding.spinnerPaket.selectedItemPosition
        val selectedWilayahPosition = binding.spinnerWilayah.selectedItemPosition

        if (paketList.isEmpty() || wilayahList.isEmpty() || selectedPaketPosition < 0 || selectedWilayahPosition < 0) {
            Toast.makeText(context, "Data paket dan wilayah belum terisi, silakan tunggu atau coba lagi.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = SimpanPelangganRequest(
            idPelanggan = args.pelanggan.idPelanggan,
            namaPelanggan = binding.etNamaPelanggan.text.toString(),
            alamatPelanggan = binding.etAlamat.text.toString(),
            teleponPelanggan = binding.etTelepon.text.toString(),
            idPaket = paketList[selectedPaketPosition].id_paket,
            idWilayah = wilayahList[selectedWilayahPosition].id_wilayah,
            mikrotikUsername = binding.etMikrotikUsername.text.toString(),
            mikrotikPassword = args.pelanggan.mikrotikPassword, // Not editable in this screen
            macAddress = binding.etMacAddress.text.toString(),
            latitude = args.pelanggan.latitude,
            longitude = args.pelanggan.longitude,
            tglDaftar = args.pelanggan.tglDaftar,
            tglExpired = args.pelanggan.tglExpired
        )

        setLoading(true)
        ApiConfig.getApiService().simpanPelanggan(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (_binding == null) return
                setLoading(false)
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Data pelanggan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                if (_binding == null) return
                setLoading(false)
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    */
    
    private fun setLoading(isLoading: Boolean) {
        if (_binding == null) return
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSimpan.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
