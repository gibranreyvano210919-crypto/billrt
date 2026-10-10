package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetPelangganBinding
import com.linkbit.billrt.model.StandardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPelangganBinding? = null
    private val binding get() = _binding!!

    private var onEditClickListener: ((String) -> Unit)? = null
    private var onStatusUpdateSuccessful: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getString(ARG_PELANGGAN_ID) ?: ""
        val name = arguments?.getString(ARG_PELANGGAN_NAME) ?: ""

        binding.tvPelangganName.text = name

        // 1. Menu Edit Pelanggan
        binding.optionEditPelanggan.setOnClickListener {
            onEditClickListener?.invoke(id)
            dismiss()
        }

        // 2. Menu Detail Pelanggan
        binding.optionDetailPelanggan.setOnClickListener {
            val bundle = Bundle().apply {
                putString("pelangganId", id)
            }
            try {
                findNavController().navigate(R.id.detailPelangganFragment, bundle)
            } catch (e: Exception) {
                Toast.makeText(context, "Navigasi detail tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }

        // 3. Menu Ubah Status
        binding.optionUbahStatus.setOnClickListener {
            val options = arrayOf("Isolir Pelanggan", "Nonaktifkan Pelanggan")
            AlertDialog.Builder(requireContext())
                .setTitle("Ubah Status Pelanggan")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> confirmAction("Isolir", name) { isolirPelanggan(id) }
                        1 -> confirmAction("Nonaktifkan", name) { nonaktifkanPelanggan(id) }
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        // 4. Menu Edit Secret PPPoE
        binding.optionEditSecret.setOnClickListener {
            Toast.makeText(context, "Fitur Secret PPPoE segera hadir", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun confirmAction(action: String, name: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("$action Pelanggan")
            .setMessage("Apakah Anda yakin ingin $action pelanggan '$name'?")
            .setPositiveButton("Ya") { _, _ -> onConfirm() }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun isolirPelanggan(id: String) {
        ApiConfig.apiService.isolirPelanggan(id).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, response.body()?.message ?: "Berhasil di-isolir", Toast.LENGTH_SHORT).show()
                    onStatusUpdateSuccessful?.invoke()
                    dismiss()
                } else {
                    Toast.makeText(context, "Gagal: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun nonaktifkanPelanggan(id: String) {
        ApiConfig.apiService.nonaktifPelanggan(id).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, response.body()?.message ?: "Berhasil dinonaktifkan", Toast.LENGTH_SHORT).show()
                    onStatusUpdateSuccessful?.invoke()
                    dismiss()
                } else {
                    Toast.makeText(context, "Gagal: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun setOnEditClickListener(listener: (String) -> Unit) {
        onEditClickListener = listener
    }

    fun setOnStatusUpdateSuccessfulListener(listener: () -> Unit) {
        onStatusUpdateSuccessful = listener
    }

    // Alias for backward compatibility if needed by existing callers
    fun setOnIsolirSuccessfulListener(listener: () -> Unit) {
        onStatusUpdateSuccessful = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PELANGGAN_NAME = "pelanggan_name"
        private const val ARG_PELANGGAN_ID = "pelanggan_id"

        fun newInstance(pelangganId: String, pelangganName: String): PelangganBottomSheetFragment {
            return PelangganBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PELANGGAN_ID, pelangganId)
                    putString(ARG_PELANGGAN_NAME, pelangganName)
                }
            }
        }
    }
}
