package com.linkbit.billrt

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetLunasBinding

class LunasBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetLunasBinding? = null
    private val binding get() = _binding!!

    var itemClickListener: ItemClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetLunasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initial setup
        binding.layoutActions.visibility = View.VISIBLE
        binding.layoutConfirmation.visibility = View.GONE

        // Show confirmation view
        binding.actionBatalkanPembayaran.setOnClickListener {
            binding.layoutActions.visibility = View.GONE
            binding.layoutConfirmation.visibility = View.VISIBLE
        }

        // Hide confirmation view
        binding.btnCancel.setOnClickListener {
            binding.layoutConfirmation.visibility = View.GONE
            binding.layoutActions.visibility = View.VISIBLE
        }

        // Confirm cancellation and notify listener
        binding.btnConfirmCancel.setOnClickListener {
            itemClickListener?.onItemClick("batalkan")
            dismiss() // Dismiss after confirmation
        }

        // Handle other actions
        binding.actionCetakPembayaran.setOnClickListener {
            itemClickListener?.onItemClick("cetak")
            dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is ItemClickListener) {
            itemClickListener = parentFragment as ItemClickListener
        }
    }

    override fun onDetach() {
        super.onDetach()
        itemClickListener = null
    }

    interface ItemClickListener {
        fun onItemClick(item: String)
    }

    companion object {
        const val TAG = "LunasBottomSheet"
        fun newInstance(): LunasBottomSheetFragment {
            return LunasBottomSheetFragment()
        }
    }
}
