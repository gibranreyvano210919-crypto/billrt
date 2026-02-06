package com.linkbit.billrt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.linkbit.billrt.databinding.FragmentOdpMapBinding
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.Annotation
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import kotlin.math.roundToInt

class OdpMapFragment : BaseFragment() {

    private var _binding: FragmentOdpMapBinding? = null
    private val binding get() = _binding!!
    private var mapView: MapView? = null

    // Annotation Managers
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var polylineAnnotationManager: PolylineAnnotationManager? = null
    private var circleAnnotationManager: CircleAnnotationManager? = null // For edit points

    // Annotation Lists
    private var customerAnnotations = mutableListOf<PointAnnotation>()
    private var polylineAnnotations = mutableListOf<PolylineAnnotation>()
    private var editPointAnnotations = mutableListOf<CircleAnnotation>() // For edit points

    // Edit Mode State
    private var isEditMode = false
    private var currentEditingPort: OdpPortWithCabling? = null
    private var currentOdp: OdpDetailData? = null
    private var newCablePathPoints = mutableListOf<Point>()
    private var tempPolylineAnnotation: PolylineAnnotation? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOdpMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapViewOdp

        binding.fabSaveCablePath.setOnClickListener { showSaveConfirmationDialog() }
        binding.fabCancelEdit.setOnClickListener { cancelEditMode() }

