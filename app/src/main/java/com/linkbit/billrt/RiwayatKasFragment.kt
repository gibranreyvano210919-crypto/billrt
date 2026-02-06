package com.linkbit.billrt

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentRiwayatKasBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.Calendar
import java.util.UUID

class RiwayatKasFragment : BaseFragment() {

    private var _binding: FragmentRiwayatKasBinding? = null
    private val binding get() = _binding!!
    private lateinit var groupedAdapter: RiwayatKasGroupedAdapter
    private var originalGroupedData: List<TanggalGroup> = emptyList()

    private lateinit var bluetoothPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var pendingPrintData: Pair<String, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentFragmentManager.setFragmentResultListener("kas_updated", this) { _, _ ->
            if (isResumed) {
                fetchGroupedRiwayat()
            }
        }

        bluetoothPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                pendingPrintData?.let {
                    printToBluetoothPrinter(it.first, it.second)
                    pendingPrintData = null
                }
            } else {
                Toast.makeText(context, "Izin Bluetooth diperlukan untuk mencetak.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRiwayatKasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeToRefresh()
        setupFilters()
        setupSearchView()
        fetchGroupedRiwayat()
    }

    private fun setupRecyclerView() {
        groupedAdapter = RiwayatKasGroupedAdapter(
            emptyList(),
            onPrintClick = { tanggal -> handlePrintRequest(tanggal) },
            onDeleteClick = { catatan -> showDeleteConfirmationDialog(catatan) },
            onItemClick = { catatan -> groupedAdapter.toggleSelection(catatan.id) },
            onSelectAllClick = { tanggal -> groupedAdapter.toggleSelectAllInGroup(tanggal) },
            onVerifyToggle = { catatan -> toggleVerificationStatus(catatan) }
        )
        binding.rvRiwayatKas.layoutManager = LinearLayoutManager(context)
        binding.rvRiwayatKas.adapter = groupedAdapter
    }

    private fun toggleVerificationStatus(catatan: CatatanKasItem) {
        if (catatan.verified == 1) {
            unverifyCatatan(catatan)
        } else {
            verifyCatatan(catatan)
        }
    }

    private fun handlePrintRequest(tanggal: String) {
        val sharedPref = activity?.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE) ?: return
        val printerAddress = sharedPref.getString("SELECTED_PRINTER_ADDRESS", null)

        if (printerAddress.isNullOrEmpty()) {
            Toast.makeText(context, "Printer belum dipilih. Silakan atur di menu Pengaturan.", Toast.LENGTH_LONG).show()
            return
        }

        val dataToPrint = formatDataForPrinting(tanggal)
        if (dataToPrint.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val hasConnectPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                val hasScanPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

                if (hasConnectPermission && hasScanPermission) {
                    printToBluetoothPrinter(dataToPrint, printerAddress)
                } else {
                    pendingPrintData = Pair(dataToPrint, printerAddress)
                    bluetoothPermissionLauncher.launch(
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
                    )
                }
            } else {
                printToBluetoothPrinter(dataToPrint, printerAddress)
            }
        } else {
            Toast.makeText(context, "Tidak ada data untuk dicetak.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatDataForPrinting(tanggal: String): String {
        val sharedPref = activity?.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE) ?: return ""
        val paperSize = sharedPref.getInt("PAPER_SIZE", 80)
        val separatorLength = if (paperSize == 58) 32 else 48
        val mainSeparator = "=".repeat(separatorLength)

        val group = originalGroupedData.find { it.tanggal == tanggal }
        if (group == null) return ""

        val selectedItems = groupedAdapter.getSelectedItemsInGroup(tanggal)
        val itemsToPrint = if (selectedItems.isNotEmpty()) selectedItems else group.list

        if (itemsToPrint.isEmpty()) return ""

        val builder = StringBuilder()
        builder.append("Laporan Catatan - $tanggal\n")
        builder.append("$mainSeparator\n")
        itemsToPrint.forEach { 
            builder.append("Pelanggan: ${it.namaPelanggan} (ID: ${it.idPelanggan ?: "-"})\n")
            builder.append("Wilayah  : ${it.wilayah ?: "-"}\n")
            builder.append("Username : ${it.mikrotikUsername ?: "-"}\n")
            builder.append("Teknisi  : ${it.namaTeknisi ?: "N/A"}\n\n")
        }

        builder.append("\n\n\n\n")

        groupedAdapter.clearSelection()

        return builder.toString()
    }

    @SuppressLint("MissingPermission")
    private fun printToBluetoothPrinter(dataToPrint: String, printerAddress: String) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth tidak didukung perangkat ini.", Toast.LENGTH_SHORT).show()
            return
        }

        val printerDevice = bluetoothAdapter.getRemoteDevice(printerAddress)
        val sppUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        Thread {
            var socket: BluetoothSocket? = null
            try {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Menghubungkan ke printer...", Toast.LENGTH_SHORT).show()
                }

                socket = printerDevice.createRfcommSocketToServiceRecord(sppUuid)
                socket.connect()

                val outputStream = socket.outputStream
                outputStream.write(dataToPrint.toByteArray())
                outputStream.flush()

                Thread.sleep(1000)

                activity?.runOnUiThread {
                    Toast.makeText(context, "Data berhasil dikirim ke printer.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(context, "Gagal terhubung atau mencetak: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                try {
                    socket?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshRiwayat.setOnRefreshListener { fetchGroupedRiwayat() }
    }

    private fun setupFilters() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        val bulanArray = arrayOf("Semua Bulan", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val bulanAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bulanArray)
        binding.spinnerBulan.adapter = bulanAdapter
        binding.spinnerBulan.setSelection(currentMonth + 1)

        val tahunArray = (currentYear - 5..currentYear).map { it.toString() }.toMutableList()
        tahunArray.add(0, "Semua Tahun")
        val tahunAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tahunArray)
        binding.spinnerTahun.adapter = tahunAdapter
        binding.spinnerTahun.setSelection(tahunArray.indexOf(currentYear.toString()))

        val itemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { fetchGroupedRiwayat() }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.spinnerBulan.onItemSelectedListener = itemSelectedListener
        binding.spinnerTahun.onItemSelectedListener = itemSelectedListener
    }

    private fun setupSearchView() {
        binding.searchViewRiwayat.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                fetchGroupedRiwayat()
                return true
            }
        })
    }

    private fun fetchGroupedRiwayat() {
        binding.swipeRefreshRiwayat.isRefreshing = true
        binding.tvEmptyRiwayat.visibility = View.GONE

        val bulanPos = binding.spinnerBulan.selectedItemPosition
        val bulan = if (bulanPos == 0) null else bulanPos

        val tahunSpinnerVal = binding.spinnerTahun.selectedItem.toString()
        val tahun = if (tahunSpinnerVal == "Semua Tahun") null else tahunSpinnerVal.toInt()

        val searchQuery = binding.searchViewRiwayat.query.toString()

        apiService.getCatatanTagihan(search = searchQuery, bulan = bulan ?: 0, tahun = tahun ?: 0).enqueue(object : Callback<GroupedKasResponse> {
            override fun onResponse(call: Call<GroupedKasResponse>, response: Response<GroupedKasResponse>) {
                if (!isAdded || _binding == null) return
                binding.swipeRefreshRiwayat.isRefreshing = false

                if (response.isSuccessful) {
                    val kasResponse = response.body()
                    if (kasResponse != null && kasResponse.status) {
                        originalGroupedData = kasResponse.data
                        val flattenedList = flattenGroupedData(originalGroupedData)
                        groupedAdapter.updateData(flattenedList)
                        binding.tvEmptyRiwayat.visibility = if (flattenedList.isEmpty()) View.VISIBLE else View.GONE

                        // Display global rekap
                        binding.tvTotalGlobal.text = "Total Catatan Bulan Ini: ${kasResponse.totalGlobal}"
                        val rekapGlobalText = kasResponse.rekapGlobal.joinToString("\n") { "- ${it.nama}: ${it.jumlah}" }
                        binding.tvRekapGlobalTeknisi.text = rekapGlobalText
                        binding.cardRekapGlobal.visibility = View.VISIBLE
                    } else {
                        handleFailure()
                    }
                } else {
                    handleFailure()
                }
            }

            override fun onFailure(call: Call<GroupedKasResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                binding.swipeRefreshRiwayat.isRefreshing = false
                handleFailure(t.message)
            }
        })
    }

    private fun flattenGroupedData(groupedData: List<TanggalGroup>): List<RiwayatKasListItem> {
        val flattenedList = mutableListOf<RiwayatKasListItem>()
        for (group in groupedData) {
            flattenedList.add(RiwayatKasListItem.Header(group)) // Pass the whole group
            for (item in group.list) {
                flattenedList.add(RiwayatKasListItem.Item(item))
            }
        }
        return flattenedList
    }

    private fun showDeleteConfirmationDialog(catatan: CatatanKasItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Catatan")
            .setMessage("Anda yakin ingin menghapus catatan untuk ${catatan.namaPelanggan}?")
            .setPositiveButton("Hapus") { _, _ -> deleteCatatanById(catatan.id) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteCatatanById(id: Int) {
        val request = HapusCatatanRequest(id = id)
        apiService.hapusCatatan(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Catatan berhasil dihapus", Toast.LENGTH_SHORT).show()
                    fetchGroupedRiwayat()
                } else {
                    Toast.makeText(context, "Gagal menghapus: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<StandardResponse>, t: Throwable) { Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show() }
        })
    }

    private fun verifyCatatan(catatan: CatatanKasItem) {
        val request = VerifyCatatanRequest(id = catatan.id)
        apiService.verifyCatatan(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Catatan berhasil diverifikasi", Toast.LENGTH_SHORT).show()
                    fetchGroupedRiwayat()
                } else {
                    Toast.makeText(context, "Gagal verifikasi: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun unverifyCatatan(catatan: CatatanKasItem) {
        val request = UnverifyCatatanRequest(id = catatan.id)
        apiService.unverifyCatatan(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Verifikasi catatan dibatalkan", Toast.LENGTH_SHORT).show()
                    fetchGroupedRiwayat()
                } else {
                    Toast.makeText(context, "Gagal membatalkan verifikasi: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleFailure(message: String? = "Gagal mengambil riwayat") {
        if (!isAdded || _binding == null) return
        binding.tvEmptyRiwayat.visibility = View.VISIBLE
        binding.cardRekapGlobal.visibility = View.GONE
        groupedAdapter.updateData(emptyList())
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}