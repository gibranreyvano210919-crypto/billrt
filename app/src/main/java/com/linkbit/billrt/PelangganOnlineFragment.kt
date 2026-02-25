package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PelangganOnlineAdapter
import com.linkbit.billrt.databinding.FragmentPelangganOnlineBinding
import com.linkbit.billrt.model.PppoeOnlineUser
import com.linkbit.billrt.viewmodel.PelangganOnlineViewModel

class PelangganOnlineFragment : BaseFragment() {

    private var _binding: FragmentPelangganOnlineBinding? = null
    private val binding get() = _binding!!

    private val args: PelangganOnlineFragmentArgs by navArgs()
    private val viewModel: PelangganOnlineViewModel by viewModels()
    private lateinit var adapter: PelangganOnlineAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganOnlineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        viewModel.fetchOnlineUsers(args.routerId)
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Pelanggan Online"
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = PelangganOnlineAdapter(emptyList()) { user ->
            showKickConfirmationDialog(user)
        }
        binding.rvPelangganOnline.layoutManager = LinearLayoutManager(context)
        binding.rvPelangganOnline.adapter = adapter
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
        viewModel.users.observe(viewLifecycleOwner) { users ->
            adapter.updateData(users)
            binding.tvEmpty.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                binding.tvEmpty.text = errorMessage
                binding.tvEmpty.visibility = View.VISIBLE
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.onToastShown()
            }
        }
    }

    private fun showKickConfirmationDialog(user: PppoeOnlineUser) {
        AlertDialog.Builder(requireContext())
            .setTitle("Kick User")
            .setMessage("Anda yakin ingin kick ${user.name}?")
            .setPositiveButton("Kick") { _, _ ->
                viewModel.kickUser(args.routerId, user.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
