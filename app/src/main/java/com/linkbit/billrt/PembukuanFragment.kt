package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentPembukuanBinding

class PembukuanFragment : Fragment() {

    private var _binding: FragmentPembukuanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPembukuanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        // Pemasukkan
        binding.tvTransaksiCash.setOnClickListener {
            navController.navigate(R.id.action_pembukuanFragment_to_laporanHarianFragment)
        }

        binding.tvTransaksiOnline.setOnClickListener {
            navController.navigate(R.id.action_pembukuanFragment_to_laporanBulananFragment)
        }

        binding.tvTotalPemasukkanLain.setOnClickListener {
            navController.navigate(R.id.action_pembukuanFragment_to_laporanTahunanFragment)
        }

        // Pengeluaran
        binding.tvGajiKaryawan.setOnClickListener { showToast("Gaji Karyawan") }
        binding.tvPasangBaru.setOnClickListener { showToast("Pasang Baru") }
        binding.tvPerbaikanAlat.setOnClickListener { showToast("Perbaikan Alat") }
        binding.tvBayarBandwidth.setOnClickListener { showToast("Bayar Bandwith") }
        binding.tvBayarKangTagih.setOnClickListener { showToast("Bayar Kang Tagih") }
        binding.tvListrikPdamPulsa.setOnClickListener { showToast("Listrik / PDAM / Pulsa") }
        binding.tvBayarMarketing.setOnClickListener { showToast("Bayar Marketing") }
        binding.tvLainLain.setOnClickListener { showToast("Lain lain") }

        // Menu Laporan & Catatan (Dipindahkan dari Beranda)
        binding.cardRiwayatTunggakan.setOnClickListener {
            navController.navigate(R.id.action_berandaFragment_to_customerFragment)
        }
        binding.cardKas.setOnClickListener {
            navController.navigate(R.id.action_berandaFragment_to_kasFragment)
        }
        binding.cardRekapTunggakan.setOnClickListener {
            navController.navigate(R.id.action_berandaFragment_to_rekapTunggakanFragment)
        }
        binding.cardRekapTercatat.setOnClickListener {
            navController.navigate(R.id.action_berandaFragment_to_rekapTercatatFragment)
        }
        binding.cardSetoran.setOnClickListener {
            navController.navigate(R.id.action_berandaFragment_to_setoranFragment)
        }
        binding.cardHistoriCatat.setOnClickListener {
            navController.navigate(R.id.action_berandaFragment_to_historiCatatFragment)
        }
        binding.cardValidasiPeriode.setOnClickListener {
            navController.navigate(R.id.action_berandaFragment_to_validasiPeriodeFragment)
        }

        // Menu Lain
        binding.menuUangDiAdmin.setOnClickListener { showToast("Uang di Admin") }
        binding.menuSemuaPembukuan.setOnClickListener { showToast("Semua Pembukuan") }
        binding.menuPembayaranRvAdmin.setOnClickListener { showToast("Pemb. Rv Admin") }
        binding.menuRangkumanKeuangan.setOnClickListener { showToast("Rangkuman Keuangan") }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
