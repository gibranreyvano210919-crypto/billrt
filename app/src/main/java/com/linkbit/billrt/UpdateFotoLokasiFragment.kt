package com.linkbit.billrt

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.linkbit.billrt.databinding.FragmentUpdateFotoLokasiBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UpdateFotoLokasiFragment : Fragment() {

    private var _binding: FragmentUpdateFotoLokasiBinding? = null
    private val binding get() = _binding!!
    private val args: UpdateFotoLokasiFragmentArgs by navArgs()

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var currentAddress: String? = null

    private val timeHandler = Handler(Looper.getMainLooper())
    private lateinit var timeRunnable: Runnable

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissions ->
            var allPermissionsGranted = true
            permissions.entries.forEach { if (!it.value) allPermissionsGranted = false }
            if (allPermissionsGranted) {
                startCameraAndLocation()
            } else {
                Toast.makeText(context, "Izin kamera dan lokasi dibutuhkan", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUpdateFotoLokasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.namaPelangganTextView.text = args.namaPelanggan
        binding.pelangganIdTextView.text = "ID: ${args.idPelanggan}"
        cameraExecutor = Executors.newSingleThreadExecutor()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        checkPermissionsAndStart()
        startDatetimeUpdater()
        binding.captureButton.setOnClickListener { takePhotoAndUpload() }
    }

    private fun checkPermissionsAndStart() {
        if (allPermissionsGranted()) startCameraAndLocation() else activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCameraAndLocation() {
        startCamera()
        getCurrentLocation()
    }

    private fun startDatetimeUpdater() {
        timeRunnable = Runnable {
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            binding.datetimeTextView.text = sdf.format(Date())
            timeHandler.postDelayed(timeRunnable, 1000)
        }
        timeHandler.post(timeRunnable)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.cameraPreview.surfaceProvider) }
            imageCapture = ImageCapture.Builder().setJpegQuality(90).build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (e: Exception) {
                Log.e(TAG, "Gagal memulai kamera", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLatitude = it.latitude
                    currentLongitude = it.longitude
                    binding.locationTextView.text = "Lat: %.6f, Lon: %.6f".format(it.latitude, it.longitude)
                    getAddressFromLocation(it.latitude, it.longitude)
                }
            }
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            currentAddress = if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].getAddressLine(0) ?: "Alamat tidak ditemukan"
            } else {
                "Alamat tidak ditemukan"
            }
            binding.addressTextView.text = "Alamat: $currentAddress"
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mendapatkan alamat", e)
            binding.addressTextView.text = "Gagal mendapatkan alamat"
        }
    }

    private fun takePhotoAndUpload() {
        val imageCapture = imageCapture ?: return
        if (currentLatitude == null || currentLongitude == null || currentAddress == null) {
            Toast.makeText(context, "Lokasi GPS atau alamat belum didapatkan, harap tunggu.", Toast.LENGTH_SHORT).show()
            return
        }
        binding.uploadProgressBar.isVisible = true
        binding.captureButton.isEnabled = false

        imageCapture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                val sourceBitmap = imageProxyToBitmap(imageProxy)
                imageProxy.close()

                val watermarkedBitmap = addWatermarkToBitmap(sourceBitmap)
                val imageUri = saveBitmapToCache(watermarkedBitmap)

                activity?.runOnUiThread {
                    if (imageUri != null) {
                        uploadToCloudinary(imageUri)
                    } else {
                        Toast.makeText(context, "Gagal memproses gambar.", Toast.LENGTH_SHORT).show()
                        binding.uploadProgressBar.isVisible = false
                        binding.captureButton.isEnabled = true
                    }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Gagal mengambil gambar: ", exception)
                activity?.runOnUiThread {
                    Toast.makeText(context, "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
                    binding.uploadProgressBar.isVisible = false
                    binding.captureButton.isEnabled = true
                }
            }
        })
    }

    private fun uploadToCloudinary(imageUri: Uri) {
        Toast.makeText(context, "Gambar berhasil diproses. Mengunggah...", Toast.LENGTH_SHORT).show()

        // Membuat nama file kustom
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${args.idPelanggan}_${timeStamp}"

        MediaManager.get().upload(imageUri)
            .option("public_id", fileName)
            .callback(object : UploadCallback {
            override fun onSuccess(requestId: String, resultData: MutableMap<Any?, Any?>) {
                val imageUrl = resultData["secure_url"] as String
                deleteCacheFile(imageUri)
                
                val action = UpdateFotoLokasiFragmentDirections.actionUpdateFotoLokasiFragmentToKonfirmasiUpdateFragment(
                    idPelanggan = args.idPelanggan,
                    namaPelanggan = args.namaPelanggan,
                    imageUrl = imageUrl,
                    latitude = currentLatitude?.toFloat() ?: 0f,
                    longitude = currentLongitude?.toFloat() ?: 0f,
                    alamat = currentAddress ?: ""
                )
                findNavController().navigate(action)
                binding.uploadProgressBar.isVisible = false
                binding.captureButton.isEnabled = true
            }

            override fun onError(requestId: String, error: ErrorInfo) {
                deleteCacheFile(imageUri)
                Log.e(TAG, "Gagal upload ke Cloudinary: ${error.description}")
                Toast.makeText(context, "Gagal upload: ${error.description}", Toast.LENGTH_LONG).show()
                binding.uploadProgressBar.isVisible = false
                binding.captureButton.isEnabled = true
            }
            override fun onStart(requestId: String) {}
            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
            override fun onReschedule(requestId: String, error: ErrorInfo) {}
        }).dispatch()
    }

    private fun deleteCacheFile(uri: Uri) {
        try {
            uri.path?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            } 
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting cache file", e)
        }
    }
    
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun addWatermarkToBitmap(source: Bitmap): Bitmap {
        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 40f // Ukuran teks watermark
            setShadowLayer(5f, 2f, 2f, Color.BLACK)
        }

        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val textLines = listOf(
            args.namaPelanggan,
            "ID: ${args.idPelanggan}",
            sdf.format(Date()),
            "Lat: ${currentLatitude?.format(6)}, Lon: ${currentLongitude?.format(6)}",
            currentAddress ?: ""
        )

        var yPos = 60f
        for (line in textLines) {
            canvas.drawText(line, 40f, yPos, paint)
            yPos += paint.descent() - paint.ascent() + 10
        }

        return result
    }
    
    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "watermarked_image_${System.currentTimeMillis()}.jpg")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.close()
            file.toUri()
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menyimpan bitmap ke cache", e)
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timeHandler.removeCallbacks(timeRunnable)
        cameraExecutor.shutdown()
        _binding = null
    }

    companion object {
        private const val TAG = "UpdateFotoLokasiFragment"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
    }
}
