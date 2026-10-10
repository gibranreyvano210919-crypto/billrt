package com.linkbit.billrt

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.SyncMikrotikAdapter
import com.linkbit.billrt.databinding.FragmentSyncMikrotikBinding
import com.linkbit.billrt.viewmodel.SyncMikrotikViewModel

class SyncMikrotikFragment : BaseFragment() {

    private var _binding: FragmentSyncMikrotikBinding? = null
    private val binding get() = _binding!!

    private val args: SyncMikrotikFragmentArgs by navArgs()
    private val viewModel: SyncMikrotikViewModel by viewModels()
    private lateinit var adapter: SyncMikrotikAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncMikrotikBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets(binding.appBarLayout)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupListeners()
        setupObservers()
        setupBackPressedHandler()

        viewModel.fetchCustomerList(args.routerId)
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Sync MikroTik"
        }
        binding.toolbar.setNavigationOnClickListener {
            if (viewModel.isBatchSyncing.value == true) {
                Toast.makeText(context, "Proses sync sedang berlangsung, mohon tunggu...", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupBackPressedHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.isBatchSyncing.value == true) {
                    Toast.makeText(context, "Proses sync sedang berlangsung, mohon tunggu...", Toast.LENGTH_SHORT).show()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = SyncMikrotikAdapter { item ->
            viewModel.syncSingle(item)
        }
        binding.rvPelangganSync.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganSync.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchCustomerList()
        }

        binding.btnSyncAll.setOnClickListener {
            viewModel.syncAll()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            if (isLoading && adapter.itemCount == 0) {
                binding.progressBarMain.visibility = View.VISIBLE
            } else {
                binding.progressBarMain.visibility = View.GONE
            }
        }

        viewModel.filteredList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(ArrayList(list))

            val totalCount = list.size
            val validCount = list.count { !it.mikrotikUsername.isNullOrEmpty() && it.mikrotikUsername != "-" }
            binding.tvSummaryInfo.text = "Total: $totalCount Pelanggan | Username Valid: $validCount"

            binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isBatchSyncing.observe(viewLifecycleOwner) { isBatch ->
            binding.btnSyncAll.isEnabled = !isBatch
            binding.swipeRefresh.isEnabled = !isBatch
            binding.layoutLoadingOverlay.visibility = if (isBatch) View.VISIBLE else View.GONE
            adapter.setBatchSyncing(isBatch)
        }

        viewModel.batchProgressTotal.observe(viewLifecycleOwner) { total ->
            binding.progressOverlayBar.max = total
        }

        viewModel.batchProgressCurrent.observe(viewLifecycleOwner) { current ->
            binding.progressOverlayBar.progress = current

            val total = viewModel.batchProgressTotal.value ?: 100
            if (total > 0) {
                val percent = (current * 100) / total
                binding.tvOverlayPercent.text = "$percent%"
            }

            if (viewModel.isBatchSyncing.value == true && current > 0) {
                val targetPos = current - 1
                if (targetPos < adapter.itemCount) {
                    binding.rvPelangganSync.smoothScrollToPosition(targetPos)
                }
            }
        }

        viewModel.batchProgressText.observe(viewLifecycleOwner) { text ->
            if (!text.isNullOrEmpty()) {
                binding.tvOverlayProgressText.text = text
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearToastMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
