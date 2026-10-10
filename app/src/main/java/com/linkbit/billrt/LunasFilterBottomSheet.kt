package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetFilterPelangganLunasBinding
import com.linkbit.billrt.model.LunasSummary
import com.linkbit.billrt.model.PelangganLunasItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LunasFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterPelangganLunasBinding? = null
    private val binding get() = _binding!!

    private var summary: LunasSummary? = null
    private var allItems: List<PelangganLunasItem> = emptyList()

    private var onApplyFilter: ((Int?, Int?, String?, String?) -> Unit)? = null
    private var onReset: (() -> Unit)? = null

    private val wilayahIds = mutableListOf<Int?>()
    private val pencatatIds = mutableListOf<Int?>()

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var startDate: String? = null
    private var endDate: String? = null

    private var selectedWilayahId: Int? = null
    private var selectedPencatatId: Int? = null

    companion object {
        const val TAG = "LunasFilterBottomSheet"
        fun newInstance(summary: LunasSummary?, items: List<PelangganLunasItem>): LunasFilterBottomSheet {
            val fragment = LunasFilterBottomSheet()
            fragment.summary = summary
            fragment.allItems = items
            return fragment
        }
    }

    fun setFilterListeners(
        onApply: (Int?, Int?, String?, String?) -> Unit,
        onReset: () -> Unit
    ) {
        this.onApplyFilter = onApply
        this.onReset = onReset
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterPelangganLunasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDatePickers()
        setupDropdowns()

        binding.btnTerapkan.setOnClickListener {
            onApplyFilter?.invoke(selectedWilayahId, selectedPencatatId, startDate, endDate)
            dismiss()
        }
    }

    private fun setupDatePickers() {
        binding.etStartDate.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                binding.etStartDate.setText(date)
            }
        }

        binding.etEndDate.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                binding.etEndDate.setText(date)
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                onDateSelected(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupDropdowns() {
        // Setup Wilayah Dropdown
        val wilayahLabels = mutableListOf<String>()
        wilayahLabels.add("Semua Wilayah")
        wilayahIds.clear()
        wilayahIds.add(null)

        val nameToIdWilayah = allItems.associateBy({ it.wilayah ?: "Tanpa Wilayah" }, { it.idWilayah })
        summary?.countWilayah?.forEach { (name, count) ->
            val label = if (name == null) "Unknown ($count)" else "$name ($count)"
            wilayahLabels.add(label)
            wilayahIds.add(nameToIdWilayah[name ?: "Tanpa Wilayah"])
        }

        val wilayahAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_filter, wilayahLabels)
        binding.acWilayah.setAdapter(wilayahAdapter)
        binding.acWilayah.setOnItemClickListener { _, _, position, _ ->
            selectedWilayahId = wilayahIds.getOrNull(position)
        }

        // Setup Pencatat Dropdown
        val pencatatLabels = mutableListOf<String>()
        pencatatLabels.add("Semua Pencatat")
        pencatatIds.clear()
        pencatatIds.add(null)

        val nameToIdPencatat = allItems.associateBy({ it.namaPencatat ?: "Sistem" }, { it.idUserPencatat })
        summary?.countPencatat?.forEach { (name, count) ->
            val label = if (name == null) "Unknown ($count)" else "$name ($count)"
            pencatatLabels.add(label)
            pencatatIds.add(nameToIdPencatat[name ?: "Sistem"])
        }

        val pencatatAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_filter, pencatatLabels)
        binding.acPencatat.setAdapter(pencatatAdapter)
        binding.acPencatat.setOnItemClickListener { _, _, position, _ ->
            selectedPencatatId = pencatatIds.getOrNull(position)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
