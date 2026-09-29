package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentPengaturanBinding
import com.linkbit.billrt.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PengaturanFragment : Fragment() {

    private var _binding: FragmentPengaturanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPengaturanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        loadPengaturan()

        binding.btnSimpan.setOnClickListener {
            savePengaturan()
        }
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Pengaturan"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadPengaturan() {
        ApiClient.instance.getPengaturan().enqueue(object : Callback<PengaturanResponse> {
            override fun onResponse(call: Call<PengaturanResponse>, response: Response<PengaturanResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { updateUi(it) }
                } else {
                    Toast.makeText(context, "Gagal memuat pengaturan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PengaturanResponse>, t: Throwable) {
                Toast.makeText(context, "Gagal memuat pengaturan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun savePengaturan() {
        val pengaturanId = 1

        val namaPerusahaan = binding.etNamaPerusahaan.text.toString()
        val alamatPerusahaan = binding.etAlamatPerusahaan.text.toString()
        val teleponPerusahaan = binding.etTeleponPerusahaan.text.toString()
        val emailPerusahaan = binding.etEmailPerusahaan.text.toString()
        val bankNama = binding.etBankNama.text.toString()
        val bankAtasNama = binding.etBankAtasNama.text.toString()
        val bankNoRekening = binding.etBankNoRekening.text.toString()
        val masaTenggang = binding.etMasaTenggang.text.toString().toIntOrNull() ?: 0

        ApiClient.instance.updatePengaturan(
            id = pengaturanId,
            nama = namaPerusahaan,
            alamat = alamatPerusahaan,
            telepon = teleponPerusahaan,
            email = emailPerusahaan,
            bank = bankNama,
            an = bankAtasNama,
            norek = bankNoRekening,
            tenggang = masaTenggang
        ).enqueue(object : Callback<UpdatePengaturanResponse> {
            override fun onResponse(call: Call<UpdatePengaturanResponse>, response: Response<UpdatePengaturanResponse>) {
                if (response.isSuccessful) {
                    val updateResponse = response.body()
                    if (updateResponse != null && updateResponse.status) {
                        Toast.makeText(context, updateResponse.message, Toast.LENGTH_SHORT).show()
                        // Langsung perbarui UI dengan data dari respons
                        updateResponse.data?.let { updateUi(it) }
                    } else {
                        val errorMessage = updateResponse?.message ?: "Gagal menyimpan pengaturan"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Gagal menyimpan pengaturan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdatePengaturanResponse>, t: Throwable) {
                Toast.makeText(context, "Gagal menyimpan pengaturan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUi(data: PengaturanResponse) {
        binding.etNamaPerusahaan.setText(data.namaPerusahaan)
        binding.etAlamatPerusahaan.setText(data.alamatPerusahaan)
        binding.etTeleponPerusahaan.setText(data.teleponPerusahaan)
        binding.etEmailPerusahaan.setText(data.emailPerusahaan)
        binding.etBankNama.setText(data.bankNama)
        binding.etBankAtasNama.setText(data.bankAtasNama)
        binding.etBankNoRekening.setText(data.bankNoRekening)
        binding.etMasaTenggang.setText(data.masaTenggang)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
