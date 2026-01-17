package com.linkbit.billrt

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class WizardPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WizardStep1Fragment()
            1 -> WizardStep2Fragment()
            2 -> WizardStep3Fragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}
