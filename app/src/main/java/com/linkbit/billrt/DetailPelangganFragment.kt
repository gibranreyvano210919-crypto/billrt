package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.google.android.material.color.MaterialColors
import com.linkbit.billrt.databinding.FragmentDetailPelangganBinding
import com.linkbit.billrt.model.PelangganDetailV2Data
import com.linkbit.billrt.model.PelangganDetailV2Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailPelangganFragment : BaseFragment() {

    private var _binding: FragmentDetailPelangganBinding? = null
    private val binding get() = _binding!!

    private var pelangganId: String? = null
    private var currentPelanggan: PelangganDetailV2Data? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pelangganId = it.getString("pelangganId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSystemBarsColor()
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(top = systemBars.top)
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }

        setupToolbar()

        pelangganId?.let {
            fetchDetailPelanggan(it)
        } ?: run {
            Toast.makeText(context, "ID Pelanggan tidak valid", Toast.LENGTH_SHORT).show()
        }

        binding.tvDetailTelepon.setOnClickListener {
            currentPelanggan?.identitas?.let { identitas ->
                val bottomSheet = GantiTeleponPelangganBottomSheet.newInstance(
                    identitas.idPelanggan,
                    identitas.teleponPelanggan
                ) {
                    pelangganId?.let { fetchDetailPelanggan(it) }
                }
                bottomSheet.show(childFragmentManager, GantiTeleponPelangganBottomSheet.TAG)
            }
        }

        binding.tvDetailMacAddress.setOnClickListener {
            currentPelanggan?.jaringan?.let { jaringan ->
                currentPelanggan?.identitas?.let { identitas ->
                    val bottomSheet = GantiMacPelangganBottomSheet.newInstance(
                        identitas.idPelanggan,
                        jaringan.macAddressDb
                    ) {
                        pelangganId?.let { fetchDetailPelanggan(it) }
                    }
                    bottomSheet.show(childFragmentManager, GantiMacPelangganBottomSheet.TAG)
                }
            }
        }
    }

    private fun setupSystemBarsColor() {
        val window = activity?.window ?: return
        val colorPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary, android.graphics.Color.BLUE)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = colorPrimary
        
        val isLightColor = MaterialColors.isColorLight(colorPrimary)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = isLightColor
            isAppearanceLightNavigationBars = isLightColor
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun fetchDetailPelanggan(id: String) {
        setLoading(true)
        apiService.getPelangganDetailV2(idPelanggan = id).enqueue(object : Callback<PelangganDetailV2Response> {
            override fun onResponse(call: Call<PelangganDetailV2Response>, response: Response<PelangganDetailV2Response>) {
                if (!isAdded || _binding == null) return
                setLoading(false)
                if (response.isSuccessful) {
                    val detail = response.body()?.data
                    if (detail != null) {
                        currentPelanggan = detail
                        bindData(detail)
                    } else {
                        Toast.makeText(context, "Data pelanggan tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMsg = "Gagal memuat detail (Error ${response.code()})"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<PelangganDetailV2Response>, t: Throwable) {
                if (!isAdded || _binding == null) return
                setLoading(false)
                Toast.makeText(context, "Gagal memuat detail. Periksa koneksi Anda.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun bindData(data: PelangganDetailV2Data) {
        // Card 1: Identitas
        binding.tvDetailNamaPelanggan.text = data.identitas.namaPelanggan
        binding.tvDetailIdPelanggan.text = "ID: ${data.identitas.idPelanggan}"
        binding.tvDetailTelepon.text = data.identitas.teleponPelanggan

        // Card 2: Tagihan
        binding.tvRekapStatusTagihan.text = data.tagihan.rekapStatus

        // Card 3: Jaringan
        binding.tvDetailUsernameMikrotik.text = data.jaringan.username
        binding.tvStatusKoneksi.text = data.jaringan.statusKoneksi
        binding.tvUptime.text = data.jaringan.uptime
        binding.tvIpAktif.text = data.jaringan.ipAktif
        binding.tvMacLive.text = data.jaringan.macClientLive
        binding.tvDetailMacAddress.text = data.jaringan.macAddressDb
        binding.tvTotalUsage.text = data.jaringan.totalUsage
        binding.tvLastLogout.text = data.jaringan.lastLogout

        // Card 4: Detail
        binding.tvTglDaftar.text = "Tgl Daftar: ${data.detail.tglDaftar}"
        binding.tvTglInstalasi.text = "Tgl Instalasi: ${data.detail.installationDate}"
        binding.tvDetailAlamat.text = "Alamat: ${data.detail.alamatPelanggan}"
        binding.tvKoordinat.text = "Koordinat: ${data.detail.latitude}, ${data.detail.longitude}"

        // Card 5: Paket
        binding.tvNamaPaket.text = data.paket.namaPaket
        binding.tvHargaPaket.text = data.paket.hargaPaket
        binding.tvProfileMikrotik.text = "Profile: ${data.paket.profileMikrotik ?: "-"}"
        binding.tvDetailWilayah.text = "Wilayah: ${data.paket.wilayah}"
        
        // Dynamic styling for status
        if (data.jaringan.statusKoneksi.contains("Online", ignoreCase = true)) {
            binding.tvStatusKoneksi.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
        } else {
            binding.tvStatusKoneksi.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
