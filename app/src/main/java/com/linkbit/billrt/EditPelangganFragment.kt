package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.linkbit.billrt.databinding.FragmentEditPelangganBinding
import com.linkbit.billrt.model.StandardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class EditPelangganFragment : BaseFragment() {

    private var _binding: FragmentEditPelangganBinding? = null
    private val binding get() = _binding!!

    private val args: EditPelangganFragmentArgs by navArgs()

    private var paketList = listOf<Paket>()
    private var wilayahList = listOf<Wilayah>()
    private var currentPelanggan: PelangganData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sinkronisasi Insets agar toolbar tidak menabrak status bar
        applyWindowInsets(binding.appBarLayout)

        setupToolbar()
        fetchInitialData()
        setupListeners()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun fetchInitialData() {
        setLoading(true)
        fetchPaketAndWilayah()
        fetchPelangganDetails()
    }

    private fun setupListeners() {
        binding.btnPilihInstallationDate.setOnClickListener { showDatePickerDialog(isInstallationDate = true) }
        binding.btnPilihTglDaftar.setOnClickListener { showDatePickerDialog(isInstallationDate = false) }
        binding.btnUpdate.setOnClickListener { attemptUpdate() }
    }

    private fun populateFields(pelanggan: PelangganData) {
        currentPelanggan = pelanggan
        binding.etNamaPelanggan.setText(pelanggan.nama)
        binding.etAlamat.setText(pelanggan.alamat ?: "")
        binding.etTelepon.setText(pelanggan.telepon ?: "")
        binding.etMikrotikUsername.setText(pelanggan.mikrotikUsername ?: "")
        binding.etMikrotikPassword.setText(pelanggan.mikrotikPassword ?: "")
        binding.tvTglDaftar.text = pelanggan.tglDaftar ?: ""
        binding.tvInstallationDate.text = pelanggan.installationDate ?: ""

        // Set spinner selections
        val paketPosition = paketList.indexOfFirst { it.id_paket == pelanggan.idPaket }
        if (paketPosition != -1) {
            binding.spinnerPaket.setSelection(paketPosition)
        }

        val wilayahPosition = wilayahList.indexOfFirst { it.id_wilayah == pelanggan.idWilayah }
        if (wilayahPosition != -1) {
            binding.spinnerWilayah.setSelection(wilayahPosition)
        }
    }

    private fun showDatePickerDialog(isInstallationDate: Boolean) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val correctedMonth = month + 1
                val selectedDate = String.format("%d-%02d-%02d", year, correctedMonth, day)
                if (isInstallationDate) {
                    binding.tvInstallationDate.text = selectedDate
                } else {
                    binding.tvTglDaftar.text = selectedDate
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun fetchPaketAndWilayah() {
        // Fetch Paket
        apiService.getPaket().enqueue(object : Callback<PaketResponse> {
            override fun onResponse(call: Call<PaketResponse>, response: Response<PaketResponse>) {
                if (!isAdded) return
                if (response.isSuccessful && response.body()?.data != null) {
                    paketList = response.body()!!.data!!
                    val paketNames = paketList.map { it.nama_paket }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, paketNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerPaket.adapter = adapter
                } else {
                    Toast.makeText(context, "Gagal memuat data paket", Toast.LENGTH_SHORT).show()
                }
                checkIfDataReady()
            }

            override fun onFailure(call: Call<PaketResponse>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                checkIfDataReady()
            }
        })

        // Fetch Wilayah
        apiService.getWilayah().enqueue(object : Callback<WilayahResponse> {
            override fun onResponse(call: Call<WilayahResponse>, response: Response<WilayahResponse>) {
                if (!isAdded) return
                if (response.isSuccessful && response.body()?.data != null) {
                    wilayahList = response.body()!!.data!!
                    val wilayahNames = wilayahList.map { it.nama_wilayah }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wilayahNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerWilayah.adapter = adapter
                } else {
                    Toast.makeText(context, "Gagal memuat data wilayah", Toast.LENGTH_SHORT).show()
                }
                checkIfDataReady()
            }

            override fun onFailure(call: Call<WilayahResponse>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                checkIfDataReady()
            }
        })
    }

    private fun fetchPelangganDetails() {
        apiService.getPelangganById(args.pelangganIdToEdit).enqueue(object : Callback<GetPelangganByIdResponse> {
            override fun onResponse(call: Call<GetPelangganByIdResponse>, response: Response<GetPelangganByIdResponse>) {
                if (!isAdded) return
                if (response.isSuccessful && response.body()?.data != null) {
                    currentPelanggan = response.body()!!.data!!
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal memuat detail pelanggan"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
                checkIfDataReady()
            }

            override fun onFailure(call: Call<GetPelangganByIdResponse>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                checkIfDataReady()
            }
        })
    }

    private var dataReadyCounter = 3
    private fun checkIfDataReady() {
        dataReadyCounter--
        if (dataReadyCounter <= 0) {
            if (currentPelanggan != null && paketList.isNotEmpty() && wilayahList.isNotEmpty()) {
                if (!isAdded) return
                populateFields(currentPelanggan!!)
            }
            if (!isAdded) return
            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnUpdate.isEnabled = !isLoading
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
        return isValid
    }

    private fun attemptUpdate() {
        if (!validateInput() || currentPelanggan == null) return

        setLoading(true)

        val selectedPaket = if (paketList.isNotEmpty()) paketList[binding.spinnerPaket.selectedItemPosition] else null
        val selectedWilayah = if (wilayahList.isNotEmpty()) wilayahList[binding.spinnerWilayah.selectedItemPosition] else null

        // Buat password null jika tidak diisi agar backend tidak mengupdate field tersebut
        val inputPassword = binding.etMikrotikPassword.text.toString().trim()
        val finalPassword = if (inputPassword.isEmpty()) null else inputPassword

        val request = UpdatePelangganRequest(
            id_pelanggan = args.pelangganIdToEdit,
            nama_pelanggan = binding.etNamaPelanggan.text.toString().trim(),
            alamat_pelanggan = binding.etAlamat.text.toString().trim(),
            telepon_pelanggan = binding.etTelepon.text.toString().trim(),
            id_paket = selectedPaket?.id_paket,
            id_wilayah = selectedWilayah?.id_wilayah,
            mikrotik_username = binding.etMikrotikUsername.text.toString().trim(),
            mikrotik_password = finalPassword,
            installation_date = binding.tvInstallationDate.text.toString()
        )

        apiService.updatePelanggan("edit_pelanggan", request).enqueue(object : Callback<com.linkbit.billrt.model.StandardResponse> {
            override fun onResponse(call: Call<com.linkbit.billrt.model.StandardResponse>, response: Response<com.linkbit.billrt.model.StandardResponse>) {
                if (!isAdded) return
                setLoading(false)
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Data pelanggan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.setFragmentResult("edit_result", bundleOf("updated" to true))
                    findNavController().popBackStack()
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal memperbarui data"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<com.linkbit.billrt.model.StandardResponse>, t: Throwable) {
                if (!isAdded) return
                setLoading(false)
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
