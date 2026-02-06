package com.linkbit.billrt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.linkbit.billrt.databinding.FragmentOdpLokasiPelangganMapBinding
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OdpLokasiPelangganMapFragment : BaseFragment() {

    private var _binding: FragmentOdpLokasiPelangganMapBinding? = null
    private val binding get() = _binding!!
    private var mapView: MapView? = null
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var lineAnnotationManager: PolylineAnnotationManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOdpLokasiPelangganMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapViewOdpLokasi
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
        mapView?.mapboxMap?.loadStyle(Style.SATELLITE_STREETS) { style ->
            val odpIcon = bitmapFromVector(requireContext(), android.R.drawable.ic_dialog_map)
            if (odpIcon != null) {
                style.addImage("odp_icon", odpIcon)
            }

            val annotationApi = mapView?.annotations
            pointAnnotationManager = annotationApi?.createPointAnnotationManager()
            lineAnnotationManager = annotationApi?.createPolylineAnnotationManager()

            val lineOptionsList = mutableListOf<PolylineAnnotationOptions>()

            val optionsList = odpList.mapNotNull { odp ->
                if (odp.latitude != null && odp.longitude != null) {
                    odp.listPorts?.forEach { port ->
                        if (port.custLat != null && port.custLng != null) {
                            val points = listOf(Point.fromLngLat(odp.longitude, odp.latitude), Point.fromLngLat(port.custLng, port.custLat))
                            lineOptionsList.add(
                                PolylineAnnotationOptions()
                                    .withPoints(points)
                                    .withLineColor(Color.RED)
                                    .withLineWidth(2.0)
                            )
                        }
                    }
                    PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(odp.longitude, odp.latitude))
                        .withTextField(odp.namaOdp)
                        .withTextColor(Color.WHITE)
                        .withIconImage("odp_icon")
                        .withData(Gson().toJsonTree(odp))
                } else null
            }

            pointAnnotationManager?.create(optionsList)
            lineAnnotationManager?.create(lineOptionsList)

            if (odpList.isNotEmpty()) {
                odpList.firstOrNull { it.latitude != null && it.longitude != null }?.let {
                    mapView?.mapboxMap?.setCamera(CameraOptions.Builder().center(Point.fromLngLat(it.longitude!!, it.latitude!!)).zoom(12.0).build())
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
        _binding = null
    }
}
