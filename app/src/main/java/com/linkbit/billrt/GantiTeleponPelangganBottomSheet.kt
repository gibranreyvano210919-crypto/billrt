package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetGantiTeleponPelangganBinding
import com.linkbit.billrt.model.StandardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GantiTeleponPelangganBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetGantiTeleponPelangganBinding? = null
    private val binding get() = _binding!!

    private var idPelanggan: String? = null
    private var oldPhone: String? = null
    private var onSuccess: (() -> Unit)? = null

    companion object {
        const val TAG = "GantiTeleponPelangganBS"
        fun newInstance(idPelanggan: String, oldPhone: String?, onSuccess: () -> Unit): GantiTeleponPelangganBottomSheet {
            val fragment = GantiTeleponPelangganBottomSheet()
            fragment.idPelanggan = idPelanggan
            fragment.oldPhone = oldPhone
            fragment.onSuccess = onSuccess
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetGantiTeleponPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etTelepon.setText(oldPhone)

        binding.btnSimpan.setOnClickListener {
            val newPhone = binding.etTelepon.text.toString().trim()
            if (newPhone.isEmpty()) {
                binding.tilTelepon.error = "Nomor telepon tidak boleh kosong"
                return@setOnClickListener
            }
            updateTelepon(newPhone)
        }
    }

    private fun updateTelepon(newPhone: String) {
        val id = idPelanggan ?: return
        setLoading(true)

        ApiConfig.apiService.updateTeleponPelanggan("update_telepon_pelanggan", id, UpdateTeleponRequest(newPhone))
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
