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

    private var onEditClickListener: (() -> Unit)? = null
    private var onIsolirClickListener: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvPelangganName.text = arguments?.getString(ARG_PELANGGAN_NAME)

        binding.optionEditPelanggan.setOnClickListener {
            onEditClickListener?.invoke()
            dismiss()
        }

        binding.optionUbahKeIsolir.setOnClickListener {
            onIsolirClickListener?.invoke()
            dismiss()
        }
    }

    fun setOnEditClickListener(listener: () -> Unit) {
        onEditClickListener = listener
    }

    fun setOnIsolirClickListener(listener: () -> Unit) {
        onIsolirClickListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PELANGGAN_NAME = "pelanggan_name"

        fun newInstance(pelangganName: String): PelangganBottomSheetFragment {
            return PelangganBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PELANGGAN_NAME, pelangganName)
                }
            }
        }
    }
}