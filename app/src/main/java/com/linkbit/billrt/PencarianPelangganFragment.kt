package com.linkbit.billrt

import android.content.Context
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.AutoCompleteAdapter
import com.linkbit.billrt.adapter.RadiusPelangganAdapter
import com.linkbit.billrt.databinding.FragmentPencarianPelangganBinding
import com.linkbit.billrt.model.*
import com.linkbit.billrt.network.RetrofitClient
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PencarianPelangganFragment : BaseFragment() {

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
        mapView?.setTileSource(TileSourceFactory.MAPNIK)
        mapView?.setMultiTouchControls(true)

        val appBarLayout = binding.toolbarPencarianPelanggan.parent as? View
        appBarLayout?.let { applyWindowInsets(it) }

        setupToolbar()
        setupResultRecyclerView()
        setupAutoCompleteRecyclerView()

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

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarPencarianPelanggan)
        binding.toolbarPencarianPelanggan.setupWithNavController(findNavController())
        binding.toolbarPencarianPelanggan.title = "Pencarian Pelanggan"
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
            binding.pelangganRecyclerView.isVisible = false
            radiusAdapter.updateData(null, null)
            updateMarkers(null, null)
        } else {
             binding.pelangganRecyclerView.isVisible = true
        }
    }

    private fun updateMarkers(pusat: PelangganPusat?, tetangga: List<PelangganTetangga>?) {
        val currentMapView = mapView ?: return
        currentMapView.overlays.clear()

        val pusatDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_pusat)
        val tetanggaDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_tetangga)

        pusat?.let { p ->
            val lat = p.latitude
            val lon = p.longitude
            if (lat != null && lon != null) {
                val point = GeoPoint(lat, lon)
                val marker = Marker(currentMapView).apply {
                    position = point
                    title = p.nama
                    icon = pusatDrawable
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                currentMapView.overlays.add(marker)

                currentMapView.controller.setZoom(16.0)
                currentMapView.controller.animateTo(point)
            }
        }

        tetangga?.forEach { t ->
            val lat = t.latitude
            val lon = t.longitude
            if (lat != null && lon != null) {
                val point = GeoPoint(lat, lon)
                val marker = Marker(currentMapView).apply {
                    position = point
                    title = t.nama
                    icon = tetanggaDrawable
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                currentMapView.overlays.add(marker)
            }
        }

        currentMapView.invalidate()
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
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
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        _binding = null
    }
}
