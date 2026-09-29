package com.linkbit.billrt

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    val apiService: ApiService by lazy { ApiConfig.apiService }

    /**
     * Menambahkan padding atas setinggi Status Bar agar konten tidak tertutup.
     * Gunakan ini pada Toolbar atau Header Layout.
     */
    protected fun applyWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Tambahkan padding atas sesuai tinggi status bar
            v.updatePadding(top = systemBars.top)
            
            // Jika view memiliki tinggi spesifik (bukan wrap_content), sesuaikan tingginya
            val layoutParams = v.layoutParams
            if (layoutParams.height > 0) {
                // Simpan tinggi asli di tag jika belum ada
                val originalHeight = v.tag as? Int ?: layoutParams.height
                if (v.tag == null) v.tag = originalHeight
                
                v.updateLayoutParams {
                    height = originalHeight + systemBars.top
                }
            }
            
            insets
        }
    }
}
