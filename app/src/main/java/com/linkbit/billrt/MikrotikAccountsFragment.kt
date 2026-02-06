package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.MikrotikAccountsAdapter
import com.linkbit.billrt.databinding.FragmentMikrotikAccountsBinding
import com.linkbit.billrt.MikrotikAccount
import com.linkbit.billrt.MikrotikAccountsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MikrotikAccountsFragment : BaseFragment() {

    private var _binding: FragmentMikrotikAccountsBinding? = null
    private val binding get() = _binding!!

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
        setupRecyclerView()
        setupSearchView()
        fetchAccounts()
    }

    private fun setupRecyclerView() {
        accountsAdapter = MikrotikAccountsAdapter(emptyList()) { account ->
            // KEMBALIKAN KE ALUR SEMULA: Navigasi ke MikrotikMenuFragment
            val action = MikrotikAccountsFragmentDirections.actionMikrotikAccountsFragmentToMikrotikMenuFragment(account.id)
            findNavController().navigate(action)
        }
        binding.rvMikrotikAccounts.layoutManager = LinearLayoutManager(context)
        binding.rvMikrotikAccounts.adapter = accountsAdapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                accountsAdapter.filter(newText)
                return true
            }
        })
    }

    private fun fetchAccounts() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        ApiConfig.apiService.getMikrotikAccounts().enqueue(object : Callback<MikrotikAccountsResponse> {
            override fun onResponse(call: Call<MikrotikAccountsResponse>, response: Response<MikrotikAccountsResponse>) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == true) {
                    val accounts = response.body()?.data ?: emptyList()
                    accountsAdapter.updateData(accounts)
                    if (accounts.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Gagal memuat data"
                    binding.tvEmpty.text = "Gagal: $errorMessage"
                    binding.tvEmpty.visibility = View.VISIBLE
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MikrotikAccountsResponse>, t: Throwable) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                binding.tvEmpty.text = "Error: ${t.message}"
                binding.tvEmpty.visibility = View.VISIBLE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
