package com.linkbit.billrt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.AutoCompleteAdapter
import com.linkbit.billrt.adapter.RadiusPelangganAdapter
import com.linkbit.billrt.databinding.FragmentPencarianPelangganBinding
import com.linkbit.billrt.model.*
import com.linkbit.billrt.network.RetrofitClient
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PencarianPelangganFragment : Fragment() {

    private var _binding: FragmentPencarianPelangganBinding? = null
    private val binding get() = _binding!!

    private var mapView: MapView? = null
    private lateinit var radiusAdapter: RadiusPelangganAdapter
    private lateinit var autoCompleteAdapter: AutoCompleteAdapter

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPencarianPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapView

        setupResultRecyclerView()
        setupAutoCompleteRecyclerView()
        mapView?.getMapboxMap()?.loadStyleUri("mapbox://styles/mapbox/satellite-streets-v12")

        binding.gantiLokasiButton.setOnClickListener {
            findNavController().navigate(R.id.action_pencarianPelangganFragment_to_gantiLokasiFragment)
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
            }

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                showResultView(clearPrevious = true)

                if (query.length < 2) {
                    binding.autoCompleteRecyclerView.isVisible = false
                    return
                }

                binding.searchProgressBar.isVisible = true
                searchRunnable = Runnable { searchAutocomplete(query) }
                searchHandler.postDelayed(searchRunnable!!, 500)
            }
        })
    }

    private fun setupResultRecyclerView() {
        radiusAdapter = RadiusPelangganAdapter(emptyList())
        binding.pelangganRecyclerView.apply {
            adapter = radiusAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupAutoCompleteRecyclerView() {
        autoCompleteAdapter = AutoCompleteAdapter(emptyList()) { item ->
            binding.searchEditText.clearFocus()
            binding.autoCompleteRecyclerView.isVisible = false
            hideKeyboard()
            item.idPelanggan?.let { searchRadiusById(it) }
        }
        binding.autoCompleteRecyclerView.apply {
            adapter = autoCompleteAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun searchAutocomplete(query: String) {
        RetrofitClient.instance.searchAutocomplete(query).enqueue(object : Callback<SearchAutoCompleteResponse> {
            override fun onResponse(call: Call<SearchAutoCompleteResponse>, response: Response<SearchAutoCompleteResponse>) {
                binding.searchProgressBar.isVisible = false
                if (response.isSuccessful && response.body()?.status == true) {
                    val items = response.body()?.data ?: emptyList()
                    autoCompleteAdapter.updateData(items)
                    binding.autoCompleteRecyclerView.isVisible = items.isNotEmpty()
                } else {
                    binding.autoCompleteRecyclerView.isVisible = false
                    val errorMsg = "Gagal mencari: (Code: ${response.code()}) ${response.message()}"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("PencarianPelanggan", "API Autocomplete Error: $errorMsg")
                }
            }

            override fun onFailure(call: Call<SearchAutoCompleteResponse>, t: Throwable) {
                 binding.searchProgressBar.isVisible = false
                 binding.autoCompleteRecyclerView.isVisible = false
                 Toast.makeText(context, "Error Jaringan Autocomplete: ${t.message}", Toast.LENGTH_LONG).show()
                 Log.e("PencarianPelanggan", "API Autocomplete Failure", t)
            }
        })
    }

    private fun searchRadiusById(idPelanggan: String) {
        binding.searchProgressBar.isVisible = true
        showResultView(isLoading = true)
        RetrofitClient.instance.searchRadiusById(idPelanggan).enqueue(object : Callback<RadiusByIdResponse> {
            override fun onResponse(call: Call<RadiusByIdResponse>, response: Response<RadiusByIdResponse>) {
                binding.searchProgressBar.isVisible = false
                if (response.isSuccessful && response.body()?.status == true) {
                    val radiusResponse = response.body()
                    showResultView(isLoading = false, isError = false)
                    radiusAdapter.updateData(radiusResponse?.pusat, radiusResponse?.tetangga)
                    updateMarkers(radiusResponse?.pusat, radiusResponse?.tetangga)
                } else {
                    val message = response.body()?.message ?: "Pelanggan ditemukan, tapi koordinat GPS kosong."
                    showResultView(isLoading = false, isError = true, errorMessage = message)
                }
            }

            override fun onFailure(call: Call<RadiusByIdResponse>, t: Throwable) {
                binding.searchProgressBar.isVisible = false
                showResultView(isLoading = false, isError = true, errorMessage = "Error: ${t.message}")
            }
        })
    }

    private fun showResultView(isLoading: Boolean = false, isError: Boolean = false, errorMessage: String? = null, clearPrevious: Boolean = false) {
        if(clearPrevious) {
            binding.mapCard.isVisible = false
            binding.pelangganCard.isVisible = false
            return
        }

        binding.progressBar.isVisible = isLoading
        binding.mapCard.isVisible = !isLoading && !isError
        binding.pelangganCard.isVisible = !isLoading
        binding.statusLayout.isVisible = isError
        if (isError) {
            binding.statusTextView.text = errorMessage
            binding.gantiLokasiButton.isVisible = true
            binding.pelangganRecyclerView.isVisible = false
            radiusAdapter.updateData(null, null)
            updateMarkers(null, null)
        } else {
             binding.pelangganRecyclerView.isVisible = true
        }
    }

    private fun updateMarkers(pusat: PelangganPusat?, tetangga: List<PelangganTetangga>?) {
        val pusatBitmap = bitmapFromDrawableRes(requireContext(), R.drawable.ic_pusat)
        val tetanggaBitmap = bitmapFromDrawableRes(requireContext(), R.drawable.ic_tetangga)

        mapView?.annotations?.createPointAnnotationManager()?.let { annotationManager ->
            annotationManager.deleteAll()
            pusat?.let { p ->
                p.latitude?.let { lat -> p.longitude?.let { lon ->
                    val point = Point.fromLngLat(lon, lat)
                    val options = PointAnnotationOptions().withPoint(point).withTextField(p.nama ?: "").withTextOffset(listOf(0.0, -2.5)).withTextColor(Color.WHITE).withTextHaloColor(Color.argb(192, 0, 0, 0)).withTextHaloWidth(1.5)
                    pusatBitmap?.let { options.withIconImage(it) }
                    annotationManager.create(options)

                    // Autozoom ke titik pusat dengan level zoom tetap
                    mapView?.getMapboxMap()?.flyTo(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(16.0) // Skala 200 kaki
                            .build()
                    )
                }}
            }
            tetangga?.forEach { t ->
                t.latitude?.let { lat -> t.longitude?.let { lon ->
                    val point = Point.fromLngLat(lon, lat)
                    val options = PointAnnotationOptions().withPoint(point).withTextField(t.nama ?: "").withTextOffset(listOf(0.0, -2.5)).withTextColor(Color.WHITE).withTextHaloColor(Color.argb(192, 0, 0, 0)).withTextHaloWidth(1.5)
                    tetanggaBitmap?.let { options.withIconImage(it) }
                    annotationManager.create(options)
                }}
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int): Bitmap? {
        return ContextCompat.getDrawable(context, resourceId)?.let {
            val bitmap = Bitmap.createBitmap(it.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap).apply { it.setBounds(0, 0, width, height); it.draw(this) }
            bitmap
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        _binding = null
    }
}
