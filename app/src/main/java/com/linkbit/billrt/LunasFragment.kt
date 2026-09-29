package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.linkbit.billrt.adapter.LunasAdapter
import com.linkbit.billrt.databinding.FragmentLunasBinding
import com.linkbit.billrt.model.PelangganLunasItem
import com.linkbit.billrt.viewmodel.LunasViewModel

class LunasFragment : BaseFragment(), LunasBottomSheetFragment.ItemClickListener {

    private var _binding: FragmentLunasBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LunasViewModel
    private lateinit var lunasAdapter: LunasAdapter

    private var bulan: Int = 0
    private var tahun: Int = 0

    private var selectedPelanggan: PelangganLunasItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bulan = it.getInt("bulan")
            tahun = it.getInt("tahun")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLunasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sinkronisasi Insets agar toolbar tidak menabrak status bar
        applyWindowInsets(binding.appBarLayout)

        viewModel = ViewModelProvider(this).get(LunasViewModel::class.java)

        setupRecyclerView()
        observeViewModel()
        setupSearchView()

        viewModel.fetchPelangganLunas(bulan, tahun)
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.fetchPelangganLunas(bulan, tahun, search = newText.orEmpty())
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        lunasAdapter = LunasAdapter { pelanggan ->
            selectedPelanggan = pelanggan
            val bottomSheet = LunasBottomSheetFragment.newInstance()
            bottomSheet.show(childFragmentManager, LunasBottomSheetFragment.TAG)
        }
        binding.rvLunas.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = lunasAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.pelangganList.observe(viewLifecycleOwner) { pelangganList ->
            lunasAdapter.submitList(pelangganList)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if(isLoading) View.VISIBLE else View.GONE
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(bulan: Int, tahun: Int): LunasFragment {
            val fragment = LunasFragment()
            val args = Bundle()
            args.putInt("bulan", bulan)
            args.putInt("tahun", tahun)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onItemClick(item: String) {
        val bottomSheet = childFragmentManager.findFragmentByTag(LunasBottomSheetFragment.TAG) as? LunasBottomSheetFragment

        when (item) {
            "batalkan" -> {
                selectedPelanggan?.let { pelanggan ->
                    context?.let { ctx ->
                        MaterialAlertDialogBuilder(ctx)
                            .setTitle("Konfirmasi Pembatalan")
                            .setMessage("Anda yakin ingin membatalkan pembayaran untuk ${pelanggan.namaPelanggan}?")
                            .setNegativeButton("Tidak") { _, _ ->
                                bottomSheet?.dismiss()
                            }
                            .setPositiveButton("Ya, Batalkan") { _, _ ->
                                viewModel.batalPembayaran(pelanggan.idTagihan, bulan, tahun)
                                bottomSheet?.dismiss()
                            }
                            .show()
                    }
                } ?: run {
                    Toast.makeText(context, "Silakan pilih pelanggan terlebih dahulu", Toast.LENGTH_SHORT).show()
                    bottomSheet?.dismiss()
                }
            }
            "cetak" -> {
                selectedPelanggan?.let { pelanggan ->
                    val bundle = Bundle().apply {
                        putSerializable("pelanggan_item", pelanggan)
                    }
                    findNavController().navigate(R.id.action_global_cetakNotaFragment, bundle)
                } ?: run {
                    Toast.makeText(context, "Silakan pilih pelanggan terlebih dahulu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
