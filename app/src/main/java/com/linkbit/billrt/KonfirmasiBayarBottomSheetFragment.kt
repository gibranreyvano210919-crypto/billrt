package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetKonfirmasiBayarBinding
import com.linkbit.billrt.model.PelangganBelumBayarItem
import com.linkbit.billrt.model.TambahPembayaranRequest
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class KonfirmasiBayarBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetKonfirmasiBayarBinding? = null
    private val binding get() = _binding!!

    private val calendar = Calendar.getInstance()
    private var item: PelangganBelumBayarItem? = null

    private val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updateDateInView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetKonfirmasiBayarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCanceledOnTouchOutside(false)

        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.isDraggable = false

        item = arguments?.getSerializable("pelanggan_item") as? PelangganBelumBayarItem

        if (item == null) {
            Toast.makeText(context, "Error: Data pelanggan tidak ditemukan", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        binding.tvNamaPelanggan.text = "Nama: ${item?.namaPelanggan}"
        binding.tvInvoiceDetail.text = "Invoice: ${item?.invoice}"

        updateDateInView()

        binding.etTanggalPembayaran.setOnClickListener {
            showDatePickerDialog()
        }

        binding.tilTanggalPembayaran.setEndIconOnClickListener {
            showDatePickerDialog()
        }

        binding.btnKonfirmasiBayar.setOnClickListener {
            val tanggalBayar = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val keterangan = binding.etKeterangan.text.toString()
            val request = TambahPembayaranRequest(
                idTagihan = item!!.invoice!!,
                metodeBayar = "Cash",
                adminId = 1, // Ganti dengan ID admin yang sebenarnya
                tanggalBayar = tanggalBayar,
                keterangan = keterangan
            )
            tambahPembayaran(request)
        }

        binding.btnBatal.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    private fun tambahPembayaran(request: TambahPembayaranRequest) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.tagihanApiService.tambahPembayaran(request)
                if (response.status) {
                    Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                    setFragmentResult("payment_successful", bundleOf("refresh" to true))
                    dismiss()
                } else {
                    Toast.makeText(context, "Gagal melakukan pembayaran: ${response.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerDialog() {
        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etTanggalPembayaran.setText(sdf.format(calendar.time))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(item: PelangganBelumBayarItem): KonfirmasiBayarBottomSheetFragment {
            val fragment = KonfirmasiBayarBottomSheetFragment()
            val args = Bundle()
            args.putSerializable("pelanggan_item", item)
            fragment.arguments = args
            return fragment
        }
    }
}
