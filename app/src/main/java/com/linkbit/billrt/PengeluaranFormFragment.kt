package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.linkbit.billrt.api.RetrofitClient
import com.linkbit.billrt.databinding.FragmentPengeluaranFormBinding
import com.linkbit.billrt.model.MasterKategoriResponse
import com.linkbit.billrt.model.StandardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class PengeluaranFormFragment : Fragment() {

    private var _binding: FragmentPengeluaranFormBinding? = null
    private val binding get() = _binding!!
    private val args: PengeluaranFormFragmentArgs by navArgs()

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPengeluaranFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        isEditMode = args.pengeluaranItem != null
        setupToolbar()
        setupDropdowns()
        setupDatePicker()
        
        if (isEditMode) {
            populateData()
            binding.btnHapus.visibility = View.VISIBLE
        } else {
            val targetBulan = args.bulan
            val targetTahun = args.tahun
            val targetCalendar = Calendar.getInstance()
            
            if (targetBulan != 0 && targetTahun != 0) {
                val currentCal = Calendar.getInstance()
                if (targetBulan == currentCal.get(Calendar.MONTH) + 1 && targetTahun == currentCal.get(Calendar.YEAR)) {
                    // Default to today if it matches current month and year
                    binding.etTanggal.setText(dateFormat.format(currentCal.time))
                } else {
                    // Default to the 1st day of the target month/year
                    targetCalendar.set(Calendar.YEAR, targetTahun)
                    targetCalendar.set(Calendar.MONTH, targetBulan - 1)
                    targetCalendar.set(Calendar.DAY_OF_MONTH, 1)
                    binding.etTanggal.setText(dateFormat.format(targetCalendar.time))
                    // Sync calendar object for date picker
                    calendar.time = targetCalendar.time
                }
            } else {
                binding.etTanggal.setText(dateFormat.format(Date()))
            }
        }

        binding.btnSimpan.setOnClickListener { validateAndSave() }
        binding.btnHapus.setOnClickListener { showDeleteConfirmation() }
        
        loadMasterKategori()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (isEditMode) "Edit Pengeluaran" else "Tambah Pengeluaran"
        }
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupDropdowns() {
        val metodeList = arrayOf("Cash", "Transfer", "Lainnya")
        val adapterMetode = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, metodeList)
        binding.actMetode.setAdapter(adapterMetode)
        binding.actMetode.setText(metodeList[0], false)
    }

    private fun setupDatePicker() {
        binding.etTanggal.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    binding.etTanggal.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun loadMasterKategori() {
        RetrofitClient.instance.getMasterKategori().enqueue(object : Callback<MasterKategoriResponse> {
            override fun onResponse(call: Call<MasterKategoriResponse>, response: Response<MasterKategoriResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    val categories = response.body()?.data?.map { it.namaKategori } ?: emptyList()
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
                    binding.actKategori.setAdapter(adapter)
                }
            }
            override fun onFailure(call: Call<MasterKategoriResponse>, t: Throwable) {}
        })
    }

    private fun populateData() {
        args.pengeluaranItem?.let { item ->
            // Date from API is dd-mm-yyyy, we need yyyy-mm-dd for server
            val parser = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val date = parser.parse(item.tanggal) ?: Date()
            binding.etTanggal.setText(dateFormat.format(date))
            
            binding.actKategori.setText(item.kategori, false)
            binding.etKeterangan.setText(item.keterangan)
            binding.etJumlah.setText(item.jumlah.toInt().toString())
            binding.actMetode.setText(item.metodePembayaran, false)
            binding.etKeteranganLain.setText(item.keteranganLain)
        }
    }

    private fun validateAndSave() {
        val tanggal = binding.etTanggal.text.toString()
        val kategori = binding.actKategori.text.toString()
        val keterangan = binding.etKeterangan.text.toString()
        val jumlahStr = binding.etJumlah.text.toString()
        val metode = binding.actMetode.text.toString()
        val ketLain = binding.etKeteranganLain.text.toString()

        if (kategori.isEmpty() || keterangan.isEmpty() || jumlahStr.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua kolom wajib", Toast.LENGTH_SHORT).show()
            return
        }

        val body = mutableMapOf<String, Any>(
            "tanggal" to tanggal,
            "kategori" to kategori,
            "keterangan" to keterangan,
            "jumlah" to jumlahStr.toDouble(),
            "metode_pembayaran" to metode,
            "keterangan_lain" to ketLain
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSimpan.isEnabled = false

        val apiCall = if (isEditMode) {
            body["id_pengeluaran"] = args.pengeluaranItem!!.idPengeluaran
            RetrofitClient.instance.editPengeluaran(body)
        } else {
            RetrofitClient.instance.tambahPengeluaran(body)
        }

        apiCall.enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                binding.progressBar.visibility = View.GONE
                binding.btnSimpan.isEnabled = true
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(requireContext(), response.body()?.message ?: "Berhasil", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), response.body()?.message ?: "Gagal", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.btnSimpan.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Data")
            .setMessage("Apakah Anda yakin ingin menghapus data pengeluaran ini?")
            .setPositiveButton("Hapus") { _, _ -> deletePengeluaran() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deletePengeluaran() {
        val id = args.pengeluaranItem?.idPengeluaran ?: return
        binding.progressBar.visibility = View.VISIBLE
        
        RetrofitClient.instance.hapusPengeluaran(id).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(requireContext(), "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
