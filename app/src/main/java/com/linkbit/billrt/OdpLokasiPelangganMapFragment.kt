package com.linkbit.billrt

import android.R
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.linkbit.billrt.databinding.FragmentOdpLokasiPelangganMapBinding
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OdpLokasiPelangganMapFragment : BaseFragment() {

    private var _binding: FragmentOdpLokasiPelangganMapBinding? = null
    private val binding get() = _binding!!
    private var mapView: MapView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOdpLokasiPelangganMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapViewOdpLokasi
        mapView?.setTileSource(TileSourceFactory.MAPNIK)
        mapView?.setMultiTouchControls(true)
        fetchOdpData()
    }

    private fun fetchOdpData() {
        binding.progressBarOdpLokasi.visibility = View.VISIBLE
        apiService.getOdpLokasiPelanggan().enqueue(object : Callback<OdpLokasiPelangganResponse> {
            override fun onResponse(call: Call<OdpLokasiPelangganResponse>, response: Response<OdpLokasiPelangganResponse>) {
                if (!isAdded || _binding == null) return
                binding.progressBarOdpLokasi.visibility = View.GONE
                val body = response.body()
                if (response.isSuccessful && body != null && body.status) {
                    setupMap(body.data)
                } else {
                    Toast.makeText(context, "Gagal memuat data ODP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OdpLokasiPelangganResponse>, t: Throwable) {
                if (isAdded) {
                    binding.progressBarOdpLokasi.visibility = View.GONE
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupMap(odpList: List<OdpData>) {
        val currentMapView = mapView ?: return
        currentMapView.overlays.clear()

        val odpIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_dialog_map)

        odpList.forEach { odp ->
            val odpLat = odp.latitude
            val odpLng = odp.longitude
            if (odpLat != null && odpLng != null) {
                val odpPoint = GeoPoint(odpLat, odpLng)

                // Add ODP Marker
                val marker = Marker(currentMapView).apply {
                    position = odpPoint
                    title = odp.namaOdp
                    icon = odpIcon
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                currentMapView.overlays.add(marker)

                // Add lines to ports
                odp.listPorts?.forEach { port ->
                    val custLat = port.custLat
                    val custLng = port.custLng
                    if (custLat != null && custLng != null) {
                        val custPoint = GeoPoint(custLat, custLng)
                        val polyline = Polyline(currentMapView).apply {
                            setPoints(listOf(odpPoint, custPoint))
                            outlinePaint.color = Color.RED
                            outlinePaint.strokeWidth = 4f
                        }
                        currentMapView.overlays.add(polyline)
                    }
                }
            }
        }

        if (odpList.isNotEmpty()) {
            odpList.firstOrNull { it.latitude != null && it.longitude != null }?.let {
                currentMapView.controller.setZoom(13.0)
                currentMapView.controller.setCenter(GeoPoint(it.latitude!!, it.longitude!!))
            }
        }

        currentMapView.invalidate()
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
