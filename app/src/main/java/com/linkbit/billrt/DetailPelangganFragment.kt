package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.linkbit.billrt.databinding.FragmentDetailPelangganBinding

class DetailPelangganFragment : Fragment() {

    private var _binding: FragmentDetailPelangganBinding? = null
    private val binding get() = _binding!!

    private lateinit var pagerAdapter: DetailPelangganPagerAdapter
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
        _binding = FragmentDetailPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pelanggan?.let { 
            // Setup ViewPager
            pagerAdapter = DetailPelangganPagerAdapter(this, it)
            binding.viewPager.adapter = pagerAdapter

            // Setup TabLayout
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Detail"
                    1 -> "Bulan"
                    2 -> "Bayar"
                    3 -> "Teknisi"
                    else -> null
                }
            }.attach()

            // Setup Header
            binding.tvHeaderNama.text = it.namaPelanggan
            binding.tvHeaderId.text = "ID pel: ${it.idPelanggan}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PELANGGAN = "pelanggan"

        @JvmStatic
        fun newInstance(pelanggan: PelangganData) =
            DetailPelangganFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PELANGGAN, pelanggan)
                }
            }
    }
}
