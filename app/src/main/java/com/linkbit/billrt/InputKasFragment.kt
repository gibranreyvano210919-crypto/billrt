package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.linkbit.billrt.adapter.PelangganChecklistAdapter
import com.linkbit.billrt.databinding.BottomSheetPreviewKasBinding
import com.linkbit.billrt.databinding.FragmentInputKasBinding
import com.linkbit.billrt.model.CatatanTagihanResponse
import com.linkbit.billrt.model.InputCatatanRequest
import com.linkbit.billrt.model.StandardResponse
import com.linkbit.billrt.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InputKasFragment : BaseFragment() {

    private var _binding: FragmentInputKasBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var pelangganAdapter: PelangganChecklistAdapter
    private var selectedPelanggans: List<InputKasPelanggan> = emptyList()
    private val selectedDate = Calendar.getInstance()

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInputKasBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.navBarSpacer.layoutParams.height = systemBars.bottom
            insets
        }

        binding.tvNamaTeknisiLogin.text = sessionManager.getUserName()

        setupDatePicker()
        setupMonthAndYearSpinners()
        setupRecyclerView()
        setupSearchEditText()
        fetchData()

        binding.btnSubmit.setOnClickListener {
            showConfirmationDialog()
        }

        binding.btnPreviewPilihan.setOnClickListener {
            showPreviewBottomSheet()
        }
    }

    private fun setupSearchEditText() {
        binding.etSearchPelanggan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    fetchData(s?.toString())
                }
                searchHandler.postDelayed(searchRunnable!!, 400)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showPreviewBottomSheet() {
        if (selectedPelanggans.isEmpty()) return

        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = BottomSheetPreviewKasBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val pelangganNames = selectedPelanggans.map { "${it.nama} (${it.idPelanggan})" }
        dialogBinding.rvPreviewPelanggan.layoutManager = LinearLayoutManager(requireContext())
        
        dialogBinding.rvPreviewPelanggan.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            inner class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
                val textView: android.widget.TextView = view.findViewById(android.R.id.text1)
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.dialog_list_item_mepet, parent, false)
                return ViewHolder(view)
            }
            override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
                (holder as ViewHolder).textView.text = pelangganNames[position]
            }
            override fun getItemCount(): Int = pelangganNames.size
        }

        dialogBinding.btnTutup.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showConfirmationDialog() {
        val selectedTeknisiName = sessionManager.getUserName()
        val bulanStr = binding.spinnerBulanInput.selectedItem.toString()
        val tahunStr = binding.spinnerTahunInput.selectedItem.toString()
        val nominal = binding.inputNominalSetor.text.toString()
        val keterangan = binding.inputKeterangan.text.toString()

        if (selectedPelanggans.isEmpty()) {
            Toast.makeText(context, "Pelanggan harus dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        val message = "Konfirmasi simpan catatan bulk:\n\n" +
                      "Teknisi: $selectedTeknisiName\n" +
                      "Periode: $bulanStr $tahunStr\n" +
                      "Total: ${selectedPelanggans.size} Pelanggan\n" +
                      "Nominal Per Item: Rp $nominal\n" +
                      "Keterangan: $keterangan"

        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Input")
            .setMessage(message)
            .setPositiveButton("Simpan Data") { _, _ ->
                submitCatatanInBulk()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupDatePicker() {
        updateDateInView()
        binding.inputTanggal.setOnClickListener { showDatePickerDialog() }
    }

    private fun setupMonthAndYearSpinners() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        val bulanArray = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val bulanAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bulanArray)
        binding.spinnerBulanInput.adapter = bulanAdapter
        binding.spinnerBulanInput.setSelection(currentMonth)

        val tahunArray = (currentYear - 2..currentYear + 2).map { it.toString() }
        val tahunAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tahunArray)
        binding.spinnerTahunInput.adapter = tahunAdapter
        binding.spinnerTahunInput.setSelection(tahunArray.indexOf(currentYear.toString()))

        val listener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                fetchData(binding.etSearchPelanggan.text.toString())
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        binding.spinnerBulanInput.onItemSelectedListener = listener
        binding.spinnerTahunInput.onItemSelectedListener = listener
    }

    private fun showDatePickerDialog() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, y, m, d ->
            selectedDate.set(y, m, d)
            updateDateInView()
        }, year, month, day).show()
    }

    private fun updateDateInView() {
        val myFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.inputTanggal.setText(sdf.format(selectedDate.time))
    }

    private fun setupRecyclerView() {
        pelangganAdapter = PelangganChecklistAdapter(emptyList()) { pelanggans ->
            selectedPelanggans = pelanggans
            updateSelectionUI()
        }
        binding.rvPelangganCheckable.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganCheckable.adapter = pelangganAdapter
    }

    private fun updateSelectionUI() {
        binding.tvPelangganTerpilih.text = if (selectedPelanggans.isEmpty()) {
            "Pelanggan Belum Dipilih"
        } else {
            "${selectedPelanggans.size} Pelanggan Dipilih"
        }
        binding.btnPreviewPilihan.isVisible = selectedPelanggans.isNotEmpty()
    }

    private fun fetchData(searchQuery: String? = null) {
        val bulan = binding.spinnerBulanInput.selectedItemPosition + 1
        val tahun = binding.spinnerTahunInput.selectedItem.toString().toInt()

        RetrofitClient.instance.getDataPelangganList(searchQuery).enqueue(object: Callback<PelangganListResponse>{
            override fun onResponse(call: Call<PelangganListResponse>, response: Response<PelangganListResponse>) {
                if (!isAdded || !response.isSuccessful) return

                val pelangganList = response.body()?.data ?: emptyList()
                val activePelangganList = pelangganList.filter { it.statusAktif == "aktif" }

                val tknId = sessionManager.getTeknisiId()
                val idTeknisi = if (tknId != -1) tknId.toString() else sessionManager.getUserId() ?: "-1"

                RetrofitClient.instance.getCatatanTagihan(idTeknisi, bulan, tahun).enqueue(object: Callback<CatatanTagihanResponse>{
                    override fun onResponse(call: Call<CatatanTagihanResponse>, response: Response<CatatanTagihanResponse>) {
                        val tercatatIds = if(response.isSuccessful) {
                            response.body()?.data?.flatMap { it.list }?.mapNotNull { it.idPelanggan }?.toSet() ?: emptySet()
                        } else {
                            emptySet()
                        }

                        val mergedList = activePelangganList.map { 
                            InputKasPelanggan(
                                idPelanggan = it.idPelanggan,
                                nama = it.namaPelanggan,
                                wilayah = null,
                                status = it.statusAktif,
                                macAddress = it.macAddress,
                                isTercatat = tercatatIds.contains(it.idPelanggan),
                                mikrotikUsername = it.mikrotikUsername
                            )
                        }
                        pelangganAdapter.updateData(mergedList)
                    }
                    override fun onFailure(call: Call<CatatanTagihanResponse>, t: Throwable) {
                        val list = activePelangganList.map { 
                             InputKasPelanggan(
                                idPelanggan = it.idPelanggan,
                                nama = it.namaPelanggan,
                                wilayah = null,
                                status = it.statusAktif,
                                macAddress = it.macAddress,
                                isTercatat = false,
                                mikrotikUsername = it.mikrotikUsername
                            )
                        }
                         pelangganAdapter.updateData(list)
                    }
                })
            }
            override fun onFailure(call: Call<PelangganListResponse>, t: Throwable) {}
        })
    }

    private fun submitCatatanInBulk() {
        val tknId = sessionManager.getTeknisiId()
        val idTeknisi = if (tknId != -1) tknId.toString() else sessionManager.getUserId() ?: "-1"

        if (idTeknisi == "-1") {
            Toast.makeText(context, "ID Teknisi tidak ditemukan", Toast.LENGTH_LONG).show()
            return
        }

        val tanggalCatat = binding.inputTanggal.text.toString()
        val bulanInt = binding.spinnerBulanInput.selectedItemPosition + 1
        val bulanStr = String.format("%02d", bulanInt)
        val tahunInt = binding.spinnerTahunInput.selectedItem.toString().toInt()
        
        val nominalInt = binding.inputNominalSetor.text.toString().filter { it.isDigit() }.toIntOrNull() ?: 0
        val keteranganStr = binding.inputKeterangan.text.toString()

        val pelangganIdList = selectedPelanggans.map { it.idPelanggan }

        val request = InputCatatanRequest(
            pelangganList = pelangganIdList,
            idTeknisi = idTeknisi,
            idTeknisiCollection = idTeknisi,
            idSetoran = 1,
            namaSetoran = "Setoran Tagihan Mobile",
            tanggalCatat = tanggalCatat,
            bulan = bulanStr,
            tahun = tahunInt,
            nominal = nominalInt,
            keterangan = keteranganStr,
            verified = 0
        )

        RetrofitClient.instance.tambahCatatan(bulanInt, tahunInt, request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, response.body()?.message ?: "Berhasil simpan bulk", Toast.LENGTH_SHORT).show()
                    
                    // Reset UI
                    binding.inputNominalSetor.text?.clear()
                    binding.inputKeterangan.text?.clear()
                    selectedPelanggans = emptyList()
                    pelangganAdapter.clearSelection()
                    updateSelectionUI()
                    
                    fetchData(binding.etSearchPelanggan.text.toString())
                } else {
                    Toast.makeText(context, "Gagal simpan data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
