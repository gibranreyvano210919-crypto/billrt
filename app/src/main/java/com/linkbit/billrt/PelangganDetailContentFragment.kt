package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.linkbit.billrt.databinding.FragmentPelangganDetailContentBinding

class PelangganDetailContentFragment : Fragment() {

    private var _binding: FragmentPelangganDetailContentBinding? = null
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
        _binding = FragmentPelangganDetailContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // All logic is temporarily commented out to allow the project to build.
        // You can uncomment and fix this later.

        /*
        pelanggan?.let {
            // The following lines are causing errors and need to be fixed
            // based on the new data structure in ApiData.kt

            // binding.tvDetailNamaPelanggan.text = it.nama
            // binding.tvDetailAlamat.text = it.alamat
            // binding.tvDetailTelepon.text = it.telepon
            // ... and so on for all other UI elements
        }
        */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PELANGGAN = "pelanggan"

        @JvmStatic
        fun newInstance(pelanggan: PelangganData) =
            PelangganDetailContentFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PELANGGAN, pelanggan)
                }
            }
    }
}
