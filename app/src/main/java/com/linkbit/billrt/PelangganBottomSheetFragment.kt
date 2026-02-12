package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetPelangganBinding

class PelangganBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPelangganBinding? = null
    private val binding get() = _binding!!

    private var onEditClickListener: ((String) -> Unit)? = null
    private var onIsolirClickListener: ((String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvPelangganName.text = arguments?.getString(ARG_PELANGGAN_NAME)

        binding.optionEditPelanggan.setOnClickListener {
            // Selalu ambil ID dari arguments untuk memastikan data tidak usang
            arguments?.getString(ARG_PELANGGAN_ID)?.let { id ->
                onEditClickListener?.invoke(id)
            }
            dismiss()
        }

        binding.optionUbahKeIsolir.setOnClickListener {
            // Selalu ambil ID dari arguments untuk memastikan data tidak usang
            arguments?.getString(ARG_PELANGGAN_ID)?.let { id ->
                onIsolirClickListener?.invoke(id)
            }
            dismiss()
        }
    }

    fun setOnEditClickListener(listener: (String) -> Unit) {
        onEditClickListener = listener
    }

    fun setOnIsolirClickListener(listener: (String) -> Unit) {
        onIsolirClickListener = listener
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