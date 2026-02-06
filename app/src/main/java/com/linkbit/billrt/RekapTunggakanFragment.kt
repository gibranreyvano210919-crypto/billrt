package com.linkbit.billrt

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentRekapTunggakanBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.UUID

class RekapTunggakanFragment : BaseFragment() {

    private var _binding: FragmentRekapTunggakanBinding? = null
    private val binding get() = _binding!!

    private var selectedMonth: Int
    private var selectedYear: Int
    private lateinit var rekapAdapter: RekapTunggakanAdapter

    private lateinit var bluetoothPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var pendingPrintData: Pair<String, String>? = null

    init {
        val calendar = Calendar.getInstance()
        selectedMonth = calendar.get(Calendar.MONTH) + 1
        selectedYear = calendar.get(Calendar.YEAR)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRekapTunggakanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rekapAdapter = RekapTunggakanAdapter(emptyList(), 
            onShareClick = { rekapWilayah ->
                shareToWhatsApp(rekapWilayah)
            },
            onPrintClick = { rekapWilayah ->
                printRekap(rekapWilayah)
            }
        )
        binding.rvRekapTunggakan.layoutManager = LinearLayoutManager(context)
        binding.rvRekapTunggakan.adapter = rekapAdapter

        binding.swipeRefreshTunggakan.setOnRefreshListener {
            fetchRekapTunggakan()
        }

        setupSpinners()
        fetchRekapTunggakan()
    }

    private fun setupSpinners() {
        val months = DateFormatSymbols().months
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBulanTunggakan.adapter = monthAdapter
        binding.spinnerBulanTunggakan.setSelection(selectedMonth - 1)

        val years = (2020..selectedYear + 5).toList().map { it.toString() }
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTahunTunggakan.adapter = yearAdapter
        binding.spinnerTahunTunggakan.setSelection(years.indexOf(selectedYear.toString()))

        val itemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonth = binding.spinnerBulanTunggakan.selectedItemPosition + 1
                selectedYear = binding.spinnerTahunTunggakan.selectedItem.toString().toInt()
                fetchRekapTunggakan()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.spinnerBulanTunggakan.onItemSelectedListener = itemSelectedListener
        binding.spinnerTahunTunggakan.onItemSelectedListener = itemSelectedListener
    }

    private fun fetchRekapTunggakan() {
        binding.swipeRefreshTunggakan.isRefreshing = true
        apiService.getRekapTunggakan(bulan = selectedMonth, tahun = selectedYear).enqueue(object : Callback<RekapTunggakanResponse> {
            override fun onResponse(call: Call<RekapTunggakanResponse>, response: Response<RekapTunggakanResponse>) {
                if (_binding == null) return
                binding.swipeRefreshTunggakan.isRefreshing = false
                if (response.isSuccessful) {
                    val rekapResponse = response.body()
                    if (rekapResponse != null && rekapResponse.status) {
                        binding.tvTotalTunggakan.text = "Total Tunggakan: ${rekapResponse.totalTunggakan} Pelanggan"
                        binding.cardSummaryTunggakan.visibility = View.VISIBLE
                        rekapAdapter.updateData(rekapResponse.data)
                    } else {
                        Toast.makeText(context, "Gagal memuat data tunggakan", Toast.LENGTH_SHORT).show()
                        binding.cardSummaryTunggakan.visibility = View.GONE
                        rekapAdapter.updateData(emptyList())
                    }
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    binding.cardSummaryTunggakan.visibility = View.GONE
                    rekapAdapter.updateData(emptyList())
                }
            }

            override fun onFailure(call: Call<RekapTunggakanResponse>, t: Throwable) {
                if (_binding == null) return
                binding.swipeRefreshTunggakan.isRefreshing = false
                Toast.makeText(context, "Koneksi Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                binding.cardSummaryTunggakan.visibility = View.GONE
                rekapAdapter.updateData(emptyList())
            }
        })
    }

    private fun shareToWhatsApp(rekapWilayah: RekapTunggakanWilayah) {
        val builder = StringBuilder()
        builder.append("*REKAP TUNGGAKAN - ${rekapWilayah.namaWilayah}*\n\n")
        rekapWilayah.pelanggan.forEach { pelanggan ->
            builder.append("- ${pelanggan.namaPelanggan} (${pelanggan.mikrotikUsername})\n")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, builder.toString())
        }

        try {
            startActivity(intent)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp tidak terinstall.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun printRekap(rekapWilayah: RekapTunggakanWilayah) {
        val sharedPref = activity?.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE) ?: return
        val printerAddress = sharedPref.getString("SELECTED_PRINTER_ADDRESS", null)

        if (printerAddress.isNullOrEmpty()) {
            Toast.makeText(context, "Printer belum dipilih. Silakan atur di menu Pengaturan.", Toast.LENGTH_LONG).show()
            return
        }

        val dataToPrint = formatDataForPrinting(rekapWilayah)
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

    private fun formatDataForPrinting(rekap: RekapTunggakanWilayah): String {
        val builder = StringBuilder()
        builder.append("REKAP ( ${rekap.namaWilayah.uppercase()} )\n")
        rekap.pelanggan.forEach { pelanggan ->
            builder.append("- ${pelanggan.namaPelanggan} (${pelanggan.mikrotikUsername})\n")
        }
        
        builder.append("\n\n\n\n")
        
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
                // ESC/POS command for left alignment
                val leftAlign = byteArrayOf(0x1B, 0x61, 0x00)
                outputStream.write(leftAlign)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        pendingPrintData = null
    }
}