package com.linkbit.billrt

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetMikrotikBinding
import com.linkbit.billrt.model.MikrotikAccount
import com.linkbit.billrt.viewmodel.MikrotikAccountsViewModel

class MikrotikBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMikrotikBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share the same ViewModel instance with the fragment
    private val viewModel: MikrotikAccountsViewModel by viewModels({ requireParentFragment() })

    private var accountId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            accountId = it.getInt(ARG_ACCOUNT_ID)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMikrotikBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()

        if (accountId > 0) {
            viewModel.fetchAccountDetail(accountId)
        }

        binding.btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun observeViewModel() {
        viewModel.accountDetail.observe(viewLifecycleOwner) { account ->
            account?.let { populateUi(it) }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.editStatus.observe(viewLifecycleOwner) { response ->
            response?.let {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                if (it.status) {
                    viewModel.resetEditStatus()
                    dismiss()
                }
            }
        }
    }

    private fun populateUi(account: MikrotikAccount) {
        binding.etRouterName.setText(account.routerName)
        binding.etIpAddress.setText(account.ipAddress)
        binding.etUsername.setText(account.username)
        binding.etPort.setText(account.port.toString())
        binding.etOwnerId.setText(account.ownerId?.toString() ?: "0")
    }

    private fun saveChanges() {
        val routerName = binding.etRouterName.text.toString()
        val ipAddress = binding.etIpAddress.text.toString()
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString().takeIf { it.isNotEmpty() }
        val port = binding.etPort.text.toString().toIntOrNull() ?: 8728
        val ownerId = binding.etOwnerId.text.toString().toIntOrNull() ?: 0

        if (routerName.isEmpty() || ipAddress.isEmpty() || username.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua field wajib", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateAccount(
            accountId,
            routerName,
            ipAddress,
            username,
            password,
            port,
            ownerId
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "MikrotikBottomSheetFragment"
        private const val ARG_ACCOUNT_ID = "account_id"

        fun newInstance(accountId: Int): MikrotikBottomSheetFragment {
            val fragment = MikrotikBottomSheetFragment()
            val args = Bundle()
            args.putInt(ARG_ACCOUNT_ID, accountId)
            fragment.arguments = args
            return fragment
        }
    }
}
