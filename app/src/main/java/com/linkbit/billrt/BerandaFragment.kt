package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.linkbit.billrt.databinding.FragmentBerandaBinding
import com.linkbit.billrt.LaporanFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BerandaFragment : Fragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Menampilkan nama pengguna
        val userName = activity?.intent?.getStringExtra("USER_NAMA") ?: "Pengguna"
        binding.tvWelcome.text = "Selamat Datang, $userName!"

        // Menampilkan tanggal dan waktu saat ini
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm", Locale.getDefault())
        binding.tvDateTime.text = sdf.format(Date())

        // Menambahkan listener untuk setiap kartu menu
        setupMenuListeners()
    }

    private fun setupMenuListeners() {
        binding.cardUser.setOnClickListener { openFragment(PelangganFragment()) }
        binding.cardLaporan.setOnClickListener { openFragment(LaporanFragment()) }
        // Tambahkan listener untuk kartu lain jika diperlukan
        // binding.cardOlt.setOnClickListener { openFragment(SomeOtherFragment()) }
        // binding.cardMikrotik.setOnClickListener { openFragment(SomeOtherFragment()) }
        // etc.
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
