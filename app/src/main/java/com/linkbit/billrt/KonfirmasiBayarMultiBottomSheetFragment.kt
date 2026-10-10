package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.adapter.PeriodeChecklistAdapter
import com.linkbit.billrt.adapter.PeriodeItem
import com.linkbit.billrt.databinding.BottomSheetKonfirmasiBayarMultiBinding
import com.linkbit.billrt.model.NotaDataResponse
import com.linkbit.billrt.model.PelangganLunasItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class KonfirmasiBayarMultiBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetKonfirmasiBayarMultiBinding? = null
    private val binding get() = _binding!!

    private val calendar = Calendar.getInstance()
    private var tagihanData: TagihanBelumBayar? = null
    private lateinit var sessionManager: SessionManager
    private val apiService = ApiConfig.apiService
    private lateinit var periodeAdapter: PeriodeChecklistAdapter

    private val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updateDateInView()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let { sheet ->
            val behavior = BottomSheetBehavior.from(sheet)
            sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

            sheet.post {
                val offset = getToolbarBottomOffset()
                behavior.isFitToContents = false
                behavior.expandedOffset = offset
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun getToolbarBottomOffset(): Int {
        val act = activity ?: return 0
        val toolbar = act.findViewById<View>(R.id.toolbar)
            ?: act.findViewById<View>(R.id.toolbar_pencarian)

        if (toolbar != null && toolbar.height > 0) {
            val location = IntArray(2)
            toolbar.getLocationOnScreen(location)
            return location[1] + toolbar.height
        }

        var actionBarHeight = 0
        val tv = TypedValue()
        if (act.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }
        
        val statusBarHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            act.window.decorView.rootWindowInsets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
        } else {
            @Suppress("DEPRECATION")
            act.window.decorView.rootWindowInsets?.stableInsetTop ?: 0
        }

        return statusBarHeight + actionBarHeight
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetKonfirmasiBayarMultiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Menggunakan BundleCompat atau cast manual untuk kompatibilitas
        tagihanData = arguments?.getSerializable("tagihan_data") as? TagihanBelumBayar

        if (tagihanData == null) {
            Toast.makeText(context, "Error: Data tagihan tidak ditemukan", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        setupRecyclerView()
        displayData()
        updateDateInView()

        // Menampilkan nama user pencatat yang login
        binding.etIdUser.setText(sessionManager.getUserName())

        binding.etTanggalPembayaran.setOnClickListener { showDatePickerDialog() }
        binding.tilTanggalPembayaran.setEndIconOnClickListener { showDatePickerDialog() }

        binding.btnKonfirmasiBayar.setOnClickListener {
            prosesPembayaranMulti()
        }

        binding.btnBatal.setOnClickListener {
            dismiss()
        }
    }

    private fun setupRecyclerView() {
        periodeAdapter = PeriodeChecklistAdapter(emptyList()) {
            updateTotalNominal()
        }
        binding.rvPeriode.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = periodeAdapter
        }
    }

    private fun displayData() {
        tagihanData?.let {
            binding.tvNamaPelanggan.text = it.namaPelanggan
            binding.tvUsernameWilayah.text = "User: ${it.mikrotikUsername ?: "-"} | Wilayah: ${it.wilayah ?: "-"}"
            binding.tvNamaPaket.text = it.namaPaket ?: "Paket Tidak Ditemukan"
            binding.tvPerforma.text = it.performaPembayaran ?: "100%"

            binding.tvLastPayment.text = "Bayar Terakhir: ${it.tglBayarTerakhir ?: "Belum ada riwayat"}"
            binding.tvAdminPencatat.text = "Dicatat oleh: ${it.namaPencatat ?: "-"}"

            val ids = it.listIdTagihan?.split(",")?.map { id -> id.trim() } ?: emptyList()
            val rincian = it.rincianTunggakan ?: emptyList()

            val items = ids.mapIndexed { index, id ->
                val rincianText = rincian.getOrNull(index) ?: "Periode ${index + 1}"
                // Extract nominal from string "Bulan Tahun (Rp 100.000)"
                val nominal = try {
                    val regex = Regex("""Rp\s?([\d.]+)""")
                    val match = regex.find(rincianText)
                    match?.groupValues?.get(1)?.replace(".", "")?.toFloat() ?: (it.totalNominal / ids.size)
                } catch (e: Exception) {
                    it.totalNominal / ids.size
                }

                // Extract just the name "Bulan Tahun" from "Bulan Tahun (Rp 100.000)"
                val nameOnly = if (rincianText.contains(" (")) {
                    rincianText.substringBefore(" (")
                } else {
                    rincianText
                }

                PeriodeItem(
                    id = id,
                    nama = nameOnly,
                    nominal = nominal
                )
            }

            periodeAdapter.updateData(items)
            updateTotalNominal()
        }
    }

    private fun updateTotalNominal() {
        val total = periodeAdapter.getCheckedTotal()
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        binding.tvTotalNominal.text = "Total: ${formatter.format(total)}"
        
        binding.btnKonfirmasiBayar.isEnabled = total > 0
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etTanggalPembayaran.setText(sdf.format(calendar.time))
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

    private fun prosesPembayaranMulti() {
        val tagihan = tagihanData ?: return
        val listId = periodeAdapter.getCheckedIds()

        if (listId.isEmpty()) {
            Toast.makeText(context, "Pilih minimal satu periode", Toast.LENGTH_SHORT).show()
            return
        }

        val tanggalBayar = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val userId = sessionManager.getUserId()?.toIntOrNull() ?: 0

        if (userId == 0) {
            Toast.makeText(context, "Error: Sesi berakhir, silakan login kembali", Toast.LENGTH_SHORT).show()
            return
        }

        val request = TambahPembayaranMultiRequest(
            idTagihanList = listId,
            idPelanggan = tagihan.idPelanggan.toString(),
            metodeBayar = "Cash",
            idUser = userId,
            tanggalBayar = tanggalBayar
        )

        binding.btnKonfirmasiBayar.isEnabled = false
        
        apiService.tambahPembayaranMulti(request).enqueue(object : Callback<NotaDataResponse> {
            override fun onResponse(call: Call<NotaDataResponse>, response: Response<NotaDataResponse>) {
                if (!isAdded) return
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, response.body()?.message ?: "Pembayaran berhasil", Toast.LENGTH_LONG).show()
                    setFragmentResult("payment_multi_successful", bundleOf("refresh" to true))

                    val bundle = Bundle()
                    val notaData = response.body()?.data?.nota
                    if (notaData != null) {
                        bundle.putSerializable("nota_data", notaData)
                    } else {
                        val totalNominal = periodeAdapter.getCheckedTotal()
                        val lunasItem = PelangganLunasItem(
                            idTagihan = listId.joinToString(", "),
                            id_pelanggan = tagihan.idPelanggan,
                            namaPelanggan = tagihan.namaPelanggan,
                            mikrotikUsername = tagihan.mikrotikUsername,
                            teleponPelanggan = tagihan.teleponPelanggan,
                            idWilayah = null,
                            wilayah = tagihan.wilayah,
                            nominalTagihan = totalNominal,
                            jumlahBayar = totalNominal,
                            tanggalBayar = tanggalBayar,
                            metodeBayar = "Cash",
                            keterangan = "Pembayaran Multi (${listId.size} periode)",
                            idUserPencatat = userId,
                            namaPencatat = sessionManager.getUserName(),
                            statusAktif = "1",
                            statusPembayaran = 1,
                            bulanTagihan = calendar.get(Calendar.MONTH) + 1,
                            tahunTagihan = calendar.get(Calendar.YEAR)
                        )
                        bundle.putSerializable("pelanggan_item", lunasItem)
                    }

                    try {
                        findNavController().navigate(R.id.action_global_cetakNotaFragment, bundle)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    dismiss()
                } else {
                    binding.btnKonfirmasiBayar.isEnabled = true
                    Toast.makeText(context, "Gagal: ${response.body()?.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<NotaDataResponse>, t: Throwable) {
                if (!isAdded) return
                binding.btnKonfirmasiBayar.isEnabled = true
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(tagihan: TagihanBelumBayar): KonfirmasiBayarMultiBottomSheetFragment {
            val fragment = KonfirmasiBayarMultiBottomSheetFragment()
            val args = Bundle()
            args.putSerializable("tagihan_data", tagihan as Serializable)
            fragment.arguments = args
            return fragment
        }
    }
}
