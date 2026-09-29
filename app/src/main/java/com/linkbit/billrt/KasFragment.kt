package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.linkbit.billrt.databinding.FragmentKasBinding

class KasFragment : BaseFragment() {

    private var _binding: FragmentKasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        applyWindowInsets(binding.appBarLayout)
        setupToolbar()

        val adapter = KasViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPagerKas.adapter = adapter

        TabLayoutMediator(binding.tabLayoutKas, binding.viewPagerKas) { tab, position ->
            tab.text = when (position) {
                0 -> "Input Kas"
                1 -> "Riwayat Kas"
                else -> null
            }
        }.attach()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Kas"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
