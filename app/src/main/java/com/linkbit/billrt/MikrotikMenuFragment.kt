package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view. View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.linkbit.billrt.databinding.FragmentMikrotikMenuBinding
import com.linkbit.billrt.model.MikrotikDashboardData
import com.linkbit.billrt.viewmodel.MikrotikMenuViewModel

class MikrotikMenuFragment : BaseFragment() {

    private var _binding: FragmentMikrotikMenuBinding? = null
    private val binding get() = _binding!!

    private val args: MikrotikMenuFragmentArgs by navArgs()
    private val viewModel: MikrotikMenuViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMikrotikMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Terapkan Insets pada AppBarLayout
        applyWindowInsets(binding.appBarLayout)
        
        setupToolbar()
        setupObservers()
        setupClickListeners()

        viewModel.fetchDashboardData(args.routerId)
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Menu Router"
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                binding.scrollView.visibility = View.GONE
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                binding.tvErrorMessage.text = errorMessage
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.scrollView.visibility = View.GONE
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            } else {
                binding.tvErrorMessage.visibility = View.GONE
            }
        }

        viewModel.dashboardData.observe(viewLifecycleOwner) { response ->
            response?.data?.let {
                updateUi(it)
                binding.scrollView.visibility = View.VISIBLE
            }
        }
    }

    private fun setupClickListeners() {
        binding.cardStatusOnline.setOnClickListener {
            val action = MikrotikMenuFragmentDirections.actionMikrotikMenuFragmentToPelangganOnlineFragment(args.routerId)
            findNavController().navigate(action)
        }

        binding.cardStatusOffline.setOnClickListener {
            val action = MikrotikMenuFragmentDirections.actionMikrotikMenuFragmentToPelangganOfflineFragment(args.routerId)
            findNavController().navigate(action)
        }

        binding.cardDaftarSecret.setOnClickListener {
            val action = MikrotikMenuFragmentDirections.actionMikrotikMenuFragmentToDaftarSecretFragment(args.routerId)
            findNavController().navigate(action)
        }

        binding.btnCekPppoe.setOnClickListener {
            val action = MikrotikMenuFragmentDirections.actionMikrotikMenuFragmentToAuditUserGlobalFragment()
            findNavController().navigate(action)
        }

        binding.btnSyncMac.setOnClickListener {
            val action = MikrotikMenuFragmentDirections.actionMikrotikMenuFragmentToSyncMikrotikFragment()
            findNavController().navigate(action)
        }
    }

    private fun updateUi(data: MikrotikDashboardData) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = data.header.identity
        binding.tvBoardName.text = "Board Name: ${data.header.boardName}"
        binding.tvIdentity.text = "Identity: ${data.header.identity}"
        binding.tvVersion.text = "Version: ${data.header.version}"
        binding.tvUptime.text = "Uptime: ${data.header.uptime}"
        binding.tvCpuLoad.text = "CPU Load: ${data.header.cpuLoad}"
        binding.tvFreeMem.text = "Free Memory: ${data.header.freeMem}"

        binding.tvTotalSecret.text = data.pppoe.totalSecret.toString()
        binding.tvTotalActive.text = data.pppoe.totalActive.toString()
        binding.tvNonActive.text = data.pppoe.nonActive.toString()

        binding.tvTotalQueue.text = "Total Queue: ${data.system.totalQueue}"
        binding.tvTotalArp.text = "Total ARP: ${data.system.totalArp}"
        binding.tvQueueNonaktif.text = "Queue Non-aktif: ${data.system.queueNonaktif}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
