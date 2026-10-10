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
import com.linkbit.billrt.adapter.AuditRouterAdapter
import com.linkbit.billrt.databinding.FragmentAuditUserGlobalBinding
import com.linkbit.billrt.viewmodel.MikrotikMenuViewModel

class AuditUserGlobalFragment : BaseFragment() {

    private var _binding: FragmentAuditUserGlobalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MikrotikMenuViewModel by viewModels()
    private lateinit var adapter: AuditRouterAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuditUserGlobalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets(binding.appBarLayout)
        
        setupToolbar()
        setupRecyclerView()
        observeViewModel()

        viewModel.auditUserGlobal()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Audit User PPPoE Global"
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = AuditRouterAdapter(emptyList())
        binding.rvAudit.layoutManager = LinearLayoutManager(context)
        binding.rvAudit.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.auditResult.observe(viewLifecycleOwner) { response ->
            if (response?.status == true) {
                adapter.updateData(response.data)
                binding.tvEmpty.visibility = if (response.data.isEmpty()) View.VISIBLE else View.GONE
            } else {
                binding.tvEmpty.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
