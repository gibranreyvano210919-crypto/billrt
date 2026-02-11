package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.adapter.HistoriCatatAdapter
import com.linkbit.billrt.model.HistoriCatat
import com.linkbit.billrt.model.HistoriCatatResponse
import com.linkbit.billrt.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class HistoriCatatFragment : Fragment() {

    private lateinit var rvHistoriCatat: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var spinnerWilayah: Spinner
    private lateinit var spinnerPeriode: Spinner
    private lateinit var searchView: SearchView
    private lateinit var historiCatatAdapter: HistoriCatatAdapter
    private val historiCatatList = mutableListOf<HistoriCatat>()
    private var originalHistoriCatatList = listOf<HistoriCatat>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_histori_catat, container, false)
        rvHistoriCatat = view.findViewById(R.id.rv_histori_catat)
        progressBar = view.findViewById(R.id.progress_bar)
        spinnerWilayah = view.findViewById(R.id.spinner_wilayah)
        spinnerPeriode = view.findViewById(R.id.spinner_periode)
        searchView = view.findViewById(R.id.search_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchHistoriCatat()
        setupSearchView()
    }

    private fun setupRecyclerView() {
        historiCatatAdapter = HistoriCatatAdapter(historiCatatList)
        rvHistoriCatat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historiCatatAdapter
        }
    }

    private fun fetchHistoriCatat() {
        progressBar.visibility = View.VISIBLE
        RetrofitClient.instance.getHistoriCatat().enqueue(object : Callback<HistoriCatatResponse> {
            override fun onResponse(call: Call<HistoriCatatResponse>, response: Response<HistoriCatatResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        originalHistoriCatatList = it
                        historiCatatList.clear()
                        historiCatatList.addAll(it)
                        historiCatatAdapter.notifyDataSetChanged()
                        setupFilterSpinners(it)
                        updateTitle(it.size)
                    }
                } else {
                    Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<HistoriCatatResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupFilterSpinners(data: List<HistoriCatat>) {
        // Wilayah Spinner
        val wilayahList = data.map { it.wilayah }.distinct()
        val wilayahSpinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf("Semua Wilayah") + wilayahList)
        wilayahSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerWilayah.adapter = wilayahSpinnerAdapter

        // Periode Spinner
        val periodeList = data.flatMap { it.riwayat_per_periode.map { it.periode } }.distinct()
        val periodeSpinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf("Semua Periode") + periodeList)
        periodeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPeriode.adapter = periodeSpinnerAdapter

        val selectionListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerWilayah.onItemSelectedListener = selectionListener
        spinnerPeriode.onItemSelectedListener = selectionListener
    }

    private fun filterData() {
        val selectedWilayah = spinnerWilayah.selectedItem as? String
        val selectedPeriode = spinnerPeriode.selectedItem as? String
        val searchQuery = searchView.query.toString()

        var filteredList = originalHistoriCatatList

        if (selectedWilayah != "Semua Wilayah") {
            filteredList = filteredList.filter { it.wilayah == selectedWilayah }
        }

        if (selectedPeriode != "Semua Periode") {
            filteredList = filteredList.filter { it.riwayat_per_periode.any { it.periode == selectedPeriode } }
        }

        if (searchQuery.isNotBlank()) {
            filteredList = filteredList.filter {
                it.nama_pelanggan.contains(searchQuery, ignoreCase = true) ||
                        it.mikrotik_username.contains(searchQuery, ignoreCase = true)
            }
        }

        historiCatatList.clear()
        historiCatatList.addAll(filteredList)
        historiCatatAdapter.notifyDataSetChanged()
        updateTitle(filteredList.size)
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterData()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterData()
                return true
            }
        })
    }

    private fun updateTitle(count: Int) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Histori Catat ($count)"
    }
}