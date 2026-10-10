package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetKonfirmasiBayarBinding
import com.linkbit.billrt.model.NotaDataResponse
import com.linkbit.billrt.model.PelangganBelumBayarItem
import com.linkbit.billrt.model.PelangganLunasItem
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
    private lateinit var sessionManager: SessionManager

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

        sessionManager = SessionManager(requireContext())
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

        // Menampilkan Nama User Login
        val userNameLogin = sessionManager.getUserName()
        binding.etIdUser.setText(userNameLogin)

        setupMetodeBayar()
        updateDateInView()

        binding.etTanggalPembayaran.setOnClickListener { showDatePickerDialog() }
        binding.tilTanggalPembayaran.setEndIconOnClickListener { showDatePickerDialog() }

        binding.btnKonfirmasiBayar.setOnClickListener {
            val metode = binding.actMetodeBayar.text.toString()
            val tanggalBayar = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val keterangan = binding.etKeterangan.text.toString()
            
            val userId = sessionManager.getUserId()?.toIntOrNull() ?: 0

            if (userId == 0) {
                Toast.makeText(context, "Error: Sesi berakhir, silakan login kembali", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentItem = item ?: return@setOnClickListener

            val lunasItem = PelangganLunasItem(
                idTagihan = currentItem.invoice,
                id_pelanggan = currentItem.id_pelanggan,
                namaPelanggan = currentItem.namaPelanggan,
                mikrotikUsername = currentItem.mikrotikUsername,
                teleponPelanggan = currentItem.teleponPelanggan,
                idWilayah = currentItem.idWilayah,
                wilayah = currentItem.wilayah,
                nominalTagihan = currentItem.nominal,
                jumlahBayar = currentItem.nominal,
                tanggalBayar = tanggalBayar,
                metodeBayar = metode,
                keterangan = keterangan,
                idUserPencatat = userId,
                namaPencatat = sessionManager.getUserName(),
                statusAktif = "1",
                statusPembayaran = 1,
                bulanTagihan = currentItem.bulanTagihan?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1),
                tahunTagihan = currentItem.tahunTagihan ?: calendar.get(Calendar.YEAR)
            )

            if (metode == "Transfer") {
                // Gunakan endpoint confirm_payment untuk Transfer sesuai logika PHP
                confirmPaymentTransfer(currentItem.invoice!!, metode, tanggalBayar, keterangan, userId, lunasItem)
            } else {
                // Gunakan endpoint tambah_pembayaran untuk Tunai (Cash)
                val request = TambahPembayaranRequest(
                    idTagihan = currentItem.invoice!!,
                    metodeBayar = "Cash",
                    idUser = userId,
                    tanggalBayar = tanggalBayar,
                    keterangan = keterangan
                )
                tambahPembayaran(request, lunasItem)
            }
        }

        binding.btnBatal.setOnClickListener { dismiss() }
    }

    private fun setupMetodeBayar() {
        val listMetode = arrayOf("Tunai", "Transfer")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listMetode)
        binding.actMetodeBayar.setAdapter(adapter)
    }

    private fun tambahPembayaran(request: TambahPembayaranRequest, lunasItem: PelangganLunasItem) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.tagihanApiService.tambahPembayaran(request)
                if (response.status) {
                    Toast.makeText(context, response.message ?: "Pembayaran berhasil", Toast.LENGTH_LONG).show()
                    setFragmentResult("payment_successful", bundleOf("refresh" to true))

                    val bundle = Bundle()
                    val notaData = response.data?.nota
                    if (notaData != null) {
                        bundle.putSerializable("nota_data", notaData)
                    } else {
                        bundle.putSerializable("pelanggan_item", lunasItem)
                    }

                    try {
                        findNavController().navigate(R.id.action_global_cetakNotaFragment, bundle)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    dismiss()
                } else {
                    Toast.makeText(context, "Gagal: ${response.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmPaymentTransfer(idTagihan: String, metode: String, tanggal: String, keterangan: String, adminId: Int, lunasItem: PelangganLunasItem) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.confirmPayment(
                    id_tagihan = idTagihan,
                    metode_bayar = metode,
                    tanggal_bayar = tanggal,
                    keterangan = keterangan,
                    admin_id = adminId
                )
                handleApiResponse(response.status, response.message, lunasItem)
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleApiResponse(status: Boolean, message: String, lunasItem: PelangganLunasItem? = null) {
        if (status) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            setFragmentResult("payment_successful", bundleOf("refresh" to true))

            lunasItem?.let {
                val bundle = Bundle().apply {
                    putSerializable("pelanggan_item", it)
                }
                try {
                    findNavController().navigate(R.id.action_global_cetakNotaFragment, bundle)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            dismiss()
        } else {
            Toast.makeText(context, "Gagal: $message", Toast.LENGTH_LONG).show()
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
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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
