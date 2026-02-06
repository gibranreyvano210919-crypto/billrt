package com.linkbit.billrt



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.linkbit.billrt.adapter.SemuaPelangganAdapter
import com.linkbit.billrt.databinding.FragmentGantiLokasiBinding
import com.linkbit.billrt.model.SemuaPelangganResponse
import com.linkbit.billrt.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GantiLokasiFragment : Fragment() {

    private var _binding: FragmentGantiLokasiBinding? = null
    private val binding get() = _binding!!

    private lateinit var semuaPelangganAdapter: SemuaPelangganAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGantiLokasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.titleTextView.text = "Data Semua Pelanggan"
        setupRecyclerView()
        setupSearchView()
        fetchSemuaPelanggan()
    }

    private fun setupRecyclerView() {
        semuaPelangganAdapter = SemuaPelangganAdapter(emptyList()) { pelanggan ->
            val id = pelanggan.idPelanggan
            val nama = pelanggan.nama
            if (id != null && nama != null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Konfirmasi Tindakan")
                    .setMessage("Anda yakin ingin mengubah lokasi untuk pelanggan: \n$nama?")
                    .setNegativeButton("Batal", null)
                    .setPositiveButton("Ya, Lanjutkan") { _, _ ->
                        val action = GantiLokasiFragmentDirections.actionGantiLokasiFragmentToUpdateFotoLokasiFragment(id, nama)
                        findNavController().navigate(action)
                    }
                    .show()
            } else {
                Toast.makeText(context, "ID atau Nama Pelanggan tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerView.apply {
            adapter = semuaPelangganAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                semuaPelangganAdapter.filter(newText.orEmpty())
                return true
            }
        })
    }

    private fun fetchSemuaPelanggan() {
        binding.progressBar.isVisible = true
        binding.recyclerView.isVisible = false
        binding.emptyTextView.isVisible = false

        RetrofitClient.instance.getSemuaPelanggan().enqueue(object : Callback<SemuaPelangganResponse> {
            override fun onResponse(call: Call<SemuaPelangganResponse>, response: Response<SemuaPelangganResponse>) {
                binding.progressBar.isVisible = false
                if (response.isSuccessful) {
                    val pelangganList = response.body()?.data ?: emptyList()
                    if (pelangganList.isEmpty()) {
                        binding.recyclerView.isVisible = false
                        binding.emptyTextView.isVisible = true
                        binding.emptyTextView.text = "Tidak ada data pelanggan."
                    } else {
                        binding.recyclerView.isVisible = true
                        binding.emptyTextView.isVisible = false
                    }
                    semuaPelangganAdapter.updateList(pelangganList)
                } else {
                    binding.emptyTextView.isVisible = true
                    binding.emptyTextView.text = "Gagal mengambil data dari server."
                    Toast.makeText(context, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SemuaPelangganResponse>, t: Throwable) {
                binding.progressBar.isVisible = false
                binding.recyclerView.isVisible = false
                binding.emptyTextView.isVisible = true
                binding.emptyTextView.text = "Error: ${t.message}"
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
