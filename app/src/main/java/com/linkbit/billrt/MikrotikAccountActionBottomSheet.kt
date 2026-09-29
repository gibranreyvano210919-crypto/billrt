package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetMikrotikAccountsBinding
import com.linkbit.billrt.model.MikrotikAccount
import com.linkbit.billrt.viewmodel.MikrotikAccountsViewModel

class MikrotikAccountActionBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMikrotikAccountsBinding? = null
    private val binding get() = _binding!!

    // Menggunakan scope parent fragment agar berbagi ViewModel yang sama dengan MikrotikAccountsFragment
    private val viewModel: MikrotikAccountsViewModel by viewModels({ requireParentFragment() })

    private lateinit var account: MikrotikAccount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            account = it.getSerializable(ARG_ACCOUNT) as MikrotikAccount
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMikrotikAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = account.routerName

        // 1. Edit Akun
        binding.cardEdit.setOnClickListener {
            val bottomSheet = MikrotikBottomSheetFragment.newInstance(account.id)
            bottomSheet.show(parentFragmentManager, MikrotikBottomSheetFragment.TAG)
            dismiss()
        }

        // 2. Set Default
        binding.cardSetDefault.setOnClickListener {
            showSetDefaultConfirmation()
        }

        // 3. Info Status
        binding.cardInfo.setOnClickListener {
            val action = MikrotikAccountsFragmentDirections.actionMikrotikAccountsFragmentToMikrotikBillingFragment(account.id)
            findNavController().navigate(action)
            dismiss()
        }

        // 4. Hapus Akun
        binding.cardDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showSetDefaultConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Router Default")
            .setMessage("Jadikan '${account.routerName}' sebagai router default untuk semua pelanggan?")
            .setPositiveButton("Ya, Set Default") { _, _ ->
                viewModel.setAsDefault(account.id)
                dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Akun")
            .setMessage("Apakah Anda yakin ingin menghapus router '${account.routerName}'?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteAccount(account.id)
                dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "MikrotikAccountActionBottomSheet"
        private const val ARG_ACCOUNT = "arg_account"

        fun newInstance(account: MikrotikAccount): MikrotikAccountActionBottomSheet {
            val fragment = MikrotikAccountActionBottomSheet()
            val args = Bundle()
            args.putSerializable(ARG_ACCOUNT, account)
            fragment.arguments = args
            return fragment
        }
    }
}
