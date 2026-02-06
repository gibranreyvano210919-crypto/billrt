package com.linkbit.billrt

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.Calendar

class PelangganPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragments = mutableMapOf<Int, Fragment>()

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> DaftarPelangganFragment.newInstance() // For "Semua"
            1 -> {
                val calendar = Calendar.getInstance()
                val bulan = calendar.get(Calendar.MONTH) + 1
                val tahun = calendar.get(Calendar.YEAR)
                LunasFragment.newInstance(bulan, tahun)
            }
            2 -> {
                val calendar = Calendar.getInstance()
                val bulan = calendar.get(Calendar.MONTH) + 1
                val tahun = calendar.get(Calendar.YEAR)
                BelumBayarFragment.newInstance(bulan, tahun)
            }
            else -> throw IllegalStateException("Invalid position")
        }
        fragments[position] = fragment
        return fragment
    }

    fun getFragmentAt(position: Int): PelangganFilterListener? {
        return fragments[position] as? PelangganFilterListener
    }
}
