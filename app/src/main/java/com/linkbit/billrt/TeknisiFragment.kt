package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.linkbit.billrt.databinding.FragmentTeknisiBinding

class TeknisiFragment : Fragment() {

    private var _binding: FragmentTeknisiBinding? = null
    private val binding get() = _binding!!

    private var pelanggan: PelangganData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pelanggan = it.getSerializable(ARG_PELANGGAN) as? PelangganData
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeknisiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PELANGGAN = "pelanggan"

        @JvmStatic
        fun newInstance(pelanggan: PelangganData) =
            TeknisiFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PELANGGAN, pelanggan)
                }
            }
    }
}
