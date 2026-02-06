package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayoutMediator
import com.linkbit.billrt.databinding.FragmentRekapBinding
import java.text.DateFormatSymbols
import java.util.Calendar

class RekapFragment : Fragment() {

    private var _binding: FragmentRekapBinding? = null
    private val binding get() = _binding!!

    private val rekapViewModel: RekapViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRekapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = RekapPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Tercatat"
                1 -> "Tunggakan"
                else -> null
            }
        }.attach()

        rekapViewModel.month.observe(viewLifecycleOwner) {
            binding.btnBulan.text = "Bulan: ${DateFormatSymbols().months[it - 1]}"
        }

        rekapViewModel.year.observe(viewLifecycleOwner) {
            binding.btnTahun.text = "Tahun: $it"
        }

        binding.btnBulan.setOnClickListener {
            showMonthPicker()
        }

        binding.btnTahun.setOnClickListener {
            showYearPicker()
        }
    }

    private fun showMonthPicker() {
        //  You can use a custom dialog or a library to show a list of months
    }

    private fun showYearPicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select year")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it
            val year = calendar.get(Calendar.YEAR)
            val month = rekapViewModel.month.value ?: (calendar.get(Calendar.MONTH) + 1)
            rekapViewModel.setDate(month, year)
        }

        datePicker.show(parentFragmentManager, "YEAR_PICKER")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class RekapPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RekapTercatatFragment()
            1 -> RekapTunggakanFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}