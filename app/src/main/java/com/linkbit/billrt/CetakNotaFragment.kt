package com.linkbit.billrt

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentCetakNotaBinding
import com.linkbit.billrt.model.NotaData
import com.linkbit.billrt.model.PelangganLunasItem
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

class CetakNotaFragment : BaseFragment() {

    private var _binding: FragmentCetakNotaBinding? = null
    private val binding get() = _binding!!

    private var itemPelanggan: PelangganLunasItem? = null
    private var notaData: NotaData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            itemPelanggan = it.getSerializable("pelanggan_item") as? PelangganLunasItem
            notaData = it.getSerializable("nota_data") as? NotaData
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCetakNotaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adjust for Status Bar (top) and Navigation Bar (bottom) insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.updatePadding(top = systemBars.top)
            val basePaddingBottom = (24 * resources.displayMetrics.density).toInt()
            binding.layoutBottomActions.updatePadding(bottom = basePaddingBottom + systemBars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        populateData()

        binding.btnShareJpg.setOnClickListener {
            val bitmap = createBitmapFromView(binding.cardNota)
            shareBitmapAsJpg(bitmap)
        }

        binding.btnCetakPrinter.setOnClickListener {
            Toast.makeText(context, "Mencetak nota ke printer...", Toast.LENGTH_SHORT).show()
        }

        binding.btnTutup.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun populateData() {
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        if (notaData != null) {
            val nota = notaData!!
            
            val namaPerusahaan = nota.perusahaan?.namaPerusahaan
            if (!namaPerusahaan.isNullOrEmpty() && namaPerusahaan != "-") {
                binding.tvNamaPerusahaan.text = namaPerusahaan
            }

            binding.tvIdTagihan.text = nota.noNota ?: "-"
            binding.tvNamaPelanggan.text = nota.pelanggan?.namaPelanggan ?: "-"

            val noHp = nota.pelanggan?.noHp
            if (!noHp.isNullOrEmpty() && noHp != "-") {
                binding.layoutNoHp.visibility = View.VISIBLE
                binding.tvNoHp.text = noHp
            } else {
                binding.layoutNoHp.visibility = View.GONE
            }

            val wilayahText = if (nota.wilayah?.namaWilayah?.isNotEmpty() == true && nota.wilayah.namaWilayah != "-") {
                nota.wilayah.namaWilayah
            } else {
                nota.pelanggan?.alamat
            }
            binding.tvWilayah.text = wilayahText ?: "-"

            val periodeText = when {
                nota.periodeTagihan?.periodeFormat?.isNotEmpty() == true -> nota.periodeTagihan.periodeFormat
                nota.periodeTagihan?.namaBulan?.isNotEmpty() == true && nota.periodeTagihan.tahunTagihan != null -> "${nota.periodeTagihan.namaBulan} ${nota.periodeTagihan.tahunTagihan}"
                !nota.rincianItem.isNullOrEmpty() && !nota.rincianItem.firstOrNull()?.periode.isNullOrEmpty() -> nota.rincianItem.firstOrNull()?.periode
                else -> "-"
            }
            binding.tvPeriode.text = periodeText

            binding.tvTanggalBayar.text = nota.tanggalBayar ?: "-"
            binding.tvMetodeBayar.text = nota.metodeBayar ?: "Tunai"

            val ket = nota.keterangan
            if (!ket.isNullOrEmpty() && ket != "-") {
                binding.layoutKeterangan.visibility = View.VISIBLE
                binding.tvKeterangan.text = ket
            } else {
                binding.layoutKeterangan.visibility = View.GONE
            }

            binding.tvPencatat.text = nota.kasir ?: "-"

            val total = nota.totalBayar ?: 0f
            val formattedTotal = formatRupiah.format(total.toDouble())
                .replace("Rp", "Rp ")
                .replace(",00", "")
            binding.tvTotalBayar.text = formattedTotal

            // Populate Rincian Item
            if (!nota.rincianItem.isNullOrEmpty()) {
                binding.layoutRincianSection.visibility = View.VISIBLE
                binding.llRincianItem.removeAllViews()
                for (item in nota.rincianItem) {
                    addRincianRow(item.deskripsi ?: "-", item.harga ?: 0f, formatRupiah)
                }
            } else {
                binding.layoutRincianSection.visibility = View.GONE
            }
        } else if (itemPelanggan != null) {
            val item = itemPelanggan!!
            binding.tvIdTagihan.text = "#${item.idTagihan ?: item.id_pelanggan}"
            binding.tvNamaPelanggan.text = item.namaPelanggan

            val noHp = item.teleponPelanggan
            if (!noHp.isNullOrEmpty() && noHp != "-") {
                binding.layoutNoHp.visibility = View.VISIBLE
                binding.tvNoHp.text = noHp
            } else {
                binding.layoutNoHp.visibility = View.GONE
            }

            binding.tvWilayah.text = item.wilayah ?: "-"
            binding.tvPeriode.text = "${getBulanName(item.bulanTagihan)} ${item.tahunTagihan}"
            binding.tvTanggalBayar.text = item.tanggalBayar ?: "-"
            binding.tvMetodeBayar.text = item.metodeBayar ?: "Tunai / Cash"

            val ket = item.keterangan
            if (!ket.isNullOrEmpty() && ket != "-") {
                binding.layoutKeterangan.visibility = View.VISIBLE
                binding.tvKeterangan.text = ket
            } else {
                binding.layoutKeterangan.visibility = View.GONE
            }

            binding.tvPencatat.text = item.namaPencatat ?: "-"

            val jumlahBayarFormatted = formatRupiah.format(item.jumlahBayar.toDouble())
                .replace("Rp", "Rp ")
                .replace(",00", "")
            binding.tvTotalBayar.text = jumlahBayarFormatted
            binding.layoutRincianSection.visibility = View.GONE
        }
    }

    private fun addRincianRow(deskripsi: String, harga: Float, formatRupiah: NumberFormat) {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
        }

        val tvDeskripsi = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = deskripsi
            setTextColor(Color.parseColor("#1E293B"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        }

        val formattedHarga = formatRupiah.format(harga.toDouble())
            .replace("Rp", "Rp ")
            .replace(",00", "")

        val tvHarga = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = formattedHarga
            setTextColor(Color.parseColor("#1E293B"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTypeface(typeface, Typeface.BOLD)
        }

        row.addView(tvDeskripsi)
        row.addView(tvHarga)
        binding.llRincianItem.addView(row)
    }

    private fun getBulanName(bulan: Int): String {
        val namaBulan = arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        return if (bulan in 1..12) namaBulan[bulan - 1] else bulan.toString()
    }

    private fun createBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap
    }

    private fun shareBitmapAsJpg(bitmap: Bitmap) {
        try {
            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs()
            val fileName = "nota_pembayaran_${notaData?.noNota ?: itemPelanggan?.id_pelanggan ?: System.currentTimeMillis()}.jpg"
            val file = File(cachePath, fileName)
            val stream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )

            if (contentUri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Bagikan Nota Pembayaran"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan nota: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CetakNotaFragment"

        fun newInstance(item: PelangganLunasItem): CetakNotaFragment {
            val fragment = CetakNotaFragment()
            val args = Bundle()
            args.putSerializable("pelanggan_item", item)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(notaData: NotaData): CetakNotaFragment {
            val fragment = CetakNotaFragment()
            val args = Bundle()
            args.putSerializable("nota_data", notaData)
            fragment.arguments = args
            return fragment
        }
    }
}
