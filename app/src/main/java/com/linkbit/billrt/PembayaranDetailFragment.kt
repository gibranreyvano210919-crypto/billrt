package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.linkbit.billrt.databinding.FragmentPembayaranDetailBinding
import java.io.Serializable

class PembayaranDetailFragment : Fragment() {

    private var _binding: FragmentPembayaranDetailBinding? = null
    private val binding get() = _binding!!

    private var tagihan: TagihanData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tagihan = it.getSerializable(ARG_TAGIHAN) as? TagihanData
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPembayaranDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tagihan?.let {
            // The following lines are causing errors and need to be fixed
            // based on the new data structure in ApiData.kt

            // binding.tvPaymentUserInfo.text = "..."
            // binding.tvPaymentPaketName.text = it.nama_paket
            // ... and so on for all other UI elements
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TAGIHAN = "tagihan"

        @JvmStatic
        fun newInstance(tagihan: TagihanData) =
            PembayaranDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TAGIHAN, tagihan as Serializable)
                }
            }
    }
}
