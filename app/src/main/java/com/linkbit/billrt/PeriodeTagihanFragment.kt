package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.adapter.PeriodeTagihanAdapter
import com.linkbit.billrt.databinding.FragmentPeriodeTagihanBinding
import com.linkbit.billrt.viewmodel.PeriodeTagihanViewModel

class PeriodeTagihanFragment : Fragment() {

    private var _binding: FragmentPeriodeTagihanBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PeriodeTagihanViewModel
    private lateinit var periodeAdapter: PeriodeTagihanAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPeriodeTagihanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PeriodeTagihanViewModel::class.java)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()

        viewModel.fetchPeriodeTagihan()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() {
        periodeAdapter = PeriodeTagihanAdapter { periode ->
            val bulanInt = convertBulanStringToInt(periode.bulanTagihan)
            val bundle = bundleOf(
                "bulan" to bulanInt,
                "tahun" to periode.tahunTagihan
            )
            findNavController().navigate(R.id.action_periodeTagihanFragment_to_dashboardTagihanFragment, bundle)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = periodeAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.periodeList.observe(viewLifecycleOwner) { periodeList ->
            periodeAdapter.submitList(periodeList)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }

        viewModel.generateResult.observe(viewLifecycleOwner) { result ->
            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun convertBulanStringToInt(bulan: String): Int {
        return when (bulan) {
            "Januari" -> 1
            "Februari" -> 2
            "Maret" -> 3
            "April" -> 4
            "Mei" -> 5
            "Juni" -> 6
            "Juli" -> 7
            "Agustus" -> 8
            "September" -> 9
            "Oktober" -> 10
            "November" -> 11
            "Desember" -> 12
            else -> 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
