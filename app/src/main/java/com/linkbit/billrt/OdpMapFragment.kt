package com.linkbit.billrt

import android.R
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.linkbit.billrt.databinding.FragmentOdpMapBinding
import com.linkbit.billrt.model.StandardResponse
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import kotlin.math.roundToInt

class OdpMapFragment : BaseFragment() {

    private var _binding: FragmentOdpMapBinding? = null
    private val binding get() = _binding!!
    private var mapView: MapView? = null

    // Annotation Overlays
    private var odpMarkers = mutableListOf<Marker>()
    private var customerMarkers = mutableListOf<Marker>()
    private var polylineOverlays = mutableListOf<Polyline>()
    private var editPointMarkers = mutableListOf<Marker>()

    // Edit Mode State
    private var isEditMode = false
    private var currentEditingPort: OdpPortWithCabling? = null
    private var currentOdp: OdpDetailData? = null
    private var newCablePathPoints = mutableListOf<GeoPoint>()
    private var tempPolylineOverlay: Polyline? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOdpMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapViewOdp
        mapView?.setTileSource(TileSourceFactory.MAPNIK)
        mapView?.setMultiTouchControls(true)

        val appBarLayout = binding.toolbarOdpMap.parent as? View
        appBarLayout?.let { applyWindowInsets(it) }

        setupToolbar()

        binding.fabSaveCablePath.setOnClickListener { showSaveConfirmationDialog() }
        binding.fabCancelEdit.setOnClickListener { cancelEditMode() }

