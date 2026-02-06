package com.linkbit.billrt

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.Calendar

class TagihanPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        // Get current month and year to pass to fragments
        val calendar = Calendar.getInstance()
        val bulan = calendar.get(Calendar.MONTH) + 1
        val tahun = calendar.get(Calendar.YEAR)

        return when (position) {
            0 -> LunasFragment.newInstance(bulan, tahun)
            1 -> BelumBayarFragment.newInstance(bulan, tahun)
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}
