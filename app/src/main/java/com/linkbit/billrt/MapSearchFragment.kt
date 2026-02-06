package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.linkbit.billrt.databinding.FragmentMapSearchBinding

class MapSearchFragment : BaseFragment() {

    private var _binding: FragmentMapSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Initialize map and search functionality here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}