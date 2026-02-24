package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetBayarBinding
import com.linkbit.billrt.model.PelangganBelumBayarItem

class BayarBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetBayarBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetBayarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item = arguments?.getSerializable("pelanggan_item") as? PelangganBelumBayarItem

        if (item == null) {
            Toast.makeText(context, "Error: Data pelanggan tidak ditemukan", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        binding.tvNamaPelanggan.text = item.namaPelanggan

        binding.btnIngatkan.setOnClickListener {
            Toast.makeText(context, "Ingatkan pembayaran untuk: ${item.namaPelanggan}", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        binding.btnCetak.setOnClickListener {
            Toast.makeText(context, "Cetak invoice: ${item.invoice}", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        binding.btnBayar.setOnClickListener {
            val konfirmasiSheet = KonfirmasiBayarBottomSheetFragment.newInstance(item)
            konfirmasiSheet.show(parentFragmentManager, "KonfirmasiBayarBottomSheet")
            dismiss()
        }

        binding.btnIsolir.setOnClickListener {
            Toast.makeText(context, "Isolir pelanggan: ${item.namaPelanggan}", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        binding.btnTagout.setOnClickListener {
            val tagoutSheet = KonfirmasiTagoutBottomSheetFragment.newInstance(item)
            tagoutSheet.show(parentFragmentManager, "KonfirmasiTagoutBottomSheet")
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(item: PelangganBelumBayarItem): BayarBottomSheetFragment {
            val fragment = BayarBottomSheetFragment()
            val args = Bundle()
            args.putSerializable("pelanggan_item", item)
            fragment.arguments = args
            return fragment
        }
    }
}
