package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.color.MaterialColors
import com.linkbit.billrt.adapter.DaftarSecretAdapter
import com.linkbit.billrt.databinding.FragmentDaftarSecretBinding
import com.linkbit.billrt.viewmodel.DaftarSecretViewModel

class DaftarSecretFragment : BaseFragment() {

    private var _binding: FragmentDaftarSecretBinding? = null
    private val binding get() = _binding!!

    private val args: DaftarSecretFragmentArgs by navArgs()
    private val viewModel: DaftarSecretViewModel by viewModels()
    private lateinit var adapter: DaftarSecretAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDaftarSecretBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSystemBarsColor()
        
        // Menangani agar toolbar tidak tertutup status bar dan tombol tidak tertutup navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }

        setupToolbar()
        setupBottomSheet()
        setupRecyclerView()
        observeViewModel()

        viewModel.fetchSecrets(args.routerId)
    }

    private fun setupSystemBarsColor() {
        val window = activity?.window ?: return
        
        // Mengambil warna primary dari theme yang digunakan oleh toolbar
        val colorPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary, android.graphics.Color.BLUE)
        
        // Set warna status bar dan navigation bar agar sama dengan toolbar
        window.statusBarColor = colorPrimary
        window.navigationBarColor = colorPrimary
        
        // Menyesuaikan warna ikon (gelap/terang) berdasarkan kecerahan warna primary
        val isLightColor = MaterialColors.isColorLight(colorPrimary)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = isLightColor
            isAppearanceLightNavigationBars = isLightColor
        }
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = "Daftar Secret"
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            
            // Inflate menu search ke toolbar
            inflateMenu(R.menu.menu_search)
            val searchItem = menu.findItem(R.id.action_search)
            val searchView = searchItem.actionView as? SearchView

            searchView?.apply {
                queryHint = "Cari Secret..."
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        adapter.filter(newText)
                        return true
                    }
                })
            }
        }
    }

    private fun setupBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetTambahSecret)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED // Keep it visible

        binding.btnTambah.setOnClickListener {
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.daftarSecretFragment) {
                val action = DaftarSecretFragmentDirections.actionDaftarSecretFragmentToTambahPppoeFragment(args.routerId)
                navController.navigate(action)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = DaftarSecretAdapter(emptyList()) { secret, isEnabled ->
            viewModel.setSecretStatus(args.routerId, secret, isEnabled)
        }
        binding.rvSecrets.layoutManager = LinearLayoutManager(context)
        binding.rvSecrets.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.secrets.observe(viewLifecycleOwner) { secrets ->
            adapter.updateData(secrets)
            binding.tvEmpty.visibility = if (secrets.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                binding.tvEmpty.text = errorMessage
                binding.tvEmpty.visibility = View.VISIBLE
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.onToastShown()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
