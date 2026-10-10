package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetFilterTransaksiBinding
import com.linkbit.billrt.model.SummaryUserPencatat
import com.linkbit.billrt.model.SummaryWilayahTransaksi
import java.text.NumberFormat
import java.util.Locale

class TransaksiFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterTransaksiBinding? = null
    private val binding get() = _binding!!

    private var onFilterApplied: ((Int?, Int?) -> Unit)? = null
    private var summaryAdminList: List<SummaryUserPencatat> = emptyList()
    private var summaryWilayahList: List<SummaryWilayahTransaksi> = emptyList()
    
    private var selectedAdminId: Int? = null
    private var selectedWilayahId: Int? = null

    companion object {
        fun newInstance(
            summaryAdmin: List<SummaryUserPencatat>,
            summaryWilayah: List<SummaryWilayahTransaksi>,
            selectedAdmin: Int?,
            selectedWilayah: Int?
        ): TransaksiFilterBottomSheet {
            val fragment = TransaksiFilterBottomSheet()
            fragment.summaryAdminList = summaryAdmin
            fragment.summaryWilayahList = summaryWilayah
            fragment.selectedAdminId = selectedAdmin
            fragment.selectedWilayahId = selectedWilayah
            return fragment
        }
    }

    fun setOnFilterAppliedListener(listener: (Int?, Int?) -> Unit) {
        onFilterApplied = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterTransaksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupButtons()
    }

    private fun setupDropdowns() {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0

        // Admin Dropdown
        val adminItems = mutableListOf<String>()
        adminItems.add("Semua Admin")
        summaryAdminList.forEach { admin ->
            adminItems.add("${admin.namaAdmin} (${admin.totalTransaksi} | ${numberFormat.format(admin.totalNominal)})")
        }

        val adminAdapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, adminItems)
        binding.acAdminFilter.setAdapter(adminAdapter)

        // Set initial selection text
        selectedAdminId?.let { id ->
            val index = summaryAdminList.indexOfFirst { it.idUserPencatat == id }
            if (index != -1) {
                binding.acAdminFilter.setText(adminItems[index + 1], false)
            }
        }

        // Wilayah Dropdown
        val wilayahItems = mutableListOf<String>()
        wilayahItems.add("Semua Wilayah")
        summaryWilayahList.forEach { wilayah ->
            wilayahItems.add("${wilayah.namaWilayah} (${wilayah.totalTransaksi} | ${numberFormat.format(wilayah.totalNominal)})")
        }

        val wilayahAdapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, wilayahItems)
        binding.acWilayahFilter.setAdapter(wilayahAdapter)

        // Set initial selection text
        selectedWilayahId?.let { id ->
            val index = summaryWilayahList.indexOfFirst { it.idWilayah == id }
            if (index != -1) {
                binding.acWilayahFilter.setText(wilayahItems[index + 1], false)
            }
        }
    }

    private fun setupButtons() {
        binding.btnApply.setOnClickListener {
            val adminText = binding.acAdminFilter.text.toString()
            val wilayahText = binding.acWilayahFilter.text.toString()

            val adminPos = getAdminPosition(adminText)
            val wilayahPos = getWilayahPosition(wilayahText)

            val adminId = if (adminPos > 0) summaryAdminList[adminPos - 1].idUserPencatat else null
            val wilayahId = if (wilayahPos > 0) summaryWilayahList[wilayahPos - 1].idWilayah else null

            onFilterApplied?.invoke(adminId, wilayahId)
            dismiss()
        }

        binding.btnReset.setOnClickListener {
            onFilterApplied?.invoke(null, null)
            dismiss()
        }
    }

    private fun getAdminPosition(text: String): Int {
        if (text == "Semua Admin") return 0
        val items = mutableListOf<String>()
        items.add("Semua Admin")
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        summaryAdminList.forEach { admin ->
            items.add("${admin.namaAdmin} (${admin.totalTransaksi} | ${numberFormat.format(admin.totalNominal)})")
        }
        return items.indexOf(text)
    }

    private fun getWilayahPosition(text: String): Int {
        if (text == "Semua Wilayah") return 0
        val items = mutableListOf<String>()
        items.add("Semua Wilayah")
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        summaryWilayahList.forEach { wilayah ->
            items.add("${wilayah.namaWilayah} (${wilayah.totalTransaksi} | ${numberFormat.format(wilayah.totalNominal)})")
        }
        return items.indexOf(text)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
