package com.linkbit.billrt

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetPelangganTelatBinding
import com.linkbit.billrt.model.PelangganTelatItem

class PelangganTelatBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPelangganTelatBinding? = null
    private val binding get() = _binding!!

    private var pelanggan: PelangganTelatItem? = null

    companion object {
        fun newInstance(pelanggan: PelangganTelatItem): PelangganTelatBottomSheetFragment {
            val fragment = PelangganTelatBottomSheetFragment()
            fragment.pelanggan = pelanggan
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPelangganTelatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pelanggan?.let { data ->
            binding.tvNamaPelanggan.text = data.namaPelanggan

            binding.btnDetailTagihan.setOnClickListener {
                val bundle = Bundle().apply {
                    putInt("id_pelanggan", data.idPelanggan)
                }
                findNavController().navigate(R.id.action_pelangganTelatFragment_to_detailTagihanFragment, bundle)
                dismiss()
            }

            binding.btnBayarTagihan.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("pelangganId", data.idPelanggan.toString())
                }
                findNavController().navigate(R.id.action_global_bayarTagihanFragment, bundle)
                dismiss()
            }

            binding.btnHistoryPembayaran.setOnClickListener {
                val historySheet = HistoryPembayaranBottomSheetFragment.newInstance(data.idPelanggan)
                historySheet.show(parentFragmentManager, HistoryPembayaranBottomSheetFragment.TAG)
                dismiss()
            }

            binding.btnWhatsapp.setOnClickListener {
                data.telepon?.let { tel ->
                    val phoneNumber = if (tel.startsWith("0")) "62" + tel.substring(1) else tel
                    val url = "https://api.whatsapp.com/send?phone=$phoneNumber"
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                    dismiss()
                } ?: run {
                    Toast.makeText(context, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
