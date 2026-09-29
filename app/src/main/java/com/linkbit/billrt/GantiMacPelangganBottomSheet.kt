package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetGantiMacPelangganBinding
import com.linkbit.billrt.model.StandardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GantiMacPelangganBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetGantiMacPelangganBinding? = null
    private val binding get() = _binding!!

    private var idPelanggan: String? = null
    private var oldMac: String? = null
    private var onSuccess: (() -> Unit)? = null

    companion object {
        const val TAG = "GantiMacPelangganBS"
        fun newInstance(idPelanggan: String, oldMac: String?, onSuccess: () -> Unit): GantiMacPelangganBottomSheet {
            val fragment = GantiMacPelangganBottomSheet()
            fragment.idPelanggan = idPelanggan
            fragment.oldMac = oldMac
            fragment.onSuccess = onSuccess
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetGantiMacPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etMacAddress.setText(oldMac)

        binding.btnSimpan.setOnClickListener {
            val newMac = binding.etMacAddress.text.toString().trim()
            if (newMac.isEmpty()) {
                binding.tilMacAddress.error = "MAC Address tidak boleh kosong"
                return@setOnClickListener
            }
            updateMac(newMac)
        }
    }

    private fun updateMac(newMac: String) {
        val id = idPelanggan ?: return
        setLoading(true)

        // Menggunakan ApiConfig.apiService yang sudah ada di proyek
        ApiConfig.apiService.updateMacAddress(id, UpdateMacRequest(newMac))
            .enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                setLoading(false)
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, response.body()?.message ?: "Berhasil", Toast.LENGTH_SHORT).show()
                    onSuccess?.invoke()
                    dismiss()
                } else {
                    Toast.makeText(context, "Gagal: ${response.body()?.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                setLoading(false)
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnSimpan.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
