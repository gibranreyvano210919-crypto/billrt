package com.linkbit.billrt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentPencarianCepatBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PencarianCepatFragment : BaseFragment() {

    private var _binding: FragmentPencarianCepatBinding? = null
    private val binding get() = _binding!!

    private lateinit var pencarianAdapter: PencarianCepatAdapter
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var allPelanggan: List<PelangganData> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPencarianCepatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        fetchAllPelanggan()
    }

    private fun setupRecyclerView() {
        pencarianAdapter = PencarianCepatAdapter(
            emptyList(),
            onCopyMacClick = { macAddress ->
                copyToClipboard(macAddress)
            },
            onRiwayatClick = { macAddress ->
                val action = PencarianCepatFragmentDirections.actionPencarianCepatFragmentToRiwayatRedamanFragment(macAddress)
                findNavController().navigate(action)
            }
        )
        binding.rvHasilPencarian.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pencarianAdapter
        }
    }

    private fun copyToClipboard(text: String) {
        if (context == null) return
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("MAC Address", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "MAC Address disalin", Toast.LENGTH_SHORT).show()
    }

    private fun setupSearchView() {
        binding.searchViewPencarian.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterResults(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable { filterResults(newText) }
                searchHandler.postDelayed(searchRunnable!!, 300) // 300ms delay
                return true
            }
        })
    }

    private fun fetchAllPelanggan() {
        _binding?.progressBarPencarian?.visibility = View.VISIBLE
        apiService.getDataPelanggan().enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (_binding == null) return
                binding.progressBarPencarian.visibility = View.GONE
                if (response.isSuccessful) {
                    allPelanggan = response.body()?.data ?: emptyList()
                    pencarianAdapter.updateData(emptyList()) 
                } else {
                    Toast.makeText(context, "Gagal memuat data pelanggan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progressBarPencarian.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterResults(query: String?) {
        if (_binding == null) return
        if (query.isNullOrBlank() || query.length < 3) {
            pencarianAdapter.updateData(emptyList())
            binding.tvPencarianEmpty.text = "Ketik minimal 3 huruf untuk memulai..."
            binding.tvPencarianEmpty.visibility = View.VISIBLE
            return
        }

        val filteredList = allPelanggan.filter {
            it.nama.contains(query, ignoreCase = true) ||
            it.alamat?.contains(query, ignoreCase = true) == true ||
            it.idPelanggan.contains(query, ignoreCase = true) ||
            it.macAddress?.contains(query, ignoreCase = true) == true
        }
        
        pencarianAdapter.updateData(filteredList)
        binding.tvPencarianEmpty.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
        binding.tvPencarianEmpty.text = "Tidak ada hasil untuk $query"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        _binding = null
    }
}