        fetchOdpData()
    }

    private fun fetchOdpData() {
        binding.progressBarOdp.visibility = View.VISIBLE
        apiService.getOdp().enqueue(object : Callback<OdpResponse> {
            override fun onResponse(call: Call<OdpResponse>, response: Response<OdpResponse>) {
                if (!isAdded || _binding == null) return
                binding.progressBarOdp.visibility = View.GONE
                if (response.isSuccessful && response.body()?.status == true) {
                    setupMap(response.body()!!.data)
                } else {
                    Toast.makeText(context, "Gagal memuat data ODP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OdpResponse>, t: Throwable) {
                if (isAdded) {
                    binding.progressBarOdp.visibility = View.GONE
                    val message = if (t is SocketTimeoutException) "Koneksi timeout, silakan coba lagi." else "Error: ${t.message}"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupMap(odpList: List<OdpData>) {
        mapView?.mapboxMap?.loadStyle(Style.SATELLITE_STREETS) { style ->
            bitmapFromVector(requireContext(), android.R.drawable.ic_dialog_map)?.let { style.addImage("odp_icon", it) }
            bitmapFromVector(requireContext(), android.R.drawable.ic_menu_myplaces)?.let { style.addImage("customer_icon", it) }

            val annotationApi = mapView?.annotations
            pointAnnotationManager = annotationApi?.createPointAnnotationManager()
            polylineAnnotationManager = annotationApi?.createPolylineAnnotationManager()
            circleAnnotationManager = annotationApi?.createCircleAnnotationManager()

            val odpOptionsList = odpList.mapNotNull { odp ->
                odp.latitude?.let { lat ->
                    odp.longitude?.let {
                        PointAnnotationOptions()
                            .withPoint(Point.fromLngLat(it, lat))
                            .withIconImage("odp_icon")
                            .withTextField(odp.namaOdp)
                            .withTextColor(Color.WHITE)
                            .withData(Gson().toJsonTree(odp))
                    }
                }
            }
            pointAnnotationManager?.create(odpOptionsList)

            pointAnnotationManager?.addClickListener(OnPointAnnotationClickListener { annotation ->
                if (isEditMode) return@OnPointAnnotationClickListener true
                val jsonElement = annotation.getData() ?: return@OnPointAnnotationClickListener false
                val jsonObject = jsonElement.asJsonObject

                if (jsonObject.has("nama_odp")) { 
                    val odpData = Gson().fromJson(jsonObject, OdpData::class.java)
                    showOdpPortDetails(odpData)
                } else if (jsonObject.has("port_number")) { 
                    handleCustomerClick(annotation)
                }
                true
            })
            
            circleAnnotationManager?.let { manager ->
                manager.addDragListener(object : OnCircleAnnotationDragListener {
                    override fun onAnnotationDragStarted(annotation: Annotation<*>) {
                        Log.d("Mapbox", "Mulai menggeser titik jalur")
                    }

                    override fun onAnnotationDrag(annotation: Annotation<*>) {
                        if (annotation is CircleAnnotation) {
                            val indexElement = annotation.getData()?.asJsonObject?.get("edit_point_index")
                            if (indexElement != null) {
                                val index = indexElement.asInt
                                
                                if (index >= 0 && index < newCablePathPoints.size) {
                                    newCablePathPoints[index] = annotation.point
                                    drawTemporaryPolyline()
                                }
                            }
                        }
                    }

                    override fun onAnnotationDragFinished(annotation: Annotation<*>) {
                        Log.d("Mapbox", "Selesai menggeser titik")
                    }
                })
            }

            mapView?.gestures?.addOnMapClickListener(OnMapClickListener { point ->
                handleMapClick(point)
                true
            })

            odpList.firstOrNull { it.latitude != null && it.longitude != null }?.let {
                mapView?.mapboxMap?.setCamera(com.mapbox.maps.CameraOptions.Builder().center(Point.fromLngLat(it.longitude!!, it.latitude!!)).zoom(16.0).build())
            }
        }
    }

    private fun calculatePathLength(points: List<Point>): Int {
        if (points.size < 2) return 0
        var totalDistance = 0f
        for (i in 0 until points.size - 1) {
            val start = Location("")
            start.latitude = points[i].latitude()
            start.longitude = points[i].longitude()

            val end = Location("")
            end.latitude = points[i + 1].latitude()
            end.longitude = points[i + 1].longitude()

            totalDistance += start.distanceTo(end)
        }
        return totalDistance.roundToInt()
    }

    private fun clearCustomerAndCables() {
        pointAnnotationManager?.delete(customerAnnotations)
        polylineAnnotationManager?.delete(polylineAnnotations)
        customerAnnotations.clear()
        polylineAnnotations.clear()
    }

    private fun showOdpPortDetails(odpData: OdpData) {
        binding.progressBarOdp.visibility = View.VISIBLE
        apiService.getOdpDetail(odpData.id).enqueue(object : Callback<OdpDetailResponse> {
            override fun onResponse(call: Call<OdpDetailResponse>, response: Response<OdpDetailResponse>) {
                if (!isAdded) return
                binding.progressBarOdp.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == true) {
                    currentOdp = response.body()?.data
                    val odpDetail = currentOdp ?: return

                    clearCustomerAndCables()

                    val customerOpts = mutableListOf<PointAnnotationOptions>()
                    val polylineOpts = mutableListOf<PolylineAnnotationOptions>()
                    val gson = Gson()
                    val pathType = object : TypeToken<List<List<Double>>>() {}.type

                    odpDetail.listPorts.forEach { port ->
                        val polylinePoints = mutableListOf<Point>()
                        odpDetail.odpLng?.let { lng -> odpDetail.odpLat?.let { lat -> polylinePoints.add(Point.fromLngLat(lng, lat)) } }

                        if (!port.jalurKabel.isNullOrBlank()) {
                            try {
                                val path: List<List<Double>> = gson.fromJson(port.jalurKabel, pathType)
                                path.forEach { if (it.size >= 2) polylinePoints.add(Point.fromLngLat(it[0], it[1])) }
                            } catch (e: Exception) {
                                Log.e("OdpMapFragment", "Gagal parsing jalur_kabel JSON: ${port.jalurKabel}", e)
                            }
                        }

                        port.custLat?.let { custLat ->
                            port.custLng?.let { custLng ->
                                val customerPoint = Point.fromLngLat(custLng, custLat)
                                polylinePoints.add(customerPoint)

                                customerOpts.add(
                                    PointAnnotationOptions()
                                        .withPoint(customerPoint)
                                        .withIconImage("customer_icon")
                                        .withTextField(port.namaPelanggan ?: "Pelanggan")
                                        .withTextColor(Color.YELLOW)
                                        .withData(gson.toJsonTree(port))
                                )
                            }
                        }

                        if (polylinePoints.size > 1) {
                            polylineOpts.add(
                                PolylineAnnotationOptions()
                                    .withPoints(polylinePoints)
                                    .withLineColor(Color.parseColor("#3bb2d0"))
                                    .withLineWidth(2.5)
                            )
                        }
                    }

                    customerAnnotations = pointAnnotationManager?.create(customerOpts)?.toMutableList() ?: mutableListOf()
                    polylineAnnotations = polylineAnnotationManager?.create(polylineOpts)?.toMutableList() ?: mutableListOf()

                    if (customerAnnotations.isEmpty() && isAdded) {
                        Toast.makeText(context, "ODP ini tidak memiliki port terhubung.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (isAdded) Toast.makeText(context, response.body()?.message ?: "Gagal memuat detail ODP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OdpDetailResponse>, t: Throwable) {
                if (isAdded) {
                    binding.progressBarOdp.visibility = View.GONE
                    val message = if (t is SocketTimeoutException) "Koneksi timeout, silakan coba lagi." else "Error: ${t.message}"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun handleCustomerClick(annotation: PointAnnotation) {
        val portJson = annotation.getData() ?: return
        val portData = Gson().fromJson(portJson, OdpPortWithCabling::class.java)
        
        val existingPolyline = polylineAnnotations.find { 
            if(it.points.isEmpty()) return@find false
            val lastPoint = it.points.last()
            lastPoint.latitude() == portData.custLat && lastPoint.longitude() == portData.custLng
        }

        val cableLength = existingPolyline?.let { calculatePathLength(it.points) } ?: 0
        val lengthText = if (cableLength > 0) "\nPanjang Kabel: $cableLength m" else ""

        val message = "Pelanggan: ${portData.namaPelanggan}\nPort: ${portData.portNumber}$lengthText"
        
        val options = arrayOf("Edit Jalur Kabel", "Hapus Jalur Kabel", "Cabut Layanan Port")

        AlertDialog.Builder(requireContext())
            .setTitle("Opsi Port: ${portData.portNumber}")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> startEditMode(portData)
                    1 -> showDeleteCablePathConfirmation(portData)
                    2 -> showCabutLayananConfirmation(portData)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun handleMapClick(point: Point) {
        if (isEditMode && newCablePathPoints.isNotEmpty()) {
            val newPointIndex = newCablePathPoints.size - 1
            newCablePathPoints.add(newPointIndex, point)
            drawEditModeVisuals()
        }
    }

    private fun startEditMode(port: OdpPortWithCabling) {
        if (currentOdp == null) {
            Toast.makeText(context, "Silakan pilih ODP terlebih dahulu.", Toast.LENGTH_SHORT).show()
            return
        }
        isEditMode = true
        currentEditingPort = port
        newCablePathPoints.clear()

        // Set initial path from existing polyline
        val existingPolyline = polylineAnnotations.find { 
            if(it.points.isEmpty()) return@find false
            val lastPoint = it.points.last()
            lastPoint.latitude() == port.custLat && lastPoint.longitude() == port.custLng
        }
        
        newCablePathPoints = existingPolyline?.points?.toMutableList() ?: mutableListOf()

        if(newCablePathPoints.isEmpty()){
             currentOdp?.odpLng?.let { lng -> currentOdp?.odpLat?.let { lat -> newCablePathPoints.add(Point.fromLngLat(lng, lat)) } }
             port.custLng?.let { lng -> port.custLat?.let { lat -> newCablePathPoints.add(Point.fromLngLat(lng, lat)) } }
        }

        binding.fabSaveCablePath.visibility = View.VISIBLE
        binding.fabCancelEdit.visibility = View.VISIBLE
        binding.tvCableLength.visibility = View.VISIBLE
        
        drawEditModeVisuals()
        Toast.makeText(context, "Mode Edit: Tap peta untuk menambah titik, geser titik untuk mengubah jalur.", Toast.LENGTH_LONG).show()
    }
    
    private fun drawEditModeVisuals() {
        clearEditModeVisuals()
        drawTemporaryPolyline()
        drawEditPoints()
    }

    private fun drawTemporaryPolyline() {
        tempPolylineAnnotation?.let { polylineAnnotationManager?.delete(it) }
        if (newCablePathPoints.size > 1) {
            val options = PolylineAnnotationOptions()
                .withPoints(newCablePathPoints)
                .withLineColor(Color.YELLOW)
                .withLineWidth(3.0)
            tempPolylineAnnotation = polylineAnnotationManager?.create(options)
        }
        val length = calculatePathLength(newCablePathPoints)
        binding.tvCableLength.text = "Panjang Kabel: $length m"
    }
    
    private fun drawEditPoints() {
        circleAnnotationManager?.delete(editPointAnnotations)
        editPointAnnotations.clear()
        
        val editPointOpts = mutableListOf<CircleAnnotationOptions>()
        // Create draggable circles for intermediate points only
        newCablePathPoints.forEachIndexed { index, point ->
            if (index > 0 && index < newCablePathPoints.size - 1) { // Exclude ODP and Customer points
                editPointOpts.add(
                    CircleAnnotationOptions()
                        .withPoint(point)
                        .withCircleRadius(8.0)
                        .withCircleColor(Color.GREEN)
                        .withCircleStrokeWidth(2.0)
                        .withCircleStrokeColor(Color.WHITE)
                        .withDraggable(true)
                        .withData(Gson().toJsonTree(mapOf("edit_point_index" to index)))
                )
            }
        }
        editPointAnnotations = circleAnnotationManager?.create(editPointOpts)?.toMutableList() ?: mutableListOf()
    }

    private fun clearEditModeVisuals() {
        circleAnnotationManager?.delete(editPointAnnotations)
        editPointAnnotations.clear()
        tempPolylineAnnotation?.let { polylineAnnotationManager?.delete(it) }
        tempPolylineAnnotation = null
    }

    private fun showCabutLayananConfirmation(port: OdpPortWithCabling) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Cabut Layanan")
            .setMessage("Anda yakin ingin mencabut layanan dan mengosongkan Port ${port.portNumber} dari pelanggan ${port.namaPelanggan}? Port akan berstatus 'available'.")
            .setPositiveButton("Ya, Cabut") { _, _ -> cabutLayanan(port) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun cabutLayanan(port: OdpPortWithCabling) {
        if (currentOdp == null) return

        val request = CabutLayananPortRequest(odpId = currentOdp!!.id, portNumber = port.portNumber)
        binding.progressBarOdp.visibility = View.VISIBLE

        apiService.cabutLayananPort(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                binding.progressBarOdp.visibility = View.GONE
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, response.body()?.message ?: "Layanan berhasil dicabut.", Toast.LENGTH_SHORT).show()
                    // Refresh map to reflect the change
                    val originalOdp = OdpData(currentOdp!!.id, currentOdp!!.namaOdp, currentOdp!!.lokasi, currentOdp!!.odpLat, currentOdp!!.odpLng, null, null, null)
                    showOdpPortDetails(originalOdp)
                } else {
                    Toast.makeText(context, "Gagal: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                binding.progressBarOdp.visibility = View.GONE
                val message = if (t is SocketTimeoutException) "Koneksi timeout, silakan coba lagi." else "Error: ${t.message}"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteCablePathConfirmation(port: OdpPortWithCabling) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Jalur Kabel?")
            .setMessage("Anda yakin ingin menghapus jalur kabel untuk port ${port.portNumber}?")
            .setPositiveButton("Hapus") { _, _ -> deleteCablePath(port) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteCablePath(port: OdpPortWithCabling) {
        if (currentOdp == null) return
        val request = HapusJalurKabelRequest(odpId = currentOdp!!.id, portNumber = port.portNumber)
        binding.progressBarOdp.visibility = View.VISIBLE
        apiService.hapusJalurKabel(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                binding.progressBarOdp.visibility = View.GONE
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Jalur kabel berhasil dihapus.", Toast.LENGTH_SHORT).show()
                    val originalOdp = OdpData(currentOdp!!.id, currentOdp!!.namaOdp, currentOdp!!.lokasi, currentOdp!!.odpLat, currentOdp!!.odpLng, null, null, null)
                    showOdpPortDetails(originalOdp)
                } else {
                    Toast.makeText(context, "Gagal menghapus: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                binding.progressBarOdp.visibility = View.GONE
                val message = if (t is SocketTimeoutException) "Koneksi timeout, silakan coba lagi." else "Error: ${t.message}"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showSaveConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Simpan Jalur Kabel?")
            .setMessage("Simpan jalur yang telah digambar untuk port ${currentEditingPort?.portNumber}?")
            .setPositiveButton("Simpan") { _, _ -> saveNewCablePath() }
            .setNegativeButton("Lanjut Edit", null)
            .show()
    }

    private fun saveNewCablePath() {
        if (currentOdp == null || currentEditingPort == null || newCablePathPoints.size < 2) {
            Toast.makeText(context, "Data tidak lengkap untuk menyimpan.", Toast.LENGTH_SHORT).show()
            cancelEditMode()
            return
        }

        val intermediatePoints = if (newCablePathPoints.size > 2) newCablePathPoints.subList(1, newCablePathPoints.size - 1) else emptyList()
        val pathForJson = intermediatePoints.map { listOf(it.longitude(), it.latitude()) }
        val jalurKabelJson = Gson().toJson(pathForJson)

        val request = SimpanJalurKabelRequest(odpId = currentOdp!!.id, portNumber = currentEditingPort!!.portNumber, jalurKabel = jalurKabelJson)

        binding.progressBarOdp.visibility = View.VISIBLE
        apiService.simpanJalurKabel(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                binding.progressBarOdp.visibility = View.GONE
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Jalur kabel berhasil disimpan.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal menyimpan: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
                cancelEditMode() 
                showOdpPortDetails(OdpData(currentOdp!!.id, "", "", null, null, null, null, null)) // Refresh map
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                binding.progressBarOdp.visibility = View.GONE
                val message = if (t is SocketTimeoutException) "Koneksi timeout, silakan coba lagi." else "Error: ${t.message}"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                cancelEditMode()
            }
        })
    }

    private fun cancelEditMode() {
        isEditMode = false
        currentEditingPort = null
        newCablePathPoints.clear()

        binding.fabSaveCablePath.visibility = View.GONE
        binding.fabCancelEdit.visibility = View.GONE
        binding.tvCableLength.visibility = View.GONE

        clearEditModeVisuals()

        currentOdp?.let {
            showOdpPortDetails(OdpData(it.id, it.namaOdp, it.lokasi, it.odpLat, it.odpLng, null, null, null))
        }

        Toast.makeText(context, "Mode edit dibatalkan.", Toast.LENGTH_SHORT).show()
    }

    private fun bitmapFromVector(context: Context, vectorResId: Int): Bitmap? {
        return ContextCompat.getDrawable(context, vectorResId)?.let { vectorDrawable ->
            vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
            val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable.draw(canvas)
            bitmap
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
        _binding = null
    }
}