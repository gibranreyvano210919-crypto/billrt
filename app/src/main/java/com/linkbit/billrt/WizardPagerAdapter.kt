package com.linkbit.billrt

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Kontrak/Interface untuk setiap langkah wizard yang memerlukan validasi.
 */
interface WizardValidator {
    /**
     * Memeriksa apakah input pada langkah ini valid.
     * @return Pesan error jika tidak valid, atau `null` jika valid.
     */
    fun validate(): String?
}

class WizardPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WizardStep1InfoDasarFragment()
            1 -> WizardStep2InfoJaringanFragment()
            2 -> WizardStep3InfoTeknisFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}
