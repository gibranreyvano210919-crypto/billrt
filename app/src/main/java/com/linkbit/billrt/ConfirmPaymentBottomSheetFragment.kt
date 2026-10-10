package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.linkbit.billrt.databinding.BottomSheetConfirmPaymentBinding
import java.text.SimpleDateFormat
import java.util.*

class ConfirmPaymentBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetConfirmPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    private var listener: ((metode: String, tanggal: String, keterangan: String) -> Unit)? = null

    fun setOnConfirmClickListener(listener: (metode: String, tanggal: String, keterangan: String) -> Unit) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetConfirmPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        
        // Menampilkan ID User yang sedang login
        val userId = sessionManager.getUserId() ?: "-"
        binding.tieIdUser.setText(userId)

        setupMetodePembayaran()
        setupTanggalPicker()

        // Pre-fill with current date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.tieTanggalBayar.setText(sdf.format(Date()))

        binding.btnSubmitPayment.setOnClickListener {
            val metode = binding.actMetodeBayar.text.toString()
            val tanggal = binding.tieTanggalBayar.text.toString()
            val keterangan = binding.tieKeterangan.text.toString()
            listener?.invoke(metode, tanggal, keterangan)
            dismiss()
        }
    }

    private fun setupMetodePembayaran() {
        val metodeBayar = arrayOf("Tunai", "Transfer")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, metodeBayar)
        binding.actMetodeBayar.setAdapter(adapter)
        binding.actMetodeBayar.setText(metodeBayar[0], false)
    }

    private fun setupTanggalPicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal Bayar")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.tieTanggalBayar.setText(sdf.format(calendar.time))
        }

        binding.tieTanggalBayar.setOnClickListener {
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ConfirmPaymentBottomSheet"
    }
}
