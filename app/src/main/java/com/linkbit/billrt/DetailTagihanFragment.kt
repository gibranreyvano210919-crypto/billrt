package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.linkbit.billrt.databinding.FragmentDetailTagihanBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DetailTagihanFragment : Fragment() {

    private var _binding: FragmentDetailTagihanBinding? = null
    private val binding get() = _binding!!

    private var invoiceId: String? = null

    private val apiService: ApiService by lazy {
        (activity as MainActivity).apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            invoiceId = it.getString(ARG_INVOICE_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailTagihanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        if (invoiceId != null) {
            fetchDetailTagihan(invoiceId!!)
        } else {
            Toast.makeText(requireContext(), "ID Invoice tidak valid.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
        
        binding.btnConfirmPayment.setOnClickListener {
            showConfirmPaymentBottomSheet()
        }
    }

    private fun showConfirmPaymentBottomSheet() {
        val bottomSheet = ConfirmPaymentBottomSheetFragment()
        bottomSheet.setOnConfirmClickListener { metode, tanggal, keterangan ->
            // Lakukan sesuatu dengan data yang diterima
            confirmPayment(metode, tanggal, keterangan)
        }
        bottomSheet.show(parentFragmentManager, ConfirmPaymentBottomSheetFragment.TAG)
    }
    
    private fun confirmPayment(metode: String, tanggal: String, keterangan: String) {
        lifecycleScope.launch {
            try {
                val response = apiService.confirmPayment(
                    id_tagihan = invoiceId!!,
                    metode_bayar = metode,
                    tanggal_bayar = tanggal,
                    keterangan = keterangan,
                    admin_id = 1 // TODO: Ganti dengan ID admin yang login
                )
                if (response.status) {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    fetchDetailTagihan(invoiceId!!)
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun fetchDetailTagihan(invoiceId: String) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = apiService.getDetailBayar(invoiceId)
                if (response.status) {
                    updateUI(response)
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateUI(response: DetailBayarResponse) {
        val gson = Gson()
        if (response.isLunas) {
            val data = gson.fromJson(response.data, DetailBayarLunasData::class.java)
            showLunasLayout(data)
        } else {
            val data = gson.fromJson(response.data, DetailBayarBelumLunasData::class.java)
            showBelumLunasLayout(data)
        }
    }

    private fun showLunasLayout(data: DetailBayarLunasData) {
        binding.layoutLunas.visibility = View.VISIBLE
        binding.layoutBelumLunas.visibility = View.GONE

        binding.tvInvoiceStatus.text = "LUNAS"
        binding.tvInvoiceStatus.setBackgroundResource(R.drawable.badge_lunas)
        binding.tvInvoice.text = data.noInvoice
        setCustomerData(data.pelanggan, data.username, data.telepon, data.wilayah, data.tglInstalasi)

        binding.tvLunasPeriode.text = data.periode
        binding.tvTglBayar.text = data.tglBayar
        binding.tvLunasNominal.text = formatCurrency(data.nominal.toDouble())
        binding.tvMetodeBayar.text = data.metode
        binding.tvAdmin.text = data.adminPenerima
        binding.tvCatatan.text = data.catatan
    }

    private fun showBelumLunasLayout(data: DetailBayarBelumLunasData) {
        binding.layoutBelumLunas.visibility = View.VISIBLE
        binding.layoutLunas.visibility = View.GONE

        binding.tvInvoiceStatus.text = "BELUM LUNAS"
        binding.tvInvoiceStatus.setBackgroundResource(R.drawable.badge_belum_lunas)
        binding.tvInvoice.text = data.noInvoice
        setCustomerData(data.pelanggan, data.username, data.telepon, data.wilayah, data.tglInstalasi)

        binding.tvBelumLunasPeriode.text = data.periode
        binding.tvBelumLunasNominal.text = formatCurrency(data.nominal.toDouble())
        binding.tvJatuhTempo.text = data.jatuhTempo
        binding.tvInstruksi.text = data.instruksi
    }

    private fun setCustomerData(nama: String, username: String, telepon: String, wilayah: String, tglInstalasi: String) {
        binding.tvCustomerName.text = nama
        binding.tvCustomerUsername.text = username
        binding.tvCustomerPhone.text = telepon
        binding.tvCustomerWilayah.text = wilayah
        binding.tvCustomerTglInstalasi.text = tglInstalasi
    }

    private fun formatCurrency(amount: Double): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.minimumFractionDigits = 0
        return numberFormat.format(amount)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_INVOICE_ID = "invoice_id"

        fun newInstance(invoiceId: String): DetailTagihanFragment {
            val fragment = DetailTagihanFragment()
            val args = Bundle()
            args.putString(ARG_INVOICE_ID, invoiceId)
            fragment.arguments = args
            return fragment
        }
    }
}
