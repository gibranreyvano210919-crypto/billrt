package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.AuditUserAdapter
import com.linkbit.billrt.databinding.FragmentAuditUserBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuditUserFragment : BaseFragment() {

    private var _binding: FragmentAuditUserBinding? = null
    private val binding get() = _binding!!

    private val args: AuditUserFragmentArgs by navArgs()
    private lateinit var adapter: AuditUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuditUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AuditUserAdapter(emptyList())
        binding.rvAuditResults.layoutManager = LinearLayoutManager(context)
        binding.rvAuditResults.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchData()
        }

        fetchData()
    }

    private fun fetchData() {
        binding.swipeRefreshLayout.isRefreshing = true

        ApiConfig.apiService.auditUser(routerId = args.routerId).enqueue(object : Callback<AuditUserResponse> {
            override fun onResponse(call: Call<AuditUserResponse>, response: Response<AuditUserResponse>) {
                if (!isAdded || _binding == null) return
                binding.swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful && response.body()?.status == true) {
                    val body = response.body()
                    val summary = body?.summary
                    val data = body?.data ?: emptyList()

                    binding.tvTotalIssue.text = "Total Isu: ${summary?.totalAuditIssue ?: 0}"

                    adapter.updateData(data)

                    if (data.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                    }
                } else {
                    handleFailure("Gagal memuat data: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<AuditUserResponse>, t: Throwable) {
                handleFailure("Error: ${t.message}")
            }
        })
    }

    private fun handleFailure(message: String) {
        if (!isAdded || _binding == null) return
        binding.swipeRefreshLayout.isRefreshing = false
        binding.tvEmpty.text = message
        binding.tvEmpty.visibility = View.VISIBLE
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
