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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
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
        
        val appBarLayout = binding.toolbarPencarian.parent as? View
        appBarLayout?.let { applyWindowInsets(it) }
        
        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        fetchAllPelanggan()

        binding.swipeRefreshPencarian.setOnRefreshListener {
            fetchAllPelanggan()
        }
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarPencarian)
        binding.toolbarPencarian.setupWithNavController(findNavController())
        binding.toolbarPencarian.title = ""
    }

    private fun setupRecyclerView() {
        pencarianAdapter = PencarianCepatAdapter(
            emptyList(),
            onCopyMacClick = { macAddress ->
                copyToClipboard(macAddress)
            },
            onItemLongClick = { pelanggan ->
                val menuSheet = CariCepatBottomSheetFragment.newInstance(
                    pelanggan,
                    onBayarMultiClick = { p ->
                        showKonfirmasiBayarMulti(p)
                    },
                    onHistoryClick = { p ->
                        val historySheet = HistoryPembayaranBottomSheetFragment.newInstance(p.idPelanggan.toIntOrNull() ?: 0)
                        historySheet.show(childFragmentManager, historySheet.tag)
                    }
                )
                menuSheet.show(childFragmentManager, "CariCepatMenu")
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
        if (!binding.swipeRefreshPencarian.isRefreshing) {
            _binding?.progressBarPencarian?.visibility = View.VISIBLE
        }
        binding.searchViewPencarian.isEnabled = false
        (activity as MainActivity).apiService.getDataPelangganCepat().enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (_binding == null) return
                binding.progressBarPencarian.visibility = View.GONE
                binding.swipeRefreshPencarian.isRefreshing = false
                binding.searchViewPencarian.isEnabled = true
                if (response.isSuccessful) {
                    val body = response.body()
                    allPelanggan = body?.data ?: emptyList()
                    filterResults(binding.searchViewPencarian.query?.toString())
                } else {
                    Toast.makeText(context, "Gagal memuat data pelanggan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progressBarPencarian.visibility = View.GONE
                binding.swipeRefreshPencarian.isRefreshing = false
                binding.searchViewPencarian.isEnabled = true
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterResults(query: String?) {
        if (_binding == null) return

        var filteredList = allPelanggan

        // Filter by Search Query (Nama, Alamat, ID, MAC Address, Username, IP)
        if (!query.isNullOrBlank()) {
            val q = query.trim()
            filteredList = filteredList.filter {
                it.nama.contains(q, ignoreCase = true) ||
                it.alamat?.contains(q, ignoreCase = true) == true ||
                it.idPelanggan.contains(q, ignoreCase = true) ||
                it.macAddress?.contains(q, ignoreCase = true) == true ||
                it.mikrotikUsername?.contains(q, ignoreCase = true) == true ||
                it.staticIp?.contains(q, ignoreCase = true) == true
            }
        }

        pencarianAdapter.updateData(filteredList)
        binding.tvPencarianEmpty.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
        binding.tvPencarianEmpty.text = if (filteredList.isEmpty()) {
            if (!query.isNullOrBlank()) "Tidak ada hasil untuk \"$query\"" else "Data pelanggan tidak tersedia."
        } else ""
    }

    private fun showKonfirmasiBayarMulti(pelanggan: PelangganData) {
        binding.progressBarPencarian.visibility = View.VISIBLE
        apiService.getPelangganBelumBayarAll(pelanggan.idPelanggan).enqueue(object : Callback<TagihanBelumBayarResponse> {
            override fun onResponse(call: Call<TagihanBelumBayarResponse>, response: Response<TagihanBelumBayarResponse>) {
                if (_binding == null) return
                binding.progressBarPencarian.visibility = View.GONE
                if (response.isSuccessful && response.body()?.status == true) {
                    val list = response.body()?.data
                    if (!list.isNullOrEmpty()) {
                        val sheet = KonfirmasiBayarMultiBottomSheetFragment.newInstance(list[0])
                        sheet.show(childFragmentManager, "KonfirmasiBayarMultiBottomSheet")
                    } else {
                        Toast.makeText(context, "Data tagihan tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil data tagihan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TagihanBelumBayarResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progressBarPencarian.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        _binding = null
    }
}