        fetchOdpData()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarOdpMap)
        binding.toolbarOdpMap.setupWithNavController(findNavController())
        binding.toolbarOdpMap.title = "Peta ODP & Jalur Kabel"
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
        val currentMapView = mapView ?: return
        currentMapView.overlays.clear()
        odpMarkers.clear()

        val odpIcon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_dialog_map)

        odpList.forEach { odp ->
            val lat = odp.latitude
            val lng = odp.longitude
            if (lat != null && lng != null) {
                val point = GeoPoint(lat, lng)
                val marker = Marker(currentMapView).apply {
                    position = point
                    title = odp.namaOdp
                    icon = odpIcon
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    relatedObject = odp
                    setOnMarkerClickListener { m, _ ->
                        if (!isEditMode) {
                            val odpData = m.relatedObject as? OdpData
                            if (odpData != null) {
                                showOdpPortDetails(odpData)
                            }
                        }
                        true
                    }
                }
                odpMarkers.add(marker)
                currentMapView.overlays.add(marker)
            }
        }

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                handleMapClick(p)
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }
        currentMapView.overlays.add(MapEventsOverlay(mapEventsReceiver))

        if (odpList.isNotEmpty()) {
            odpList.firstOrNull { it.latitude != null && it.longitude != null }?.let {
                currentMapView.controller.setZoom(16.0)
                currentMapView.controller.setCenter(GeoPoint(it.latitude!!, it.longitude!!))
            }
        }

        currentMapView.invalidate()
    }

    private fun calculatePathLength(points: List<GeoPoint>): Int {
        if (points.size < 2) return 0
        var totalDistance = 0.0
        for (i in 0 until points.size - 1) {
            totalDistance += points[i].distanceToAsDouble(points[i + 1])
        }
        return totalDistance.roundToInt()
    }

    private fun clearCustomerAndCables() {
        val currentMapView = mapView ?: return
        customerMarkers.forEach { currentMapView.overlays.remove(it) }
        polylineOverlays.forEach { currentMapView.overlays.remove(it) }
        customerMarkers.clear()
        polylineOverlays.clear()
        currentMapView.invalidate()
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

                    val currentMapView = mapView ?: return
                    val customerIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_menu_myplaces)
                    val gson = Gson()
                    val pathType = object : TypeToken<List<List<Double>>>() {}.type

                    odpDetail.listPorts.forEach { port ->
                        val polylinePoints = mutableListOf<GeoPoint>()
                        odpDetail.odpLng?.let { lng -> odpDetail.odpLat?.let { lat -> polylinePoints.add(GeoPoint(lat, lng)) } }

                        if (!port.jalurKabel.isNullOrBlank()) {
                            try {
                                val path: List<List<Double>> = gson.fromJson(port.jalurKabel, pathType)
                                path.forEach { if (it.size >= 2) polylinePoints.add(GeoPoint(it[1], it[0])) }
                            } catch (e: Exception) {
                                Log.e("OdpMapFragment", "Gagal parsing jalur_kabel JSON: ${port.jalurKabel}", e)
                            }
                        }

                        port.custLat?.let { custLat ->
                            port.custLng?.let { custLng ->
                                val customerPoint = GeoPoint(custLat, custLng)
                                polylinePoints.add(customerPoint)

                                val customerMarker = Marker(currentMapView).apply {
                                    position = customerPoint
                                    title = port.namaPelanggan ?: "Pelanggan"
                                    icon = customerIcon
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    relatedObject = port
                                    setOnMarkerClickListener { m, _ ->
                                        val portData = m.relatedObject as? OdpPortWithCabling
                                        if (portData != null) {
                                            handleCustomerClick(portData)
                                        }
                                        true
                                    }
                                }
                                customerMarkers.add(customerMarker)
                                currentMapView.overlays.add(customerMarker)
                            }
                        }

                        if (polylinePoints.size > 1) {
                            val polyline = Polyline(currentMapView).apply {
                                setPoints(polylinePoints)
                                outlinePaint.color = Color.parseColor("#3bb2d0")
                                outlinePaint.strokeWidth = 5f
                                relatedObject = port
                            }
                            polylineOverlays.add(polyline)
                            currentMapView.overlays.add(polyline)
                        }
                    }

                    currentMapView.invalidate()

                    if (customerMarkers.isEmpty() && isAdded) {
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

    private fun handleCustomerClick(portData: OdpPortWithCabling) {
        val existingPolyline = polylineOverlays.find { 
            if (it.actualPoints.isEmpty()) return@find false
            val lastPoint = it.actualPoints.last()
            lastPoint.latitude == portData.custLat && lastPoint.longitude == portData.custLng
        }

        val cableLength = existingPolyline?.let { calculatePathLength(it.actualPoints) } ?: 0
        val lengthText = if (cableLength > 0) "\nPanjang Kabel: $cableLength m" else ""

        val options = arrayOf("Edit Jalur Kabel", "Hapus Jalur Kabel", "Cabut Layanan Port")

        AlertDialog.Builder(requireContext())
            .setTitle("Opsi Port: ${portData.portNumber}\nPelanggan: ${portData.namaPelanggan ?: "-"}$lengthText")
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

    private fun handleMapClick(point: GeoPoint) {
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

        val existingPolyline = polylineOverlays.find { 
            if (it.actualPoints.isEmpty()) return@find false
            val lastPoint = it.actualPoints.last()
            lastPoint.latitude == port.custLat && lastPoint.longitude == port.custLng
        }
        
        newCablePathPoints = existingPolyline?.actualPoints?.toMutableList() ?: mutableListOf()

        if (newCablePathPoints.isEmpty()) {
             currentOdp?.odpLng?.let { lng -> currentOdp?.odpLat?.let { lat -> newCablePathPoints.add(GeoPoint(lat, lng)) } }
             port.custLng?.let { lng -> port.custLat?.let { lat -> newCablePathPoints.add(GeoPoint(lat, lng)) } }
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
        val currentMapView = mapView ?: return
        tempPolylineOverlay?.let { currentMapView.overlays.remove(it) }
        if (newCablePathPoints.size > 1) {
            tempPolylineOverlay = Polyline(currentMapView).apply {
                setPoints(newCablePathPoints)
                outlinePaint.color = Color.YELLOW
                outlinePaint.strokeWidth = 6f
            }
            currentMapView.overlays.add(tempPolylineOverlay)
        }
        val length = calculatePathLength(newCablePathPoints)
        binding.tvCableLength.text = "Panjang Kabel: $length m"
        currentMapView.invalidate()
    }
    
    private fun drawEditPoints() {
        val currentMapView = mapView ?: return
        editPointMarkers.forEach { currentMapView.overlays.remove(it) }
        editPointMarkers.clear()
        
        newCablePathPoints.forEachIndexed { index, point ->
            if (index > 0 && index < newCablePathPoints.size - 1) {
                val marker = Marker(currentMapView).apply {
                    position = point
                    title = "Titik $index"
                    isDraggable = true
                    relatedObject = index
                    setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                        override fun onMarkerDragStart(m: Marker?) {}
                        override fun onMarkerDrag(m: Marker?) {
                            val idx = m?.relatedObject as? Int ?: return
                            val newPos = m.position
                            if (newPos != null && idx >= 0 && idx < newCablePathPoints.size) {
                                newCablePathPoints[idx] = newPos
                                drawTemporaryPolyline()
                            }
                        }
                        override fun onMarkerDragEnd(m: Marker?) {}
                    })
                }
                editPointMarkers.add(marker)
                currentMapView.overlays.add(marker)
            }
        }
        currentMapView.invalidate()
    }

    private fun clearEditModeVisuals() {
        val currentMapView = mapView ?: return
        editPointMarkers.forEach { currentMapView.overlays.remove(it) }
        editPointMarkers.clear()
        tempPolylineOverlay?.let { currentMapView.overlays.remove(it) }
        tempPolylineOverlay = null
        currentMapView.invalidate()
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
        val pathForJson = intermediatePoints.map { listOf(it.longitude, it.latitude) }
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
                showOdpPortDetails(OdpData(currentOdp!!.id, "", "", null, null, null, null, null))
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

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
