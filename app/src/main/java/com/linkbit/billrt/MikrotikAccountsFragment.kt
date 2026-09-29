package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.MikrotikAccountsAdapter
import com.linkbit.billrt.databinding.FragmentMikrotikAccountsBinding
import com.linkbit.billrt.model.MikrotikAccount
import com.linkbit.billrt.viewmodel.MikrotikAccountsViewModel

class MikrotikAccountsFragment : BaseFragment() {

    private var _binding: FragmentMikrotikAccountsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MikrotikAccountsViewModel by viewModels()
    private lateinit var accountsAdapter: MikrotikAccountsAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMikrotikAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        applyWindowInsets(binding.appBarLayout)
        
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.setDefaultRouterId(sessionManager.getDefaultRouterId())
        viewModel.fetchAccounts()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Akun MikroTik"
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        accountsAdapter = MikrotikAccountsAdapter(
            emptyList(),
            onClick = { account ->
                val action = MikrotikAccountsFragmentDirections.actionMikrotikAccountsFragmentToMikrotikMenuFragment(account.id)
                findNavController().navigate(action)
            },
            onLongClick = { account ->
                val actionSheet = MikrotikAccountActionBottomSheet.newInstance(account)
                actionSheet.show(childFragmentManager, MikrotikAccountActionBottomSheet.TAG)
            }
        )
        binding.rvMikrotikAccounts.layoutManager = LinearLayoutManager(context)
        binding.rvMikrotikAccounts.adapter = accountsAdapter
    }

    private fun setupListeners() {
        binding.btnAddRouter.setOnClickListener {
            val action = MikrotikAccountsFragmentDirections.actionMikrotikAccountsFragmentToMikrotikAddAccountFragment()
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewModel.accounts.observe(viewLifecycleOwner) { accounts ->
            accountsAdapter.updateData(accounts)
            binding.tvEmpty.visibility = if (accounts.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.defaultRouterId.observe(viewLifecycleOwner) { id ->
            accountsAdapter.updateDefaultRouter(id)
            sessionManager.saveDefaultRouterId(id)
        }

        viewModel.editStatus.observe(viewLifecycleOwner) { response ->
            response?.let {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                viewModel.resetEditStatus()
            }
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
