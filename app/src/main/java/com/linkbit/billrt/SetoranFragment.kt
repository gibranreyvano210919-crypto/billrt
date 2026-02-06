package com.linkbit.billrt

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.linkbit.billrt.adapter.SetoranAdapter
import com.linkbit.billrt.adapter.TimelineItem
import com.linkbit.billrt.databinding.DialogTambahSetoranBinding
import com.linkbit.billrt.databinding.FragmentSetoranBinding
import com.linkbit.billrt.model.EditCatatanSetoranRequest
import com.linkbit.billrt.model.HapusSetoranRequest
import com.linkbit.billrt.model.MasterTeknisi
import com.linkbit.billrt.model.MasterTeknisiResponse
import com.linkbit.billrt.model.RiwayatSetoranResponse
import com.linkbit.billrt.model.SetoranItem
import com.linkbit.billrt.model.StandardResponse
import com.linkbit.billrt.model.TambahSetoranRequest
import com.linkbit.billrt.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class SetoranFragment : BaseFragment(), SetoranAdapter.OnAdapterListener {

    private var _binding: FragmentSetoranBinding? = null
    private val binding get() = _binding!!

    private lateinit var setoranAdapter: SetoranAdapter
    private var teknisiList: List<MasterTeknisi> = emptyList()
    private val calendar = Calendar.getInstance()

    private var selectedImageUri: Uri? = null
    private var dialogBinding: DialogTambahSetoranBinding? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                selectedImageUri = it
                dialogBinding?.ivPreviewBukti?.setImageURI(it)
                dialogBinding?.ivPreviewBukti?.visibility = View.VISIBLE
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            selectedImageUri?.let {
                dialogBinding?.ivPreviewBukti?.setImageURI(it)
                dialogBinding?.ivPreviewBukti?.visibility = View.VISIBLE
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Izin kamera dibutuhkan untuk fitur ini", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSetoranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCloudinary()
        setupRecyclerView()
        setupFilters()
        fetchTeknisi()
        fetchRiwayatSetoran()

        binding.fabTambahSetoran.setOnClickListener { showTambahSetoranDialog() }
    }

    private fun initCloudinary() {
        if (MediaManager.get() == null) {
            try {
                MediaManager.init(requireContext())
            } catch (e: IllegalStateException) {
                // Avoid crashing if already initialized on a background thread.
            }
        }
    }

    private fun setupRecyclerView() {
        setoranAdapter = SetoranAdapter(emptyList(), this)
        binding.rvSetoran.layoutManager = LinearLayoutManager(context)
        binding.rvSetoran.adapter = setoranAdapter
    }

    private fun setupFilters() {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        val bulanArray = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val bulanAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bulanArray)
        binding.spinnerBulan.adapter = bulanAdapter
        binding.spinnerBulan.setSelection(currentMonth)

        val tahunArray = (currentYear - 2..currentYear).map { it.toString() }
        val tahunAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tahunArray)
        binding.spinnerTahun.adapter = tahunAdapter
        binding.spinnerTahun.setSelection(tahunArray.indexOf(currentYear.toString()))

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fetchRiwayatSetoran()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerBulan.onItemSelectedListener = listener
        binding.spinnerTahun.onItemSelectedListener = listener
        binding.spinnerTeknisi.onItemSelectedListener = listener
    }

    private fun fetchTeknisi() {
        RetrofitClient.instance.getMasterTeknisi().enqueue(object : Callback<MasterTeknisiResponse> {
            override fun onResponse(call: Call<MasterTeknisiResponse>, response: Response<MasterTeknisiResponse>) {
                if (isAdded && response.isSuccessful) {
                    teknisiList = listOf(MasterTeknisi(0, "Semua Teknisi", "")) + (response.body()?.data ?: emptyList())
                    val teknisiNames = teknisiList.map { it.namaTeknisi }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, teknisiNames)
                    binding.spinnerTeknisi.adapter = adapter
                }
            }
            override fun onFailure(call: Call<MasterTeknisiResponse>, t: Throwable) { }
        })
    }

    private fun fetchRiwayatSetoran() {
        val bulan = binding.spinnerBulan.selectedItemPosition + 1
        val tahun = binding.spinnerTahun.selectedItem.toString().toInt()
        val selectedTeknisi = teknisiList.getOrNull(binding.spinnerTeknisi.selectedItemPosition)
        val idTeknisi = if (selectedTeknisi?.id == 0) null else selectedTeknisi?.id.toString()

        RetrofitClient.instance.getRiwayatSetoran(bulan, tahun, idTeknisi).enqueue(object : Callback<RiwayatSetoranResponse> {
            override fun onResponse(call: Call<RiwayatSetoranResponse>, response: Response<RiwayatSetoranResponse>) {
                if (isAdded && response.isSuccessful) {
                    val setoranList = response.body()?.data ?: emptyList()
                    val timelineItems = mutableListOf<TimelineItem>()
                    val groupedByDate = setoranList.groupBy { it.tglSetoran }

                    for ((date, setorans) in groupedByDate) {
                        timelineItems.add(TimelineItem.Header(date))
                        setorans.forEach { timelineItems.add(TimelineItem.Setoran(it)) }
                    }
                    setoranAdapter.updateData(timelineItems)
                }
            }
            override fun onFailure(call: Call<RiwayatSetoranResponse>, t: Throwable) { }
        })
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Buka Kamera", "Buka Galeri")
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> launchGallery()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            Toast.makeText(context, "Gagal membuat file gambar", Toast.LENGTH_SHORT).show()
            null
        }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                it
            )
            selectedImageUri = photoURI
            cameraLauncher.launch(photoURI)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun showTambahSetoranDialog() {
        this.dialogBinding = DialogTambahSetoranBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Tambah Setoran")
            .setView(dialogBinding!!.root)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnDismissListener { this.dialogBinding = null }

        val dialogTeknisiList = teknisiList.filter { it.id != 0 }
        val dialogTeknisiNames = dialogTeknisiList.map { it.namaTeknisi }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dialogTeknisiNames)
        dialogBinding!!.spinnerTeknisiDialog.adapter = spinnerAdapter

        val dialogCalendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dialogBinding!!.etTanggal.setText(sdf.format(dialogCalendar.time))
        dialogBinding!!.etTanggal.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                dialogCalendar.set(year, month, day)
                dialogBinding!!.etTanggal.setText(sdf.format(dialogCalendar.time))
            }, dialogCalendar.get(Calendar.YEAR), dialogCalendar.get(Calendar.MONTH), dialogCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        dialogBinding!!.btnPilihGambar.setOnClickListener { showImageSourceDialog() }

        val nominalEditText = dialogBinding!!.etNominal
        nominalEditText.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    nominalEditText.removeTextChangedListener(this)

                    val cleanString = s.toString().filter { it.isDigit() }
                    val parsed = if (cleanString.isEmpty()) 0.0 else cleanString.toDouble()
                    
                    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                    formatter.maximumFractionDigits = 0

                    val formatted = formatter.format(parsed)

                    current = formatted
                    nominalEditText.setText(formatted)
                    nominalEditText.setSelection(formatted.length)

                    nominalEditText.addTextChangedListener(this)
                }
            }
        })

        dialog.setOnShowListener { 
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener { 
                val selectedTeknisi = dialogTeknisiList.getOrNull(dialogBinding!!.spinnerTeknisiDialog.selectedItemPosition)
                val idTeknisi = selectedTeknisi?.id.toString()
                val nominal = dialogBinding!!.etNominal.text.toString().filter { it.isDigit() }.toFloatOrNull() ?: 0f
                val tanggal = dialogBinding!!.etTanggal.text.toString()
                val catatan = dialogBinding!!.etCatatan.text.toString()

                if (selectedImageUri != null) {
                    uploadToCloudinary(idTeknisi, nominal, tanggal, catatan)
                } else {
                    tambahSetoran(idTeknisi, nominal, tanggal, catatan, "")
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun uploadToCloudinary(idTeknisi: String, nominal: Float, tanggal: String, catatan: String) {
        selectedImageUri?.let {
            MediaManager.get().upload(it)
                .unsigned("YOUR_UNSIGNED_UPLOAD_PRESET") // TODO: Ganti dengan upload preset Anda
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) { }
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) { }
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val url = resultData["url"] as? String ?: ""
                        tambahSetoran(idTeknisi, nominal, tanggal, catatan, url)
                    }
                    override fun onError(requestId: String, error: ErrorInfo) {
                        Toast.makeText(requireContext(), "Upload gagal: ${error.description}", Toast.LENGTH_SHORT).show()
                    }
                    override fun onReschedule(requestId: String, error: ErrorInfo) { }
                })
                .dispatch()
        }
    }

    private fun tambahSetoran(idTeknisi: String, nominal: Float, tanggal: String, catatan: String, imageUrl: String) {
        val request = TambahSetoranRequest(idTeknisi, nominal, imageUrl, tanggal, catatan)
        RetrofitClient.instance.tambahSetoran(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, response.body()?.message, Toast.LENGTH_SHORT).show()
                    fetchRiwayatSetoran()
                } else {
                    Toast.makeText(context, "Gagal menambah setoran", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onEdit(setoran: SetoranItem) {
        val editText = EditText(requireContext()).apply {
            setText(setoran.catatan.replace("\\n", "\n"))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
            gravity = android.view.Gravity.TOP
        }

        val spacing = (19 * resources.displayMetrics.density).toInt()

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Catatan")
            .setView(editText, spacing, spacing, spacing, spacing)
            .setPositiveButton("Simpan") { _, _ ->
                val newCatatan = editText.text.toString()
                val request = EditCatatanSetoranRequest(setoran.idSetoran, newCatatan)
                RetrofitClient.instance.editCatatanSetoran(request).enqueue(object : Callback<StandardResponse> {
                    override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, response.body()?.message, Toast.LENGTH_SHORT).show()
                            fetchRiwayatSetoran()
                        } else {
                            Toast.makeText(context, "Gagal mengupdate catatan", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                        Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDelete(setoran: SetoranItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Setoran")
            .setMessage("Apakah Anda yakin ingin menghapus setoran ini?")
            .setPositiveButton("Hapus") { _, _ ->
                val request = HapusSetoranRequest(setoran.idSetoran)
                RetrofitClient.instance.hapusSetoran(request).enqueue(object : Callback<StandardResponse> {
                    override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, response.body()?.message, Toast.LENGTH_SHORT).show()
                            fetchRiwayatSetoran()
                        } else {
                            Toast.makeText(context, "Gagal menghapus setoran", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                        Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    @SuppressLint("MissingPermission")
    override fun onPrint(setoran: SetoranItem) {
        val sharedPref = activity?.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE) ?: return
        val printerAddress = sharedPref.getString("SELECTED_PRINTER_ADDRESS", null)
        val paperSize = sharedPref.getInt("PAPER_SIZE", 80)

        if (printerAddress.isNullOrEmpty()) {
            Toast.makeText(context, "Printer belum diatur. Buka Pengaturan Printer...", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_setoranFragment_to_printerSettingFragment)
            return
        }

        val separator = "=".repeat(if (paperSize == 58) 32 else 48)
        val printData = """
            |DETAIL SETORAN
            |$separator
            |Nama: ${setoran.namaTeknisi ?: setoran.idTeknisi}
            |Tanggal: ${setoran.tglSetoran}
            |Nominal: ${NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(setoran.nominalSetor)}
            |$separator
            |Catatan:
            |${setoran.catatan.replace("\\n", "\n")}
            |$separator
            |
            |
            |
            """.trimMargin()

        printDataToBluetooth(printData, printerAddress)
    }

    @SuppressLint("MissingPermission")
    private fun printDataToBluetooth(data: String, address: String) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return
        val printerDevice = bluetoothAdapter.getRemoteDevice(address)
        val sppUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        Thread {
            var socket: BluetoothSocket? = null
            try {
                activity?.runOnUiThread { Toast.makeText(context, "Mencetak...", Toast.LENGTH_SHORT).show() }
                socket = printerDevice.createRfcommSocketToServiceRecord(sppUuid)
                socket.connect()
                val outputStream = socket.outputStream
                outputStream.write(data.toByteArray())
                outputStream.flush()
                Thread.sleep(1000) 
                activity?.runOnUiThread { Toast.makeText(context, "Berhasil dikirim ke printer", Toast.LENGTH_SHORT).show() }
            } catch (e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread { 
                    Toast.makeText(context, "Gagal mencetak: ${e.message}. Buka Pengaturan Printer...", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_setoranFragment_to_printerSettingFragment)
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
    }
}