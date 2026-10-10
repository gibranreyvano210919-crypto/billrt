package com.linkbit.billrt

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.linkbit.billrt.databinding.FragmentPelangganAktifDetailBinding
import com.linkbit.billrt.model.StandardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelangganAktifDetailFragment : BaseFragment() {

    private var _binding: FragmentPelangganAktifDetailBinding? = null
    private val binding get() = _binding!!
    private val args: PelangganAktifDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganAktifDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Menggunakan applyWindowInsets dari BaseFragment pada AppBarLayout
        applyWindowInsets(binding.appBarLayout)

        setupToolbar()
        fetchPelangganDetails()
        
        binding.btnBayar.setOnClickListener {
            val action = PelangganAktifDetailFragmentDirections.actionPelangganAktifDetailFragmentToBayarTagihanFragment(args.pelangganId)
            findNavController().navigate(action)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun fetchPelangganDetails() {
        val pelangganId = args.pelangganId

        apiService.getApiDetailPelanggan(pelangganId).enqueue(object : Callback<ApiDetailPelangganResponse> {
            override fun onResponse(call: Call<ApiDetailPelangganResponse>, response: Response<ApiDetailPelangganResponse>) {
                if (!isAdded || _binding == null) return

                if (response.isSuccessful && response.body()?.data != null) {
                    val pelanggan = response.body()!!.data!!
                    populateUI(pelanggan)
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal memuat detail pelanggan"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiDetailPelangganResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateUI(pelanggan: ApiDetailPelangganData) {
        // Card 1
        binding.tvIdPelanggan.text = "ID Pelanggan: ${pelanggan.card1Identitas.idPelanggan}"
        binding.tvNamaPelanggan.text = pelanggan.card1Identitas.namaPelanggan
        binding.tvTeleponPelanggan.text = pelanggan.card1Identitas.teleponPelanggan
        
        binding.tvTeleponPelanggan.setOnClickListener {
            val bottomSheet = GantiTeleponPelangganBottomSheet.newInstance(
                pelanggan.card1Identitas.idPelanggan,
                pelanggan.card1Identitas.teleponPelanggan
            ) {
                fetchPelangganDetails() // Refresh data setelah sukses
            }
            bottomSheet.show(childFragmentManager, GantiTeleponPelangganBottomSheet.TAG)
        }

        binding.btnWhatsapp.setOnClickListener {
            openWhatsApp(pelanggan.card1Identitas.teleponPelanggan)
        }

        // Card 2
        binding.tvRekapStatus.text = "Rekap Status: ${pelanggan.card2Tagihan.rekapStatus}"
        binding.tvJumlahNunggak.text = "Jumlah Nunggak: ${pelanggan.card2Tagihan.jumlahNunggak} Bulan"

        // Card 3
        binding.tvUsername.text = pelanggan.card3Jaringan.username
        binding.tvStatusKoneksi.text = pelanggan.card3Jaringan.statusKoneksi
        binding.tvUptime.text = "Uptime: ${pelanggan.card3Jaringan.uptime}"
        binding.tvIpAktif.text = "IP Aktif: ${pelanggan.card3Jaringan.ipAktif}"
        binding.tvMacClientLive.text = "MAC Client Live: ${pelanggan.card3Jaringan.macClientLive ?: "-"}"
        binding.tvMacAddressDb.text = "MAC DB: ${pelanggan.card3Jaringan.macAddressDb ?: "-"}"
        binding.tvTotalUsage.text = "Total Usage: ${pelanggan.card3Jaringan.totalUsage}"
        binding.tvLastLogout.text = "Last Logout: ${pelanggan.card3Jaringan.lastLogout}"

        binding.tvMacAddressDb.setOnClickListener {
            val bottomSheet = ReplaceMacBottomSheetFragment.newInstance(pelanggan.card3Jaringan.macAddressDb).apply {
                setOnSaveListener { newMac ->
                    updateMacAddress(pelanggan.card1Identitas.idPelanggan, newMac)
                }
            }
            bottomSheet.show(childFragmentManager, "ReplaceMacBottomSheetFragment")
        }

        // Card 4
        binding.tvTglDaftar.text = "Tanggal Daftar: ${pelanggan.card4Detail.tglDaftar}"
        binding.tvInstallationDate.text = "Tanggal Instalasi: ${pelanggan.card4Detail.installationDate}"
        binding.tvAlamatPelanggan.text = pelanggan.card4Detail.alamatPelanggan
        binding.tvLatlong.text = "${pelanggan.card4Detail.latitude}, ${pelanggan.card4Detail.longitude}"
        
        binding.btnGoogleMaps.setOnClickListener {
            val gmmIntentUri = Uri.parse(pelanggan.card4Detail.googleMapsLink)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            startActivity(mapIntent)
        }

        // Card 5
        binding.tvNamaPaket.text = pelanggan.card5Paket.namaPaket
        binding.tvHargaPaket.text = "Harga: ${pelanggan.card5Paket.hargaPaket}"
        binding.tvProfileMikrotik.text = "Profile Mikrotik: ${pelanggan.card5Paket.profileMikrotik ?: "-"}"
        binding.tvWilayahPaket.text = "Wilayah: ${pelanggan.card5Paket.wilayah}"
    }

    private fun openWhatsApp(number: String?) {
        if (number.isNullOrEmpty()) {
            Toast.makeText(context, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        
        var phoneNumber = number.replace(Regex("[^0-9]"), "")
        if (phoneNumber.startsWith("0")) {
            phoneNumber = "62" + phoneNumber.substring(1)
        }
        
        try {
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMacAddress(pelangganId: String, newMac: String) {
        val request = UpdateMacRequest(macAddress = newMac)
        apiService.updateMacAddress(pelangganId, request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "MAC Address berhasil diupdate", Toast.LENGTH_SHORT).show()
                    fetchPelangganDetails() // Refresh data
                } else {
                    Toast.makeText(context, "Gagal mengupdate MAC Address", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
