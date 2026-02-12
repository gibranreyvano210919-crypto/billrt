package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentTambahPelangganBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TambahPelangganFragment : BaseFragment() {

    private var _binding: FragmentTambahPelangganBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TambahPelangganViewModel by activityViewModels()

    private var paketList = listOf<Paket>()
    private var wilayahList = listOf<Wilayah>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInitialData()
        setupListeners()
        fetchPaketAndWilayah()
    }

    private fun setupInitialData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        binding.tvInstallationDate.text = today
        viewModel.installationDate.value = today
        binding.tvTglDaftar.text = today
        viewModel.tglDaftar.value = today
    }

    private fun setupListeners() {
        binding.btnPilihInstallationDate.setOnClickListener { showDatePickerDialog(dateType = DateType.INSTALLATION) }
        binding.btnPilihTglDaftar.setOnClickListener { showDatePickerDialog(dateType = DateType.REGISTER) }
        binding.btnPilihTglExpired.setOnClickListener { showDatePickerDialog(dateType = DateType.EXPIRED) }
        binding.btnSimpan.setOnClickListener { attemptSave() }
    }

    private enum class DateType {
        INSTALLATION,
        REGISTER,
        EXPIRED
    }

    private fun showDatePickerDialog(dateType: DateType) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val correctedMonth = month + 1
                val selectedDate = String.format("%d-%02d-%02d", year, correctedMonth, day)
                when (dateType) {
                    DateType.INSTALLATION -> {
                        binding.tvInstallationDate.text = selectedDate
                        viewModel.installationDate.value = selectedDate
                    }
                    DateType.REGISTER -> {
                        binding.tvTglDaftar.text = selectedDate
                        viewModel.tglDaftar.value = selectedDate
                    }
                    DateType.EXPIRED -> {
                        binding.tvTglExpired.text = selectedDate
                        viewModel.tglExpired.value = selectedDate
                    }
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun fetchPaketAndWilayah() {
        setLoading(true)
        // Fetch Paket
        apiService.getPaket().enqueue(object : Callback<PaketResponse> {
            override fun onResponse(call: Call<PaketResponse>, response: Response<PaketResponse>) {
                if (response.isSuccessful && response.body()?.data != null) {
                    paketList = response.body()!!.data!!
                    val paketNames = paketList.map { it.nama_paket }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, paketNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerPaket.adapter = adapter
                } else {
                    Toast.makeText(context, "Gagal memuat data paket", Toast.LENGTH_SHORT).show()
                }
                checkIfLoadingComplete()
            }

            override fun onFailure(call: Call<PaketResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                checkIfLoadingComplete()
            }
        })

        // Fetch Wilayah
        apiService.getWilayah().enqueue(object : Callback<WilayahResponse> {
            override fun onResponse(call: Call<WilayahResponse>, response: Response<WilayahResponse>) {
                if (response.isSuccessful && response.body()?.data != null) {
                    wilayahList = response.body()!!.data!!
                    val wilayahNames = wilayahList.map { it.nama_wilayah }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wilayahNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerWilayah.adapter = adapter
                } else {
                    Toast.makeText(context, "Gagal memuat data wilayah", Toast.LENGTH_SHORT).show()
                }
                checkIfLoadingComplete()
            }

            override fun onFailure(call: Call<WilayahResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                checkIfLoadingComplete()
            }
        })
    }

    private var loadingCounter = 2
    private fun checkIfLoadingComplete() {
        loadingCounter--
        if (loadingCounter <= 0) {
            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSimpan.isEnabled = !isLoading
    }

    private fun validateInput(): Boolean {
        var isValid = true
        if (binding.etNamaPelanggan.text.isNullOrBlank()) {
            binding.tilNamaPelanggan.error = "Nama tidak boleh kosong"
            isValid = false
        } else {
            binding.tilNamaPelanggan.error = null
        }

        if (binding.etMikrotikUsername.text.isNullOrBlank()) {
            binding.tilMikrotikUsername.error = "Username Mikrotik tidak boleh kosong"
            isValid = false
        } else {
            binding.tilMikrotikUsername.error = null
        }

        if (binding.spinnerPaket.selectedItemPosition < 0 || paketList.isEmpty()) {
            Toast.makeText(context, "Pilih paket terlebih dahulu", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (binding.spinnerWilayah.selectedItemPosition < 0 || wilayahList.isEmpty()) {
            Toast.makeText(context, "Pilih wilayah terlebih dahulu", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun attemptSave() {
        if (!validateInput()) {
            Toast.makeText(context, "Harap periksa kembali semua data wajib diisi.", Toast.LENGTH_LONG).show()
            return
        }

        setLoading(true)

        val selectedPaket = paketList[binding.spinnerPaket.selectedItemPosition]
        val selectedWilayah = wilayahList[binding.spinnerWilayah.selectedItemPosition]

        val request = SimpanPelangganRequest(
            idPelanggan = null, // Selalu null untuk pelanggan baru
            namaPelanggan = binding.etNamaPelanggan.text.toString().trim(),
            alamatPelanggan = binding.etAlamat.text.toString().trim(),
            teleponPelanggan = binding.etTelepon.text.toString().trim(),
            idPaket = selectedPaket.id_paket,
            idWilayah = selectedWilayah.id_wilayah,
            mikrotikUsername = binding.etMikrotikUsername.text.toString().trim(),
            mikrotikPassword = binding.etMikrotikPassword.text.toString(), // Password tidak di-trim
            installationDate = viewModel.installationDate.value,
            tglDaftar = viewModel.tglDaftar.value,
            macAddress = null,
            latitude = null,
            longitude = null,
            tglExpired = viewModel.tglExpired.value
        )

        apiService.tambahPelanggan(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                setLoading(false)
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Pelanggan baru berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    val errorMsg = response.body()?.message ?: "Terjadi kesalahan yang tidak diketahui."
                    Toast.makeText(context, "Gagal menambahkan: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                setLoading(false)
                Toast.makeText(context, "Error koneksi: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
