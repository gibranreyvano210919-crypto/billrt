package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.linkbit.billrt.databinding.FragmentTambahPelangganWizardBinding

class TambahPelangganWizardFragment : Fragment() {

    private var _binding: FragmentTambahPelangganWizardBinding? = null
    private val binding get() = _binding!!
    private lateinit var wizardAdapter: WizardPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahPelangganWizardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wizardAdapter = WizardPagerAdapter(this)
        binding.viewPagerWizard.adapter = wizardAdapter
        binding.viewPagerWizard.isUserInputEnabled = false // Mencegah geser manual

        // Hubungkan TabLayout dengan ViewPager2
        TabLayoutMediator(binding.tabLayoutWizard, binding.viewPagerWizard) { tab, position ->
            tab.text = "Langkah ${position + 1}"
            tab.view.isClickable = false // Nonaktifkan klik pada tab
        }.attach()

        updateNavigationButtons(0)

        binding.viewPagerWizard.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateNavigationButtons(position)
            }
        })

        // Logika untuk tombol navigasi
        binding.btnNext.setOnClickListener {
            validateAndGoToNext()
        }

        binding.btnPrevious.setOnClickListener {
            goToPreviousPage()
        }
    }

    fun validateAllSteps(): Boolean {
        for (i in 0 until wizardAdapter.itemCount) {
            val fragment = childFragmentManager.findFragmentByTag("f$i")
            if (fragment is WizardValidator) {
                val errorMessage = fragment.validate()
                if (errorMessage != null) {
                    binding.viewPagerWizard.currentItem = i // Langsung pindah ke tab yang error
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }
        return true
    }

    private fun validateAndGoToNext() {
        val tag = "f${binding.viewPagerWizard.currentItem}"
        val currentFragment = childFragmentManager.findFragmentByTag(tag)

        if (currentFragment is WizardValidator) {
            val errorMessage = currentFragment.validate()
            if (errorMessage != null) {
                // Jika ada pesan error, tampilkan Toast dan JANGAN pindah halaman.
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            } else {
                // Jika tidak ada error (null), pindah ke halaman berikutnya.
                goToNextPage()
            }
        } else {
            // Jika fragmen tidak ditemukan, anggap valid untuk sementara, tapi ini tidak ideal.
            goToNextPage()
        }
    }

    private fun updateNavigationButtons(position: Int) {
        binding.btnPrevious.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE
        binding.btnNext.visibility = if (position < wizardAdapter.itemCount - 1) View.VISIBLE else View.INVISIBLE
    }

    private fun goToNextPage() {
        val currentItem = binding.viewPagerWizard.currentItem
        if (currentItem < wizardAdapter.itemCount - 1) {
            binding.viewPagerWizard.currentItem = currentItem + 1
        }
    }

    private fun goToPreviousPage() {
        val currentItem = binding.viewPagerWizard.currentItem
        if (currentItem > 0) {
            binding.viewPagerWizard.currentItem = currentItem - 1
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
