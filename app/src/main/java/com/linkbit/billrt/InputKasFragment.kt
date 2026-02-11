package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PelangganChecklistAdapter
import com.linkbit.billrt.databinding.FragmentInputKasBinding
import com.linkbit.billrt.model.CatatanTagihanResponse
import com.linkbit.billrt.model.InputCatatanRequest
import com.linkbit.billrt.model.MasterTeknisi
import com.linkbit.billrt.model.MasterTeknisiResponse
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

    private var teknisiList: List<MasterTeknisi> = emptyList()
    private lateinit var pelangganAdapter: PelangganChecklistAdapter
    private var selectedPelanggans: List<InputKasPelanggan> = emptyList()
    private val selectedDate = Calendar.getInstance()

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInputKasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.navBarSpacer.layoutParams.height = systemBars.bottom
            insets
        }

        setupDatePicker()
        setupMonthAndYearSpinners()
        setupRecyclerView()
        setupSearchView()
        fetchTeknisi()
        fetchData() // Initial fetch without search term

        binding.btnSubmit.setOnClickListener {
            showConfirmationDialog()
        }

        binding.infoIcon.setOnClickListener {
            showSelectedPelangganDialog()
        }
    }

    private fun showSelectedPelangganDialog() {
        val pelangganNames = selectedPelanggans.map { it.nama }.toTypedArray()
        val adapter = ArrayAdapter(requireContext(), R.layout.dialog_list_item_mepet, pelangganNames)

        AlertDialog.Builder(requireContext())
            .setTitle("Pelanggan Terpilih (${pelangganNames.size})")
            .setAdapter(adapter, null)
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun showConfirmationDialog() {
        val selectedTeknisiName = binding.namaTeknisi.text.toString()
        val bulan = binding.spinnerBulanInput.selectedItem.toString()
        val tahun = binding.spinnerTahunInput.selectedItem.toString()

        if (selectedPelanggans.isEmpty() || selectedTeknisiName.isEmpty()) {
            Toast.makeText(context, "Teknisi dan Pelanggan harus dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        val message = "Konfirmasi data berikut:\n\n" +
                      "Teknisi: $selectedTeknisiName\n" +
                      "Periode: $bulan $tahun\n" +
                      "Total Pelanggan: ${selectedPelanggans.size}"

        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Input")
            .setMessage(message)
            .setPositiveButton("Konfirmasi & Kirim") { _, _ ->
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

        val tahunArray = (currentYear - 5..currentYear + 5).map { it.toString() }
        val tahunAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tahunArray)
        binding.spinnerTahunInput.adapter = tahunAdapter
        binding.spinnerTahunInput.setSelection(tahunArray.indexOf(currentYear.toString()))

        val listener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                fetchData(binding.searchViewPelanggan.query.toString())
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
            binding.tvPelangganTerpilih.text = if (pelanggans.isEmpty()) {
                "Pelanggan Belum Dipilih"
            } else {
                "${pelanggans.size} Pelanggan Dipilih"
            }
            binding.infoIcon.isVisible = pelanggans.isNotEmpty()
        }
        binding.rvPelangganCheckable.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganCheckable.adapter = pelangganAdapter
    }

    private fun setupSearchView() {
        binding.searchViewPelanggan.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    fetchData(newText)
                }
                searchHandler.postDelayed(searchRunnable!!, 300) // Debounce for 300ms
                return true
            }
        })
    }

    private fun fetchTeknisi() {
        RetrofitClient.instance.getMasterTeknisi().enqueue(object : Callback<MasterTeknisiResponse> {
            override fun onResponse(call: Call<MasterTeknisiResponse>, response: Response<MasterTeknisiResponse>) {
                if (!isAdded || _binding == null) return
                if (response.isSuccessful) {
                    teknisiList = response.body()?.data ?: emptyList()
                    val teknisiNames = teknisiList.map { it.namaTeknisi }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, teknisiNames)
                    binding.namaTeknisi.setAdapter(adapter)
                }
            }
            override fun onFailure(call: Call<MasterTeknisiResponse>, t: Throwable) { /* Handle failure */ }
        })
    }

    private fun fetchData(searchQuery: String? = null) {
        val bulan = binding.spinnerBulanInput.selectedItemPosition + 1
        val tahun = binding.spinnerTahunInput.selectedItem.toString().toInt()

        RetrofitClient.instance.getDataPelangganList(searchQuery).enqueue(object: Callback<PelangganListResponse>{
            override fun onResponse(call: Call<PelangganListResponse>, response: Response<PelangganListResponse>) {
                if (!isAdded || !response.isSuccessful) return

                val pelangganList = response.body()?.data ?: emptyList()

                RetrofitClient.instance.getCatatanTagihan(bulan, tahun).enqueue(object: Callback<CatatanTagihanResponse>{
                    override fun onResponse(call: Call<CatatanTagihanResponse>, response: Response<CatatanTagihanResponse>) {
                        val tercatatIds = if(response.isSuccessful) {
                            response.body()?.data?.flatMap { it.list }?.mapNotNull { it.idPelanggan }?.toSet() ?: emptySet()
                        } else {
                            emptySet()
                        }

                        val mergedList = pelangganList.map { 
                            InputKasPelanggan(
                                idPelanggan = it.idPelanggan,
                                nama = it.namaPelanggan,
                                wilayah = null, // Wilayah is not in PelangganListItem
                                status = it.statusAktif,
                                macAddress = it.macAddress,
                                isTercatat = tercatatIds.contains(it.idPelanggan),
                                mikrotikUsername = it.mikrotikUsername
                            )
                        }
                        pelangganAdapter.updateData(mergedList)
                    }
                    override fun onFailure(call: Call<CatatanTagihanResponse>, t: Throwable) {
                        val list = pelangganList.map { 
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
        val selectedTeknisiName = binding.namaTeknisi.text.toString()
        val selectedTeknisi = teknisiList.find { it.namaTeknisi == selectedTeknisiName }

        if (selectedTeknisi == null) {
            Toast.makeText(context, "Teknisi tidak valid. Harap pilih dari daftar.", Toast.LENGTH_LONG).show()
            return
        }

        val tanggalCatat = binding.inputTanggal.text.toString()
        val bulan = binding.spinnerBulanInput.selectedItemPosition + 1
        val tahun = binding.spinnerTahunInput.selectedItem.toString().toInt()

        val pelangganIdList = selectedPelanggans.map { it.idPelanggan }

        val request = InputCatatanRequest(
            pelangganList = pelangganIdList,
            idTeknisi = selectedTeknisi.id.toString(),
            tanggalCatat = tanggalCatat
        )

        RetrofitClient.instance.tambahCatatan(bulan, tahun, request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (!isAdded || _binding == null) return
                
                val responseBody = response.body()
                if (response.isSuccessful && responseBody?.status == true) {
                    Toast.makeText(context, responseBody.message, Toast.LENGTH_LONG).show()
                    
                    binding.namaTeknisi.text.clear()
                    pelangganAdapter.clearSelection()
                    binding.tvPelangganTerpilih.text = "Pelanggan Belum Dipilih"
                    parentFragmentManager.setFragmentResult("kas_updated", Bundle.EMPTY)
                    fetchData() // Refresh list
                } else {
                    Toast.makeText(context, "Gagal: ${responseBody?.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        super.onDestroyView()
        _binding = null
    }
}
