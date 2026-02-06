package com.linkbit.billrt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * This activity is now obsolete and should be removed.
 * The main navigation flow is handled by MainActivity and its NavHostFragment.
 */
class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The content is now set by MainActivity's navigation graph.
        // This activity can be safely removed from the project.
    }
}

/**
 * This interface is also obsolete as the bottom navigation visibility
 * is now handled automatically by MainActivity's destination listener.
 */
interface BottomNavHandler {
    fun setBottomNavVisibility(isVisible: Boolean)
}
