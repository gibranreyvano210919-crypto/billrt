package com.linkbit.billrt

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.CustomerAdapter
import com.linkbit.billrt.databinding.FragmentCustomerBinding
import com.linkbit.billrt.model.Customer
import com.linkbit.billrt.viewmodel.CustomerViewModel

class CustomerFragment : Fragment() {

    private var _binding: FragmentCustomerBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CustomerViewModel
    private lateinit var customerAdapter: CustomerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(CustomerViewModel::class.java)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefreshLayout()
        observeViewModel()

        viewModel.fetchNewCustomers(null)
    }

    private fun setupSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchNewCustomers(null)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.search_menu)
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(Color.WHITE)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Menyembunyikan keyboard saat submit
                searchView.clearFocus()
                viewModel.fetchNewCustomers(query?.takeIf { it.isNotBlank() })
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Melakukan pencarian setiap kali teks berubah
                viewModel.fetchNewCustomers(newText?.takeIf { it.isNotBlank() })
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        customerAdapter = CustomerAdapter(emptyList()) { customer ->
            showCustomerMenu(customer)
        }
        binding.rvCustomers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = customerAdapter
        }
    }

    private fun showCustomerMenu(customer: Customer) {
        val bottomSheet = CustomerMenuBottomSheetFragment.newInstance(customer.id)
        bottomSheet.show(childFragmentManager, CustomerMenuBottomSheetFragment.TAG)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            if (!isLoading) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.customers.observe(viewLifecycleOwner) { customers ->
            customers?.let { 
                customerAdapter.updateData(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}