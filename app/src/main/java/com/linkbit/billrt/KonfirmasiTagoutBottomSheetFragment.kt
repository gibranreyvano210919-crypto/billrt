package com.linkbit.billrt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetKonfirmasiTagoutBinding
import com.linkbit.billrt.model.PelangganBelumBayarItem
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class KonfirmasiTagoutBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetKonfirmasiTagoutBinding? = null
    private val binding get() = _binding!!

    private var item: PelangganBelumBayarItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetKonfirmasiTagoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        item = arguments?.getSerializable("pelanggan_item") as? PelangganBelumBayarItem

        if (item == null) {
            Toast.makeText(context, "Error: Data pelanggan tidak ditemukan", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        binding.tvNamaPelanggan.text = "Nama: ${item?.namaPelanggan}"
        binding.tvInvoiceDetail.text = "Invoice: ${item?.invoice ?: "Tidak Tersedia"}"

        binding.btnKonfirmasiTagout.setOnClickListener {
            val alasan = binding.etAlasan.text.toString().trim()
            if (alasan.isEmpty()) {
                binding.tilAlasan.error = "Alasan wajib diisi"
                return@setOnClickListener
            }
            
            val idInvoice = item?.invoice
            
            if (idInvoice.isNullOrEmpty()) {
                Toast.makeText(context, "Error: ID Tagihan (Invoice) tidak ditemukan", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            binding.tilAlasan.error = null
            tagoutPelanggan(idInvoice, alasan)
        }

        binding.btnBatal.setOnClickListener {
            dismiss()
        }
    }

    private fun tagoutPelanggan(idTagihan: String, alasan: String) {
        binding.btnKonfirmasiTagout.isEnabled = false
        binding.btnKonfirmasiTagout.text = "Memproses..."

        lifecycleScope.launch {
            try {
                // Sekarang menggunakan parameter tabel sebagai Query agar kompatibel dengan backend
                val response = ApiClient.tagihanApiService.postTagout(
                    tabel = "post_tagout",
                    idTagihan = idTagihan,
                    alasan = alasan,
                    idUser = 1
                )
                
                Log.d("TagoutDebug", "Respon Server: status=${response.status}, message=${response.message}")

                if (response.status) {
                    Toast.makeText(context, response.message ?: "Tagout Berhasil", Toast.LENGTH_LONG).show()
                    setFragmentResult("tagout_successful", bundleOf("refresh" to true))
                    dismiss()
                } else {
                    val errorMsg = response.message ?: "Gagal: Server menolak (Status False)"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    Log.e("TagoutDebug", "Server Response Error: $errorMsg")
                    resetButton()
                }
            } catch (e: Exception) {
                val exceptionMsg = e.localizedMessage ?: "Koneksi Bermasalah"
                Toast.makeText(context, "Gagal Tagout: $exceptionMsg", Toast.LENGTH_LONG).show()
                Log.e("TagoutDebug", "Exception: ", e)
                resetButton()
            }
        }
    }

    private fun resetButton() {
        binding.btnKonfirmasiTagout.isEnabled = true
        binding.btnKonfirmasiTagout.text = "Konfirmasi"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(item: PelangganBelumBayarItem): KonfirmasiTagoutBottomSheetFragment {
            val fragment = KonfirmasiTagoutBottomSheetFragment()
            val args = Bundle()
            args.putSerializable("pelanggan_item", item)
            fragment.arguments = args
            return fragment
        }
    }
}
