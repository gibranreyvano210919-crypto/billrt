package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.linkbit.billrt.databinding.FragmentMikrotikMenuBinding

class MikrotikMenuFragment : BaseFragment() {

    private var _binding: FragmentMikrotikMenuBinding? = null
    private val binding get() = _binding!!

    private val args: MikrotikMenuFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMikrotikMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardStatusOnline.setOnClickListener {
            val action = MikrotikMenuFragmentDirections.actionMikrotikMenuFragmentToPelangganOnlineFragment(args.routerId)
            findNavController().navigate(action)
        }

        binding.cardStatusOffline.setOnClickListener {
            val action = MikrotikMenuFragmentDirections.actionMikrotikMenuFragmentToPelangganOfflineFragment(args.routerId)
            findNavController().navigate(action)
        }

        binding.cardAuditUser.setOnClickListener {
            val action = MikrotikMenuFragmentDirections.actionMikrotikMenuFragmentToAuditUserFragment(args.routerId)
            findNavController().navigate(action)
        }

        binding.cardTambahPppoe.setOnClickListener {
            val action = MikrotikMenuFragmentDirections.actionMikrotikMenuFragmentToTambahPppoeFragment(args.routerId)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
