package com.linkbit.billrt

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.linkbit.billrt.databinding.DialogPelangganSearchBinding
import com.linkbit.billrt.databinding.FragmentMapPelangganBinding
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapPelangganFragment : BaseFragment() {

    private var _binding: FragmentMapPelangganBinding? = null
    private val binding get() = _binding!!
    private var mapView: MapView? = null
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var lineAnnotationManager: PolylineAnnotationManager? = null
    private var manualPointsAnnotationManager: CircleAnnotationManager? = null
    private var annotationToMove: PointAnnotation? = null

    private var allPelangganList = listOf<PelangganMapData>()

    private val searchHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    private enum class MapMode { NONE, MOVE_ANNOTATION, ADD_LOCATION, MANUAL_POLYLINE }
    private var currentMode = MapMode.NONE
    private var pelangganToUpdate: PelangganMapData? = null

    private val manualPolylinePoints = mutableListOf<Point>()
    private var manualPolylineAnnotation: PolylineAnnotation? = null

    private val mapStyles = listOf(
        "Satelit" to Style.SATELLITE_STREETS,
        "Jalan" to Style.MAPBOX_STREETS,
        "Gelap" to Style.DARK
    )
    private var currentStyleIndex = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchAndCenterOnUserLocation()
        } else {
            Toast.makeText(context, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapView
        setupBottomSheet()
        setupSearchView()
        fetchMapData()
        checkLocationPermissionAndCenter()

        binding.fabMyLocation.setOnClickListener {
            checkLocationPermissionAndCenter()
        }
    }
    
    private fun setupBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        
        binding.fabTools.setOnClickListener {
             if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        binding.buttonAddLocation.setOnClickListener { 
            showSearchPelangganDialog()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        binding.buttonMeasure.setOnClickListener { 
            toggleManualPolylineMode()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        binding.buttonClearMeasure.setOnClickListener { 
            clearManualPolyline()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        binding.buttonChangeStyle.setOnClickListener {
            showMapStyleDialog()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun showMapStyleDialog() {
        val styleNames = mapStyles.map { it.first }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Gaya Peta")
            .setItems(styleNames) { _, which ->
                currentStyleIndex = which
                setupMap(allPelangganList) // Reload map with new style
            }
            .show()
    }

    private fun setupSearchView() {
        binding.searchViewMap.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable { handleSearch(newText) }
                searchHandler.postDelayed(searchRunnable!!, 800)
                return true
            }
        })
    }

    private fun handleSearch(query: String?) {
        if (!query.isNullOrBlank()) {
            val parts = query.split(',').map { it.trim() }
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                    val point = Point.fromLngLat(lng, lat)
                    mapView?.getMapboxMap()?.setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(17.0)
                            .build()
                    )
                    manualPointsAnnotationManager?.deleteAll()
                    val circleOptions = CircleAnnotationOptions()
                        .withPoint(point)
                        .withCircleRadius(8.0)
                        .withCircleColor("#0000FF") 
                        .withCircleStrokeWidth(2.0)
                        .withCircleStrokeColor("#FFFFFF")
                    manualPointsAnnotationManager?.create(circleOptions)
                    Toast.makeText(context, "Menuju ke koordinat...", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        manualPointsAnnotationManager?.deleteAll()
        filterMapData(query)
    }


    private fun toggleManualPolylineMode() {
        if (currentMode == MapMode.MANUAL_POLYLINE) {
            currentMode = MapMode.NONE
            binding.distanceText.visibility = View.GONE
            Toast.makeText(context, "Mode ukur jarak dinonaktifkan", Toast.LENGTH_SHORT).show()
        } else {
            currentMode = MapMode.MANUAL_POLYLINE
            binding.distanceText.visibility = View.VISIBLE
            clearManualPolyline()
            Toast.makeText(context, "Mode ukur jarak diaktifkan. Klik di peta.", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearManualPolyline() {
        manualPolylinePoints.clear()
        manualPointsAnnotationManager?.deleteAll()
        manualPolylineAnnotation?.let {
            lineAnnotationManager?.delete(it)
            manualPolylineAnnotation = null
        }
        updateManualPolylineDistance()
    }

    private fun checkLocationPermissionAndCenter() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fetchAndCenterOnUserLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchAndCenterOnUserLocation() {
        mapView?.location?.enabled = true
        val fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let {
                val point = Point.fromLngLat(it.longitude, it.latitude)
                mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().center(point).zoom(14.0).build())
            }
        }
    }

    private fun fetchMapData() {
        binding.progressBar.visibility = View.VISIBLE
        apiService.getPelangganMap().enqueue(object : Callback<GetPelangganResponse> {
            override fun onResponse(call: Call<GetPelangganResponse>, response: Response<GetPelangganResponse>) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                val body = response.body()
                if (response.isSuccessful && body != null && body.status) {
                    val listType = object : TypeToken<List<PelangganMapData>>() {}.type
                    allPelangganList = try { Gson().fromJson(body.data, listType) } catch (e: Exception) { emptyList() }
                    setupMap(allPelangganList)
                } else {
                    Toast.makeText(context, "Gagal memuat data awal: ${body?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<GetPelangganResponse>, t: Throwable) {
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun filterMapData(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            allPelangganList
        } else {
            val lowerCaseQuery = query.lowercase()
            allPelangganList.filter { 
                it.nama.lowercase().contains(lowerCaseQuery) || 
                (it.alamat != null && it.alamat.lowercase().contains(lowerCaseQuery))
            }
        }
        setupMap(filteredList)
    }

    private fun setupMap(pelangganList: List<PelangganMapData>) {
        val styleUri = mapStyles[currentStyleIndex].second
        mapView?.getMapboxMap()?.loadStyleUri(styleUri) { style ->
            val locationIcon = bitmapFromVector(requireContext(), android.R.drawable.ic_menu_mylocation)
            if (locationIcon != null) {
                style.addImage("location_icon", locationIcon)
            }

            val annotationApi = mapView?.annotations
            pointAnnotationManager?.deleteAll()
            
            if (pointAnnotationManager == null) {
                pointAnnotationManager = annotationApi?.createPointAnnotationManager()
            }

            if (lineAnnotationManager == null) {
                 lineAnnotationManager = annotationApi?.createPolylineAnnotationManager()
            }

            if(manualPointsAnnotationManager == null) {
                manualPointsAnnotationManager = annotationApi?.createCircleAnnotationManager()
            }

            val optionsList = pelangganList.mapNotNull { p ->
                if (p.lat != null && p.lng != null) {
                    PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(p.lng, p.lat))
                        .withTextField(p.nama)
                        .withTextColor(Color.YELLOW)
                        .withTextSize(12.0)
                        .withTextAnchor(com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor.TOP)
                        .withTextOffset(listOf(0.0, 2.0))
                        .withIconImage("location_icon")
                        .withData(Gson().toJsonTree(p))
                } else null
            }

            pointAnnotationManager?.create(optionsList)

            pointAnnotationManager?.addClickListener(OnPointAnnotationClickListener { annotation ->
                if (currentMode != MapMode.MOVE_ANNOTATION) {
                    handlePointClick(annotation)
                }
                true
            })

            mapView?.getMapboxMap()?.addOnMapClickListener { point ->
                handleMapClick(point)
                true
            }
        }
    }

    private fun bitmapFromVector(context: Context, vectorResId: Int): Bitmap? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
        vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    private fun handlePointClick(annotation: PointAnnotation) {
        val json = annotation.getData()
        if (json != null && json.isJsonObject) {
            val pData = Gson().fromJson(json, PelangganMapData::class.java)

            apiService.getPelangganDetailMap(pData.id.toString()).enqueue(object : Callback<PelangganDetailMapResponse> {
                override fun onResponse(call: Call<PelangganDetailMapResponse>, response: Response<PelangganDetailMapResponse>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val detail = response.body()?.data
                        if (detail != null) {
                            val message = "ID: ${detail.idPelanggan}\n" +
                                        "Nama: ${detail.namaPelanggan}\n" +
                                        "Alamat: ${detail.alamatPelanggan}\n" +
                                        "Paket: ${detail.namaPaket}\n" +
                                        "Wilayah: ${detail.namaWilayah}\n" +
                                        "Status: ${detail.statusAktif}\n" +
                                        "Koordinat: ${detail.latitude}, ${detail.longitude}"

                            AlertDialog.Builder(requireContext())
                                .setTitle("Detail Pelanggan")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .setNeutralButton("Pindah") { _, _ ->
                                    startMoveMode(annotation)
                                }
                                .show()
                        } else {
                            Toast.makeText(context, "Detail pelanggan tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Gagal memuat detail: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PelangganDetailMapResponse>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun startMoveMode(annotation: PointAnnotation) {
        currentMode = MapMode.MOVE_ANNOTATION
        annotationToMove = annotation
        Toast.makeText(context, "Mode pemindahan aktif. Klik lokasi baru di peta.", Toast.LENGTH_LONG).show()
    }

    private fun handleMapClick(point: Point) {
        when (currentMode) {
            MapMode.MOVE_ANNOTATION -> {
                val newPoint = Point.fromLngLat(point.longitude(), point.latitude())
                annotationToMove?.let { annotation ->
                    val json = annotation.getData()
                    if (json != null && json.isJsonObject) {
                        val pData = Gson().fromJson(json, PelangganMapData::class.java)
                        updateAnnotationAndApi(pData.id, newPoint)
                    }
                }
                currentMode = MapMode.NONE
                annotationToMove = null
            }
            MapMode.ADD_LOCATION -> {
                val newPoint = Point.fromLngLat(point.longitude(), point.latitude())
                pelangganToUpdate?.let { p ->
                    updateAnnotationAndApi(p.id, newPoint)
                }
                currentMode = MapMode.NONE
                pelangganToUpdate = null
            }
            MapMode.MANUAL_POLYLINE -> {
                manualPolylinePoints.add(point)
                val circleOptions = CircleAnnotationOptions()
                    .withPoint(point)
                    .withCircleRadius(5.0)
                    .withCircleColor("#FF0000")
                    .withCircleStrokeWidth(1.5)
                    .withCircleStrokeColor("#FFFFFF")
                manualPointsAnnotationManager?.create(circleOptions)

                if (manualPolylinePoints.size > 1) {
                    manualPolylineAnnotation?.let { lineAnnotationManager?.delete(it) }
                    val lineOptions = PolylineAnnotationOptions()
                        .withPoints(manualPolylinePoints)
                        .withLineColor("#FF0000")
                        .withLineWidth(2.0)
                    manualPolylineAnnotation = lineAnnotationManager?.create(lineOptions)
                    updateManualPolylineDistance()
                }
            }
            MapMode.NONE -> { 
                // Do nothing
            }
        }
    }

    private fun updateManualPolylineDistance() {
        var totalDistance = 0.0
        if (manualPolylinePoints.size > 1) {
            for (i in 0 until manualPolylinePoints.size - 1) {
                val p1 = manualPolylinePoints[i]
                val p2 = manualPolylinePoints[i + 1]
                totalDistance += haversine(p1.latitude(), p1.longitude(), p2.latitude(), p2.longitude())
            }
        }
        binding.distanceText.text = "Jarak: ${String.format("%.2f", totalDistance)} meter"
    }
    
    private fun updateAnnotationAndApi(pelangganId: Int, newPoint: Point) {
        val request = UpdateLokasiRequest(pelangganId, newPoint.latitude(), newPoint.longitude())
        apiService.updateLokasi(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, response.body()?.message, Toast.LENGTH_SHORT).show()
                    fetchMapData() // Refresh map
                } else {
                    Toast.makeText(context, "Gagal update: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showSearchPelangganDialog() {
        if (allPelangganList.isEmpty()) {
            Toast.makeText(context, "Tidak ada data pelanggan.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = DialogPelangganSearchBinding.inflate(layoutInflater)
        val dialogRecyclerView = dialogBinding.rvPelangganDialog
        val searchView = dialogBinding.searchViewDialog

        dialogRecyclerView.layoutManager = LinearLayoutManager(context)

        var dialog: AlertDialog? = null

        val adapter = PelangganSearchAdapter(allPelangganList) { p ->
            pelangganToUpdate = p
            currentMode = MapMode.ADD_LOCATION
            Toast.makeText(context, "Klik di peta untuk lokasi ${p.nama}", Toast.LENGTH_LONG).show()
            dialog?.dismiss()
        }
        dialogRecyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = allPelangganList.filter { 
                    it.nama.contains(newText ?: "", ignoreCase = true) || 
                    it.alamat?.contains(newText ?: "", ignoreCase = true) == true
                }
                adapter.updateData(filteredList)
                return true
            }
        })

        dialog = AlertDialog.Builder(requireContext())
            .setTitle("Pilih Pelanggan")
            .setView(dialogBinding.root)
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
        _binding = null
    }
}
