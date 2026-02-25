package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.linkbit.billrt.adapter.DaftarSecretAdapter
import com.linkbit.billrt.databinding.FragmentDaftarSecretBinding
import com.linkbit.billrt.viewmodel.DaftarSecretViewModel

class DaftarSecretFragment : BaseFragment() {

    private var _binding: FragmentDaftarSecretBinding? = null
    private val binding get() = _binding!!

    private val args: DaftarSecretFragmentArgs by navArgs()
    private val viewModel: DaftarSecretViewModel by viewModels()
    private lateinit var adapter: DaftarSecretAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDaftarSecretBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupBottomSheet()
        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        viewModel.fetchSecrets(args.routerId)
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Daftar Secret"
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetTambahSecret)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED // Keep it visible

        binding.btnTambah.setOnClickListener {
            val action = DaftarSecretFragmentDirections.actionDaftarSecretFragmentToTambahPppoeFragment(args.routerId)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        adapter = DaftarSecretAdapter(emptyList()) { secret, isEnabled ->
            viewModel.setSecretStatus(args.routerId, secret, isEnabled)
        }
        binding.rvSecrets.layoutManager = LinearLayoutManager(context)
        binding.rvSecrets.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText)
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.secrets.observe(viewLifecycleOwner) { secrets ->
            adapter.updateData(secrets)
            binding.tvEmpty.visibility = if (secrets.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                binding.tvEmpty.text = errorMessage
                binding.tvEmpty.visibility = View.VISIBLE
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.onToastShown()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
