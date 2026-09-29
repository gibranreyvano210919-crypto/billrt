package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetCariCepatBinding
import java.io.Serializable

class CariCepatBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCariCepatBinding? = null
    private val binding get() = _binding!!

    private var pelanggan: PelangganData? = null
    private var onBayarMultiClick: ((PelangganData) -> Unit)? = null
    private var onHistoryClick: ((PelangganData) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pelanggan = arguments?.getSerializable(ARG_PELANGGAN) as? PelangganData
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCariCepatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pelanggan?.let { p ->
            binding.tvNamaPelanggan.text = "Nama: ${p.nama}"
            
            binding.btnBayarMulti.setOnClickListener {
                dismiss()
                onBayarMultiClick?.invoke(p)
            }

            binding.btnHistoryPembayaran.setOnClickListener {
                dismiss()
                onHistoryClick?.invoke(p)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PELANGGAN = "arg_pelanggan"

        fun newInstance(
            pelanggan: PelangganData,
            onBayarMultiClick: (PelangganData) -> Unit,
            onHistoryClick: (PelangganData) -> Unit
        ): CariCepatBottomSheetFragment {
            val fragment = CariCepatBottomSheetFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(ARG_PELANGGAN, pelanggan as Serializable)
            }
            fragment.onBayarMultiClick = onBayarMultiClick
            fragment.onHistoryClick = onHistoryClick
            return fragment
        }
    }
}
