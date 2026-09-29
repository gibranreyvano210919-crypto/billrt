package com.linkbit.billrt

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.linkbit.billrt.adapter.PelangganSearchAdapter
import com.linkbit.billrt.adapter.WilayahFilterAdapter
import com.linkbit.billrt.databinding.BottomSheetFilterWilayahBinding
import com.linkbit.billrt.databinding.BottomSheetMapToolsBinding
import com.linkbit.billrt.databinding.DialogPelangganSearchBinding
import com.linkbit.billrt.databinding.FragmentMapPelangganBinding
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

class MapPelangganFragment : BaseFragment() {

    private var _binding: FragmentMapPelangganBinding? = null
    private val binding get() = _binding!!
    private var mapView: MapView? = null
    private var markerToMove: Marker? = null

    private var allPelangganList = listOf<PelangganMapData>()
    private var allWilayahList = listOf<WilayahData>()

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    private enum class MapMode { NONE, MOVE_ANNOTATION, ADD_LOCATION, MANUAL_POLYLINE }
    private var currentMode = MapMode.NONE
    private var pelangganToUpdate: PelangganMapData? = null

    private val manualPolylinePoints = mutableListOf<GeoPoint>()
    private var manualPolylineOverlay: Polyline? = null

    private val mapStyles = listOf(
        "Jalan" to TileSourceFactory.MAPNIK,
        "Satelit" to TileSourceFactory.USGS_SAT
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
        
        applyWindowInsets(binding.appBarLayout)
        setupToolbar()
        
        mapView = binding.mapView
        mapView?.setTileSource(mapStyles[currentStyleIndex].second)
        mapView?.setMultiTouchControls(true)

        setupSearchView()
        fetchMapData()
        checkLocationPermissionAndCenter()

        binding.fabMyLocation.setOnClickListener {
            checkLocationPermissionAndCenter()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_tools -> {
                    showMapToolsBottomSheet()
                    true
                }
                else -> false
            }
        }
    }

    private fun showMapToolsBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetMapToolsBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding.buttonFilterWilayah.setOnClickListener {
            dialog.dismiss()
            showWilayahFilterBottomSheet()
        }

        sheetBinding.buttonAddLocation.setOnClickListener {
            dialog.dismiss()
            showSearchPelangganDialog()
        }

        sheetBinding.buttonMeasure.setOnClickListener {
            dialog.dismiss()
            toggleManualPolylineMode()
        }

        sheetBinding.buttonClearMeasure.setOnClickListener {
            dialog.dismiss()
            clearManualPolyline()
        }

        sheetBinding.buttonChangeStyle.setOnClickListener {
            dialog.dismiss()
            showMapStyleDialog()
        }

        dialog.show()
    }

    private fun showWilayahFilterBottomSheet() {
        if (allWilayahList.isEmpty()) {
            Toast.makeText(context, "Data wilayah belum dimuat.", Toast.LENGTH_SHORT).show()
            return
        }
        val dialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = BottomSheetFilterWilayahBinding.inflate(layoutInflater)
        dialog.setContentView(bottomSheetBinding.root)

        val wilayahCounts = allWilayahList.map { it.namaWilayah to it.statistik.totalPelangganAktif }

        val adapter = WilayahFilterAdapter(wilayahCounts) { selectedWilayah ->
            filterMapByWilayah(selectedWilayah)
            dialog.dismiss()
        }

        bottomSheetBinding.rvWilayahFilter.layoutManager = LinearLayoutManager(context)
        bottomSheetBinding.rvWilayahFilter.adapter = adapter

        bottomSheetBinding.buttonShowAll.setOnClickListener {
            filterMapByWilayah(null)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun filterMapByWilayah(wilayah: String?) {
        val filteredList = if (wilayah == null) {
            allPelangganList
        } else {
            allPelangganList.filter { it.namaWilayah == wilayah }
        }
        setupMap(filteredList)
        if (wilayah != null) {
            Toast.makeText(context, "Menampilkan wilayah: $wilayah", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Menampilkan semua wilayah", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMapStyleDialog() {
        val styleNames = mapStyles.map { it.first }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Gaya Peta")
            .setItems(styleNames) { _, which ->
                currentStyleIndex = which
                mapView?.setTileSource(mapStyles[currentStyleIndex].second)
                mapView?.invalidate()
            }
            .show()
    }

    private fun setupSearchView() {
        binding.toolbar.inflateMenu(R.menu.menu_map_pelanggan)
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.apply {
            queryHint = "Cari pelanggan atau koordinat..."
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchRunnable?.let { searchHandler.removeCallbacks(it) }
                    handleSearch(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    searchRunnable?.let { searchHandler.removeCallbacks(it) }
                    searchRunnable = Runnable { handleSearch(newText) }
                    searchHandler.postDelayed(searchRunnable!!, 1000)
                    return true
                }
            })
        }
    }

    private fun handleSearch(query: String?) {
        if (!query.isNullOrBlank()) {
            val parts = query.split(',').map { it.trim() }
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                    val point = GeoPoint(lat, lng)
                    mapView?.controller?.setZoom(18.0)
                    mapView?.controller?.animateTo(point)
                    Toast.makeText(context, "Menuju ke koordinat...", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        fetchMapData(query)
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
        manualPolylineOverlay?.let { mapView?.overlays?.remove(it) }
        manualPolylineOverlay = null
        mapView?.invalidate()
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
        val fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let {
                val point = GeoPoint(it.latitude, it.longitude)
                mapView?.controller?.setZoom(14.0)
                mapView?.controller?.animateTo(point)
            }
        }
    }

    private fun fetchMapData(cari: String? = null) {
        binding.progressBar.visibility = View.VISIBLE
        apiService.getWilayahPelangganNested(cari).enqueue(object : Callback<WilayahPelangganNestedResponse> {
            override fun onResponse(call: Call<WilayahPelangganNestedResponse>, response: Response<WilayahPelangganNestedResponse>) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                val body = response.body()
                if (response.isSuccessful && body != null && body.status) {
                    allWilayahList = body.data
                    allPelangganList = allWilayahList.flatMap { wilayah ->
                        wilayah.daftarPelanggan.map { pelanggan ->
                            PelangganMapData(
                                id = pelanggan.id,
                                nama = pelanggan.nama ?: "",
                                lat = pelanggan.lat,
                                lng = pelanggan.lng,
                                namaWilayah = wilayah.namaWilayah,
                                alamat = null
                            )
                        }
                    }
                    setupMap(allPelangganList)
                    
                    if (!cari.isNullOrBlank()) {
                        if (allPelangganList.isNotEmpty()) {
                            val found = allPelangganList.firstOrNull { it.lat != null && it.lng != null }
                            if (found != null) {
                                val point = GeoPoint(found.lat!!, found.lng!!)
                                mapView?.controller?.setZoom(18.5)
                                mapView?.controller?.animateTo(point)
                                Toast.makeText(context, "Ditemukan: ${found.nama}", Toast.LENGTH_SHORT).show()
                                showPelangganDetailDialog(found)
                            } else {
                                Toast.makeText(context, "Pelanggan ditemukan, namun koordinat belum diatur", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "⚠️ Pelanggan '$cari' tidak ditemukan", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Gagal memuat data: ${body?.message ?: "Terjadi kesalahan"}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WilayahPelangganNestedResponse>, t: Throwable) {
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupMap(pelangganList: List<PelangganMapData>) {
        val currentMapView = mapView ?: return
        currentMapView.overlays.clear()

        val locationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_pelanggan)

        pelangganList.forEach { p ->
            val lat = p.lat
            val lng = p.lng
            if (lat != null && lng != null) {
                val point = GeoPoint(lat, lng)
                val marker = Marker(currentMapView).apply {
                    position = point
                    title = p.nama
                    snippet = "ID: ${p.id} | Wilayah: ${p.namaWilayah}"
                    icon = locationIcon
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    relatedObject = p
                    setOnMarkerClickListener { m, _ ->
                        if (currentMode != MapMode.MOVE_ANNOTATION) {
                            val pData = m.relatedObject as? PelangganMapData
                            if (pData != null) {
                                showPelangganDetailDialog(pData, m)
                            }
                        }
                        true
                    }
                }
                currentMapView.overlays.add(marker)
            }
        }

        // Map Click Receiver
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

        currentMapView.invalidate()
    }

    private fun showPelangganDetailDialog(pData: PelangganMapData, marker: Marker? = null) {
        AlertDialog.Builder(requireContext())
            .setTitle("Detail Pelanggan")
            .setMessage("ID: ${pData.id}\nNama: ${pData.nama}\nWilayah: ${pData.namaWilayah}\nKoordinat: ${pData.lat}, ${pData.lng}")
            .setPositiveButton("OK", null)
            .setNeutralButton("Pindah") { _, _ ->
                if (marker != null) {
                    startMoveMode(marker)
                } else {
                    Toast.makeText(context, "Gunakan marker di peta untuk memindahkan", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun startMoveMode(marker: Marker) {
        currentMode = MapMode.MOVE_ANNOTATION
        markerToMove = marker
        Toast.makeText(context, "Mode pemindahan aktif. Klik lokasi baru di peta.", Toast.LENGTH_LONG).show()
    }

    private fun handleMapClick(point: GeoPoint) {
        val currentMapView = mapView ?: return
        when (currentMode) {
            MapMode.MOVE_ANNOTATION -> {
                markerToMove?.let { marker ->
                    val pData = marker.relatedObject as? PelangganMapData
                    if (pData != null) {
                        updateAnnotationAndApi(pData.id, point)
                    }
                }
                currentMode = MapMode.NONE
                markerToMove = null
            }
            MapMode.ADD_LOCATION -> {
                pelangganToUpdate?.let { p ->
                    updateAnnotationAndApi(p.id, point)
                }
                currentMode = MapMode.NONE
                pelangganToUpdate = null
            }
            MapMode.MANUAL_POLYLINE -> {
                manualPolylinePoints.add(point)
                if (manualPolylinePoints.size > 1) {
                    manualPolylineOverlay?.let { currentMapView.overlays.remove(it) }
                    manualPolylineOverlay = Polyline(currentMapView).apply {
                        setPoints(manualPolylinePoints)
                        outlinePaint.color = Color.RED
                        outlinePaint.strokeWidth = 4f
                    }
                    currentMapView.overlays.add(manualPolylineOverlay)
                    currentMapView.invalidate()
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
                totalDistance += p1.distanceToAsDouble(p2)
            }
        }
        binding.distanceText.text = "Jarak: ${String.format("%.2f", totalDistance)} meter"
    }
    
    private fun updateAnnotationAndApi(pelangganId: String, newPoint: GeoPoint) {
        val request = UpdateLokasiRequest(pelangganId.toInt(), newPoint.latitude, newPoint.longitude)
        apiService.updateLokasi(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, response.body()?.message, Toast.LENGTH_SHORT).show()
                    fetchMapData()
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
        val searchView = dialogBinding.searchViewDialog
        val dialogRecyclerView = dialogBinding.rvPelangganDialog
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
                    it.nama.contains(newText ?: "", ignoreCase = true)
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
