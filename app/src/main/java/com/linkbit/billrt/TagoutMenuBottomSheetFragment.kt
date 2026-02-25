package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetTagoutMenuBinding
import com.linkbit.billrt.model.PelangganBelumBayarItem
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class TagoutMenuBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetTagoutMenuBinding? = null
    private val binding get() = _binding!!

    private var pelangganItem: PelangganBelumBayarItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pelangganItem = arguments?.getSerializable(ARG_PELANGGAN) as? PelangganBelumBayarItem
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetTagoutMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvBatalTagout.setOnClickListener {
            pelangganItem?.invoice?.let { invoiceId ->
                batalTagout(invoiceId)
            } ?: run {
                Toast.makeText(requireContext(), "ID Tagihan tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvKonfirmasiBayar.setOnClickListener {
            pelangganItem?.let {
                val konfirmasiSheet = KonfirmasiBayarBottomSheetFragment.newInstance(it)
                konfirmasiSheet.show(parentFragmentManager, "KonfirmasiBayarBottomSheet")
            }
            dismiss()
        }
    }

    private fun batalTagout(idTagihan: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.tagihanApiService.batalTagout(idTagihan)
                if (response.status) {
                    Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                    // Mengirim sinyal refresh ke fragment (menggunakan kunci yang sama dengan payment_successful agar fragment merefresh data)
                    setFragmentResult("payment_successful", bundleOf("refresh" to true))
                    dismiss()
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TagoutMenuBottomSheetFragment"
        private const val ARG_PELANGGAN = "arg_pelanggan"

        fun newInstance(pelangganItem: PelangganBelumBayarItem): TagoutMenuBottomSheetFragment {
            val fragment = TagoutMenuBottomSheetFragment()
            val args = Bundle()
            args.putSerializable(ARG_PELANGGAN, pelangganItem)
            fragment.arguments = args
            return fragment
        }
    }
}
