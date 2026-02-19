package com.linkbit.billrt

import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.linkbit.billrt.databinding.BottomSheetCustomerMenuBinding
import com.linkbit.billrt.model.PelangganIdentifiable

abstract class BasePelangganFragment : Fragment() {

    fun showCustomerMenu(pelanggan: PelangganIdentifiable) {
        val dialog = BottomSheetDialog(requireContext())
        val binding = BottomSheetCustomerMenuBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.tvCustomerDetail.setOnClickListener {
            dialog.dismiss()
            val bottomSheet = CustomerDetailBottomSheetFragment.newInstance(pelanggan.id_pelanggan)
            bottomSheet.show(parentFragmentManager, CustomerDetailBottomSheetFragment.TAG)
        }

        binding.tvHistoryPayment.setOnClickListener {
            dialog.dismiss()
            val historySheet = HistoryPembayaranBottomSheetFragment.newInstance(pelanggan.id_pelanggan)
            historySheet.show(parentFragmentManager, HistoryPembayaranBottomSheetFragment.TAG)
        }

        dialog.show()
    }
}
