package com.linkbit.billrt

/**
 * An interface for communication between the main PelangganFragment and its child fragments
 * used in a ViewPager setup, handling filtering and search queries.
 */
interface PelangganFilterListener {
    fun onFilterChanged(month: Int, year: Int)
    fun onSearchQuery(query: String?)
}
