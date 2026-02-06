package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.linkbit.billrt.databinding.FragmentTagihanContainerBinding

class TagihanContainerFragment : Fragment() {

    private var _binding: FragmentTagihanContainerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagihanContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = TagihanPagerAdapter(this)
        binding.viewPagerTagihan.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayoutTagihan, binding.viewPagerTagihan) { tab, position ->
            tab.text = when (position) {
                0 -> "Belum Bayar"
                1 -> "Sudah Bayar"
                else -> "Semua Tagihan"
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}