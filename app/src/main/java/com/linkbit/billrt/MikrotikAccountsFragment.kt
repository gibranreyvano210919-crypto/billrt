package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.MikrotikAccountsAdapter
import com.linkbit.billrt.databinding.FragmentMikrotikAccountsBinding
import com.linkbit.billrt.viewmodel.MikrotikAccountsViewModel

class MikrotikAccountsFragment : BaseFragment() {

    private var _binding: FragmentMikrotikAccountsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MikrotikAccountsViewModel by viewModels()
    private lateinit var accountsAdapter: MikrotikAccountsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMikrotikAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        observeViewModel()

        viewModel.fetchAccounts()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        accountsAdapter = MikrotikAccountsAdapter(emptyList()) { account ->
            val action = MikrotikAccountsFragmentDirections.actionMikrotikAccountsFragmentToMikrotikMenuFragment(account.id)
            findNavController().navigate(action)
        }
        binding.rvMikrotikAccounts.layoutManager = LinearLayoutManager(context)
        binding.rvMikrotikAccounts.adapter = accountsAdapter
    }

    private fun observeViewModel() {
        viewModel.accounts.observe(viewLifecycleOwner) { accounts ->
            accountsAdapter.updateData(accounts)
            binding.tvEmpty.visibility = if (accounts.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                binding.tvEmpty.text = message
                binding.tvEmpty.visibility = View.VISIBLE
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
