package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.linkbit.billrt.databinding.FragmentTagihanBinding

class TagihanFragment : Fragment() {

    private var _binding: FragmentTagihanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagihanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TagihanPagerAdapter(this)
        binding.viewPagerTagihan.adapter = adapter

        TabLayoutMediator(binding.tabLayoutTagihan, binding.viewPagerTagihan) { tab, position ->
            tab.text = when (position) {
                0 -> "Lunas"
                1 -> "Belum Bayar"
                else -> null
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
