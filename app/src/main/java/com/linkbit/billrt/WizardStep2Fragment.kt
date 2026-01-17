package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.linkbit.billrt.databinding.FragmentWizardStep2InfoJaringanBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class WizardStep2Fragment : Fragment() {

    private var _binding: FragmentWizardStep2InfoJaringanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TambahPelangganViewModel by activityViewModels()
    private val apiService: ApiService by lazy { ApiConfig.getApiService() }

    private var wilayahList: List<WilayahListItem> = emptyList()
    private var paketList: List<Paket> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWizardStep2InfoJaringanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchWilayah()
        fetchPaket()

        binding.etInstallationDate.setOnClickListener { showDatePicker() }

        binding.etInstallationDate.setText(viewModel.installationDate)
        binding.etMikrotikUsername.setText(viewModel.mikrotikUsername)
        binding.etMikrotikPassword.setText(viewModel.mikrotikPassword)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            binding.etInstallationDate.setText(selectedDate)
        }, year, month, day).show()
    }

    private fun fetchWilayah() {
        apiService.getWilayahList().enqueue(object : Callback<WilayahListResponse> {
            override fun onResponse(call: Call<WilayahListResponse>, response: Response<WilayahListResponse>) {
                if (response.isSuccessful) {
                    wilayahList = response.body()?.data ?: emptyList()
                    val wilayahNames = wilayahList.map { it.namaWilayah }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wilayahNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerWilayahTambah.adapter = adapter
                } else {
                    Toast.makeText(context, "Gagal memuat daftar wilayah", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<WilayahListResponse>, t: Throwable) {
                 Log.e("WizardStep2", "Gagal ambil data wilayah", t)
                 Toast.makeText(context, "Koneksi Gagal (Wilayah)", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPaket() {
        apiService.getPaket().enqueue(object : Callback<PaketResponse> {
            override fun onResponse(call: Call<PaketResponse>, response: Response<PaketResponse>) {
                if (response.isSuccessful) {
                    paketList = response.body()?.data ?: emptyList()
                    val paketNames = paketList.map { "${it.namaPaket} - Rp ${it.harga.toInt()}" }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, paketNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerPaket.adapter = adapter
                } else {
                    Toast.makeText(context, "Gagal memuat daftar paket", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<PaketResponse>, t: Throwable) {
                Log.e("WizardStep2", "Gagal ambil data paket", t)
                 Toast.makeText(context, "Koneksi Gagal (Paket)", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun saveData(): Boolean {
        val selectedWilayahPosition = binding.spinnerWilayahTambah.selectedItemPosition
        if (selectedWilayahPosition >= 0 && selectedWilayahPosition < wilayahList.size) {
            viewModel.idWilayah = wilayahList[selectedWilayahPosition].idWilayah
        }

        val selectedPaketPosition = binding.spinnerPaket.selectedItemPosition
        if (selectedPaketPosition >= 0 && selectedPaketPosition < paketList.size) {
            viewModel.idPaket = paketList[selectedPaketPosition].idPaket
        }

        viewModel.installationDate = binding.etInstallationDate.text.toString()
        viewModel.mikrotikUsername = binding.etMikrotikUsername.text.toString()
        viewModel.mikrotikPassword = binding.etMikrotikPassword.text.toString()
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
