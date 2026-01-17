package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.linkbit.billrt.databinding.FragmentTambahPelangganWizardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TambahPelangganWizardFragment : Fragment() {

    private var _binding: FragmentTambahPelangganWizardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TambahPelangganViewModel by activityViewModels()
    private val apiService: ApiService by lazy { ApiConfig.getApiService() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahPelangganWizardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPagerWizard.adapter = WizardPagerAdapter(this)
        TabLayoutMediator(binding.tabLayoutWizard, binding.viewPagerWizard) { _, _ -> }.attach()

        binding.viewPagerWizard.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateNavigationButtons(position)
            }
        })

        binding.btnNext.setOnClickListener { handleNextClick() }
        binding.btnPrevious.setOnClickListener { handlePreviousClick() }
    }

    private fun updateNavigationButtons(position: Int) {
        binding.btnPrevious.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE
        binding.btnNext.text = if (position == 2) "Simpan" else "Berikutnya"
    }

    private fun handleNextClick() {
        val currentPosition = binding.viewPagerWizard.currentItem
        if (saveCurrentStepData(currentPosition)) {
            if (currentPosition < 2) {
                binding.viewPagerWizard.currentItem = currentPosition + 1
            } else {
                submitData()
            }
        }
    }

    private fun handlePreviousClick() {
        binding.viewPagerWizard.currentItem--
    }

    private fun saveCurrentStepData(position: Int): Boolean {
        val fragment = childFragmentManager.findFragmentByTag("f$position")
        return when (val currentFragment = fragment) {
            is WizardStep1Fragment -> currentFragment.saveData()
            is WizardStep2Fragment -> currentFragment.saveData()
            is WizardStep3Fragment -> currentFragment.saveData()
            else -> false
        }
    }

    private fun submitData() {
        val request = TambahPelangganRequest(
            nama_pelanggan = viewModel.namaPelanggan,
            alamat_pelanggan = viewModel.alamatPelanggan,
            telepon_pelanggan = viewModel.teleponPelanggan,
            id_paket = viewModel.idPaket,
            id_wilayah = viewModel.idWilayah,
            installation_date = viewModel.installationDate,
            mikrotik_username = viewModel.mikrotikUsername,
            mikrotik_password = viewModel.mikrotikPassword,
            latitude = viewModel.latitude,
            longitude = viewModel.longitude,
            mac_address = viewModel.macAddress
        )

        apiService.tambahPelanggan(request).enqueue(object : Callback<StandardResponse> {
             override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Pelanggan baru berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "Gagal menyimpan: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
