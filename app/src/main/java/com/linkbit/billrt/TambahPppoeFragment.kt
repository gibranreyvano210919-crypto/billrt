package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.linkbit.billrt.databinding.FragmentTambahPppoeBinding
import com.linkbit.billrt.viewmodel.TambahPppoeViewModel

class TambahPppoeFragment : BaseFragment() {

    private var _binding: FragmentTambahPppoeBinding? = null
    private val binding get() = _binding!!

    private val args: TambahPppoeFragmentArgs by navArgs()
    private val viewModel: TambahPppoeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahPppoeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets(binding.toolbar)
        setupToolbar()
        setupClickListeners()
        observeViewModel()

        viewModel.fetchInitialData(args.routerId)
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupClickListeners() {
        binding.btnSimpan.setOnClickListener {
            val user = binding.etNamaUser.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            val profile = binding.actProfile.text.toString().trim()
            val localIp = binding.etLocalAddress.text.toString().trim().takeIf { it.isNotEmpty() } // Diubah ke etLocalAddress
            val remoteIp = binding.actRemoteIp.text.toString().trim().takeIf { it.isNotEmpty() }
            val comment = binding.etComment.text.toString().trim()

            if (user.isNotEmpty() && pass.isNotEmpty() && profile.isNotEmpty()) {
                viewModel.addPppoeSecret(
                    routerId = args.routerId,
                    user = user,
                    pass = pass,
                    profile = profile,
                    localIp = localIp,
                    remoteIp = remoteIp,
                    comment = comment
                )
            } else {
                Toast.makeText(context, "Nama, Password, dan Profil harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.profiles.observe(viewLifecycleOwner) { profiles ->
            val profileNames = profiles.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, profileNames)
            binding.actProfile.setAdapter(adapter)
        }

        viewModel.availableIps.observe(viewLifecycleOwner) { ips ->
            val ipAddresses = ips.map { it.ip }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ipAddresses)
            // PERBAIKAN: Hanya set adapter untuk Remote IP
            binding.actRemoteIp.setAdapter(adapter)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSimpan.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.addPppoeResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "PPPoe berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
