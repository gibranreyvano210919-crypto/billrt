package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.linkbit.billrt.databinding.FragmentMasterPaketBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MasterPaketFragment : BaseFragment() {

    private var _binding: FragmentMasterPaketBinding? = null
    private val binding get() = _binding!!
    private lateinit var paketAdapter: MasterPaketAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMasterPaketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchPaket()

        binding.fabAddMasterPaket.setOnClickListener {
            showEditPaketDialog(null)
        }
    }

    private fun setupRecyclerView() {
        paketAdapter = MasterPaketAdapter(emptyList(),
            onEditClick = { paket -> showEditPaketDialog(paket) },
            onDeleteClick = { paket -> showDeleteConfirmationDialog(paket) }
        )
        binding.rvMasterPaket.layoutManager = LinearLayoutManager(context)
        binding.rvMasterPaket.adapter = paketAdapter
    }

    private fun fetchPaket() {
        binding.progressBar.visibility = View.VISIBLE
        apiService.getPaket().enqueue(object : Callback<PaketResponse> {
            override fun onResponse(call: Call<PaketResponse>, response: Response<PaketResponse>) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val paketList = response.body()?.data ?: emptyList()
                    paketAdapter.updateData(paketList)
                } else {
                    Toast.makeText(context, "Gagal mengambil data paket", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PaketResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEditPaketDialog(paket: Paket?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_paket, null)
        val etNamaPaket = dialogView.findViewById<TextInputEditText>(R.id.etNamaPaket)
        val etHarga = dialogView.findViewById<TextInputEditText>(R.id.etHarga)
        val etKecepatan = dialogView.findViewById<TextInputEditText>(R.id.etKecepatan)

        paket?.let {
            etNamaPaket.setText(it.nama_paket)
            etHarga.setText(it.harga.toString())
            etKecepatan.setText(it.kecepatan)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (paket == null) "Tambah Paket" else "Edit Paket")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNamaPaket.text.toString()
                val harga = etHarga.text.toString().toFloatOrNull() ?: 0f
                val kecepatan = etKecepatan.text.toString()
                if (nama.isNotEmpty()) {
                    savePaket(SimpanPaketRequest(paket?.id_paket, nama, harga, kecepatan))
                } else {
                    Toast.makeText(context, "Nama paket tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun savePaket(request: SimpanPaketRequest) {
        apiService.simpanPaket(request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    fetchPaket()
                    Toast.makeText(context, "Data paket berhasil disimpan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal menyimpan: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<StandardResponse>, t: Throwable) { Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show() }
        })
    }

    private fun showDeleteConfirmationDialog(paket: Paket) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Paket")
            .setMessage("Anda yakin ingin menghapus paket '${paket.nama_paket}'?")
            .setPositiveButton("Hapus") { _, _ -> deletePaket(paket.id_paket) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deletePaket(id: Int) {
        apiService.hapusPaket(HapusPaketRequest(id)).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    fetchPaket()
                } else {
                    Toast.makeText(context, "Gagal menghapus: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<StandardResponse>, t: Throwable) { Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show() }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}