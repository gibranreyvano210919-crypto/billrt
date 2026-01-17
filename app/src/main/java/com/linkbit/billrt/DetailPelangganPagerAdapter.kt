package com.linkbit.billrt

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class DetailPelangganPagerAdapter(fragment: Fragment, private val pelanggan: PelangganData) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PelangganDetailContentFragment.newInstance(pelanggan)
            1 -> BulanFragment.newInstance(pelanggan)
            2 -> BayarFragment.newInstance(pelanggan)
            3 -> TeknisiFragment.newInstance(pelanggan)
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}
