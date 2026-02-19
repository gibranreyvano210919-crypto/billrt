package com.linkbit.billrt

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetCustomerMenuBinding

class CustomerMenuBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCustomerMenuBinding? = null
    private val binding get() = _binding!!

    private var customerId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            customerId = it.getInt(ARG_CUSTOMER_ID)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCustomerMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCustomerDetail.setOnClickListener {
            // Handle "Customer Detail" click
            val detailFragment = CustomerDetailBottomSheetFragment.newInstance(customerId)
            detailFragment.show(parentFragmentManager, CustomerDetailBottomSheetFragment.TAG)
            dismiss()
        }

        binding.tvHistoryPayment.setOnClickListener {
            val historyFragment = HistoryPembayaranBottomSheetFragment.newInstance(customerId)
            historyFragment.show(parentFragmentManager, HistoryPembayaranBottomSheetFragment.TAG)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CustomerMenuBottomSheetFragment"
        private const val ARG_CUSTOMER_ID = "customer_id"

        fun newInstance(customerId: Int): CustomerMenuBottomSheetFragment {
            val fragment = CustomerMenuBottomSheetFragment()
            val args = Bundle()
            args.putInt(ARG_CUSTOMER_ID, customerId)
            fragment.arguments = args
            return fragment
        }
    }
}
