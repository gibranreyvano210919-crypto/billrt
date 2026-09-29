package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentMikrotikAddAccountBinding
import com.linkbit.billrt.viewmodel.MikrotikAccountsViewModel

class MikrotikAddAccountFragment : BaseFragment() {

    private var _binding: FragmentMikrotikAddAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MikrotikAccountsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMikrotikAddAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        applyWindowInsets(binding.toolbar)
        setupToolbar()
        observeViewModel()

        binding.btnSave.setOnClickListener {
            saveAccount()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.editStatus.observe(viewLifecycleOwner) { response ->
            response?.let {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                if (it.status) {
                    viewModel.resetEditStatus()
                    findNavController().navigateUp()
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAccount() {
        val routerName = binding.etRouterName.text.toString().trim()
        val ipAddress = binding.etIpAddress.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val port = binding.etPort.text.toString().toIntOrNull() ?: 8728
        val ownerId = binding.etOwnerId.text.toString().toIntOrNull() ?: 0

        if (routerName.isEmpty() || ipAddress.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.addAccount(routerName, ipAddress, username, password, port, ownerId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
