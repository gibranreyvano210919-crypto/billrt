package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.linkbit.billrt.adapter.MikrotikBillingAdapter
import com.linkbit.billrt.databinding.FragmentMikrotikBillingBinding
import com.linkbit.billrt.model.MikrotikBillingItem
import com.linkbit.billrt.viewmodel.MikrotikBillingViewModel

class MikrotikBillingFragment : BaseFragment() {

    private var _binding: FragmentMikrotikBillingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MikrotikBillingViewModel by viewModels()
    private val args: MikrotikBillingFragmentArgs by navArgs()
    private lateinit var billingAdapter: MikrotikBillingAdapter

    private var currentSearch: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMikrotikBillingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets(binding.appBarLayout)
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.fetchBillingData(args.routerId)
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Billing Pelanggan"
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        billingAdapter = MikrotikBillingAdapter(
            emptyList(),
            onItemClick = { item ->
                val action = MikrotikBillingFragmentDirections.actionMikrotikBillingFragmentToDetailPelangganFragment(item.idPelanggan)
                findNavController().navigate(action)
            },
            onItemLongClick = { item ->
                showBottomSheet(item)
            }
        )
        binding.rvBilling.layoutManager = LinearLayoutManager(context)
        binding.rvBilling.adapter = billingAdapter
    }

    private fun showBottomSheet(item: MikrotikBillingItem) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_nonaktif, null)

        view.findViewById<TextView>(R.id.tv_customer_name).text = item.namaPelanggan
        view.findViewById<TextView>(R.id.tv_id_pelanggan).text = "ID: ${item.idPelanggan}"

        // Kita sembunyikan opsi aktifkan karena di sini biasanya pelanggan yang masih aktif/isolir
        // Opsi ini bisa disesuaikan jika perlu
        view.findViewById<TextView>(R.id.option_aktifkan_pelanggan).visibility = View.GONE

        view.findViewById<TextView>(R.id.option_hapus_pelanggan).setOnClickListener {
            dialog.dismiss()
            confirmHapusPelanggan(item)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun confirmHapusPelanggan(item: MikrotikBillingItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pelanggan")
            .setMessage("Apakah Anda yakin ingin menghapus data Mikrotik dan menonaktifkan pelanggan ${item.namaPelanggan}?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.hapusPelanggan(item.idPelanggan, args.routerId, currentSearch)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchBillingData(args.routerId, currentSearch)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearch = query
                viewModel.fetchBillingData(args.routerId, query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    currentSearch = null
                    viewModel.fetchBillingData(args.routerId, null)
                }
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.billingItems.observe(viewLifecycleOwner) { items ->
            billingAdapter.updateData(items)
            binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                binding.swipeRefresh.isRefreshing = false
                viewModel.clearMessages()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
