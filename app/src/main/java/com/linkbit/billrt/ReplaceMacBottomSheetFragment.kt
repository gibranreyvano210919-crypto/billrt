package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetReplaceMacBinding

class ReplaceMacBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetReplaceMacBinding? = null
    private val binding get() = _binding!!

    private var onSave: ((String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetReplaceMacBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentMac = arguments?.getString(ARG_CURRENT_MAC)
        binding.etMacAddress.setText(currentMac)

        binding.btnSaveMac.setOnClickListener {
            val newMac = binding.etMacAddress.text.toString()
            onSave?.invoke(newMac)
            dismiss()
        }
    }

    fun setOnSaveListener(listener: (String) -> Unit) {
        onSave = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CURRENT_MAC = "current_mac"

        fun newInstance(currentMac: String?): ReplaceMacBottomSheetFragment {
            val fragment = ReplaceMacBottomSheetFragment()
            val args = Bundle()
            args.putString(ARG_CURRENT_MAC, currentMac)
            fragment.arguments = args
            return fragment
        }
    }
}
