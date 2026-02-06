package com.linkbit.billrt

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentPrinterSettingBinding
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission") // Permissions are handled by the fragment
class PrinterSettingFragment : Fragment() {

    private var _binding: FragmentPrinterSettingBinding? = null
    private val binding get() = _binding!!

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var deviceAdapter: BluetoothDeviceAdapter
    private val foundDevices = mutableListOf<BluetoothDevice>()

    private val requestBluetoothPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.values.all { it }) {
            startScan()
        } else {
            Toast.makeText(context, "Izin Bluetooth & Lokasi diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            Toast.makeText(context, "Bluetooth harus aktif untuk scan", Toast.LENGTH_SHORT).show()
            binding.switchBluetooth.isChecked = false
        }
    }

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let { 
                        if (it.name != null && !foundDevices.any { d -> d.address == it.address }) {
                           foundDevices.add(it)
                           deviceAdapter.updateData(foundDevices)
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    binding.progressBarScan.visibility = View.GONE
                    if (foundDevices.isEmpty()) {
                        binding.tvEmptyScan.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    
    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                binding.switchBluetooth.isChecked = state == BluetoothAdapter.STATE_ON
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPrinterSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupBluetoothSwitch()
        setupPaperSizeChips()
        loadAndDisplaySelectedPrinter()
        binding.btnScanBluetooth.setOnClickListener { checkPermissionsAndScan() }
        binding.btnTestPrint.setOnClickListener { performTestPrint() }

        val scanFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        scanFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        requireActivity().registerReceiver(scanReceiver, scanFilter)
        
        val stateFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        requireActivity().registerReceiver(bluetoothStateReceiver, stateFilter)
    }

    private fun setupRecyclerView() {
        deviceAdapter = BluetoothDeviceAdapter(emptyList()) { device ->
            val sharedPref = activity?.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE) ?: return@BluetoothDeviceAdapter
            with(sharedPref.edit()) {
                putString("SELECTED_PRINTER_ADDRESS", device.address)
                putString("SELECTED_PRINTER_NAME", device.name) 
                apply()
            }
            Toast.makeText(context, "Printer ${device.name} dipilih", Toast.LENGTH_SHORT).show()
            loadAndDisplaySelectedPrinter() 
        }
        binding.rvBluetoothDevices.layoutManager = LinearLayoutManager(context)
        binding.rvBluetoothDevices.adapter = deviceAdapter
    }
    
    private fun setupBluetoothSwitch(){
        binding.switchBluetooth.isChecked = bluetoothAdapter?.isEnabled == true
        binding.switchBluetooth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (bluetoothAdapter?.isEnabled == false) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBluetoothLauncher.launch(enableBtIntent)
                }
            } else {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                startActivity(intent)
            }
        }
    }

    private fun setupPaperSizeChips() {
        val sharedPref = activity?.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE) ?: return
        val selectedSize = sharedPref.getInt("PAPER_SIZE", 80)
        if (selectedSize == 58) {
            binding.chip58mm.isChecked = true
        } else {
            binding.chip80mm.isChecked = true
        }

        binding.chipGroupPaperSize.setOnCheckedChangeListener { group, checkedId ->
            val sizeToSave = if (checkedId == R.id.chip_58mm) 58 else 80
            with(sharedPref.edit()) {
                putInt("PAPER_SIZE", sizeToSave)
                apply()
            }
            Toast.makeText(context, "Ukuran kertas diatur ke ${sizeToSave}mm", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAndDisplaySelectedPrinter() {
        val sharedPref = activity?.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE) ?: return
        val printerName = sharedPref.getString("SELECTED_PRINTER_NAME", null)
        val printerAddress = sharedPref.getString("SELECTED_PRINTER_ADDRESS", null)

        if (printerAddress != null) {
            binding.tvSelectedPrinter.text = "${printerName ?: "Unknown"} ($printerAddress)"
            binding.btnTestPrint.isEnabled = true
        } else {
            binding.tvSelectedPrinter.text = "Belum ada printer yang dipilih"
            binding.btnTestPrint.isEnabled = false
        }
    }

    private fun performTestPrint() {
        val sharedPref = activity?.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE) ?: return
        val printerAddress = sharedPref.getString("SELECTED_PRINTER_ADDRESS", null)
        val paperSize = sharedPref.getInt("PAPER_SIZE", 80)

        if (printerAddress.isNullOrEmpty()) {
            Toast.makeText(context, "Tidak ada printer yang dipilih.", Toast.LENGTH_SHORT).show()
            return
        }

        val separator = "=".repeat(if (paperSize == 58) 32 else 48)
        val testData = "Ini adalah hasil test print ${paperSize}mm.\nKoneksi berhasil!\n$separator\n\n"
        printData(testData, printerAddress)
    }
    
    private fun printData(data: String, address: String) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return
        val printerDevice = bluetoothAdapter.getRemoteDevice(address)
        val sppUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        Thread {
            var socket: BluetoothSocket? = null
            try {
                activity?.runOnUiThread { Toast.makeText(context, "Mengirim test print...", Toast.LENGTH_SHORT).show() }
                socket = printerDevice.createRfcommSocketToServiceRecord(sppUuid)
                socket.connect()
                val outputStream = socket.outputStream
                outputStream.write(data.toByteArray())
                outputStream.flush()

                // --- FIX: Add a delay to allow the printer to process the data ---
                Thread.sleep(1000)

                activity?.runOnUiThread { Toast.makeText(context, "Test print berhasil dikirim.", Toast.LENGTH_SHORT).show() }
            } catch (e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread { Toast.makeText(context, "Gagal mengirim test print: ${e.message}", Toast.LENGTH_LONG).show() }
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

    private fun checkPermissionsAndScan() {
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Perangkat ini tidak mendukung Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val allPermissionsGranted = requiredPermissions.all { 
            ActivityCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allPermissionsGranted) {
            requestBluetoothPermissions.launch(requiredPermissions)
            return
        }

        startScan()
    }

    private fun startScan() {
        if (bluetoothAdapter?.isEnabled == false) {
            Toast.makeText(context, "Bluetooth tidak aktif", Toast.LENGTH_SHORT).show()
            binding.switchBluetooth.isChecked = false
            return
        }

        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        
        foundDevices.clear()
        deviceAdapter.updateData(foundDevices)
        binding.progressBarScan.visibility = View.VISIBLE
        binding.tvEmptyScan.visibility = View.GONE
        bluetoothAdapter?.startDiscovery()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Safety check before calling isDiscovering on newer Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                if (bluetoothAdapter?.isDiscovering == true) {
                    bluetoothAdapter.cancelDiscovery()
                }
            }
        } else {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
        }
        
        requireActivity().unregisterReceiver(scanReceiver)
        requireActivity().unregisterReceiver(bluetoothStateReceiver)
        _binding = null
    }
}