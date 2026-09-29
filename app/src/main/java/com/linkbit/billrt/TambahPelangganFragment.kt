package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
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
    private lateinit var sessionManager: SessionManager

    private var paketList = listOf<Paket>()
    private var wilayahList = listOf<Wilayah>()
    private var routerList = listOf<MikrotikAccount>()
    private var loadingCounter = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Sinkronisasi Insets agar toolbar tidak menabrak status bar
        applyWindowInsets(binding.appBarLayout)

        setupToolbar()
        setupInitialData()
        setupListeners()
        fetchAllData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
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
        binding.btnSimpan.setOnClickListener { attemptSave() }
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
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private enum class DateType {
        INSTALLATION,
        REGISTER
    }

    private fun fetchAllData() {
        loadingCounter = 3
        setLoading(true)

        // Fetch Paket
        apiService.getPaket().enqueue(object : Callback<PaketResponse> {
            override fun onResponse(call: Call<PaketResponse>, response: Response<PaketResponse>) {
                if (!isAdded) return
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status && body.data != null) {
                        paketList = body.data!!
                        val paketDisplay = paketList.map { 
                            val name = it.nama_paket ?: "Paket Unknown"
                            val price = it.hargaFormat ?: "Rp ${String.format("%,.0f", it.harga)}"
                            "$name - $price"
                        }
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, paketDisplay)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerPaket.adapter = adapter
                    }
                }
                checkIfLoadingComplete()
            }
            override fun onFailure(call: Call<PaketResponse>, t: Throwable) {
                checkIfLoadingComplete()
            }
        })

        // Fetch Wilayah
        apiService.getWilayah().enqueue(object : Callback<WilayahResponse> {
            override fun onResponse(call: Call<WilayahResponse>, response: Response<WilayahResponse>) {
                if (!isAdded) return
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status && body.data != null) {
                        wilayahList = body.data!!
                        val wilayahNames = wilayahList.map { it.nama_wilayah ?: "Wilayah Unknown" }
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wilayahNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerWilayah.adapter = adapter
                    }
                }
                checkIfLoadingComplete()
            }
            override fun onFailure(call: Call<WilayahResponse>, t: Throwable) {
                checkIfLoadingComplete()
            }
        })

        // Fetch Routers
        apiService.getMikrotikAccounts().enqueue(object : Callback<MikrotikAccountsResponse> {
            override fun onResponse(call: Call<MikrotikAccountsResponse>, response: Response<MikrotikAccountsResponse>) {
                if (!isAdded) return
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status && body.data != null) {
                        routerList = body.data!!
                        val routerNames = routerList.map { it.routerName ?: "Router Unknown" }
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, routerNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerRouter.adapter = adapter
                    }
                }
                checkIfLoadingComplete()
            }
            override fun onFailure(call: Call<MikrotikAccountsResponse>, t: Throwable) {
                checkIfLoadingComplete()
            }
        })
    }

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

        if (binding.spinnerRouter.selectedItemPosition < 0 || routerList.isEmpty()) {
            Toast.makeText(context, "Pilih router terlebih dahulu", Toast.LENGTH_SHORT).show()
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
        val selectedRouter = routerList[binding.spinnerRouter.selectedItemPosition]
        val currentUserId = sessionManager.getUserId()?.toIntOrNull()

        val request = SimpanPelangganRequest(
            idPelanggan = null, 
            namaPelanggan = binding.etNamaPelanggan.text.toString().trim(),
            alamatPelanggan = binding.etAlamat.text.toString().trim(),
            teleponPelanggan = binding.etTelepon.text.toString().trim(),
            idPaket = selectedPaket.id_paket,
            idWilayah = selectedWilayah.id_wilayah,
            idRouter = selectedRouter.id,
            mikrotikUsername = binding.etMikrotikUsername.text.toString().trim(),
            mikrotikPassword = binding.etMikrotikPassword.text.toString().takeIf { it.isNotBlank() } ?: "12345",
            installationDate = viewModel.installationDate.value,
            tglDaftar = viewModel.tglDaftar.value,
            macAddress = null,
            localIp = null,
            latitude = null,
            longitude = null,
            tglExpired = null,
            idUser = currentUserId
        )

        apiService.tambahPelanggan(request).enqueue(object : Callback<TambahPelangganResponse> {
            override fun onResponse(call: Call<TambahPelangganResponse>, response: Response<TambahPelangganResponse>) {
                setLoading(false)
                val body = response.body()
                if (response.isSuccessful && body?.status == true) {
                    val syncMsg = body.mikrotikSync?.message ?: ""
                    val successMsg = "${body.message}\n$syncMsg".trim()
                    Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                } else {
                    val errorMsg = body?.message ?: "Terjadi kesalahan yang tidak diketahui."
                    Toast.makeText(context, "Gagal menambahkan: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<TambahPelangganResponse>, t: Throwable) {
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
