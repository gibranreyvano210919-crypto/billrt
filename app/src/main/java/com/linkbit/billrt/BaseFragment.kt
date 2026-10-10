package com.linkbit.billrt

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

open class BaseFragment : Fragment() {
    val apiService: ApiService by lazy { ApiConfig.apiService }

    private var isNetworkErrorDialogShowing = false

    /**
     * Menampilkan dialog informasi/peringatan ketika terjadi kegagalan koneksi / timeout jaringan.
     */
    protected fun showNetworkErrorDialog(
        context: Context,
        throwable: Throwable,
        onRetry: (() -> Unit)? = null
    ) {
        if (!isAdded || isNetworkErrorDialogShowing) return
        isNetworkErrorDialogShowing = true

        val dialogView = layoutInflater.inflate(R.layout.dialog_network_info, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val tvSubtitle = dialogView.findViewById<TextView>(R.id.tv_dialog_subtitle)
        val tvDetail = dialogView.findViewById<TextView>(R.id.tv_network_detail)

        val detailMsg = throwable.localizedMessage ?: throwable.message ?: "SocketTimeoutException"
        tvDetail.text = "HTTP FAILED: $detailMsg"

        when (throwable) {
            is SocketTimeoutException -> {
                tvTitle.text = "Info Jaringan (Timeout)"
                tvSubtitle.text = "Koneksi ke server 112.78.170.196:8890 melebihi batas waktu."
            }
            is ConnectException -> {
                tvTitle.text = "Info Jaringan (Server Offline)"
                tvSubtitle.text = "Gagal terhubung ke server 112.78.170.196:8890."
            }
            is UnknownHostException -> {
                tvTitle.text = "Info Jaringan (Terputus)"
                tvSubtitle.text = "Perangkat Anda sedang tidak terhubung ke internet."
            }
            else -> {
                tvTitle.text = "Info Status Jaringan"
                tvSubtitle.text = "Terjadi masalah saat menghubungkan ke server."
            }
        }

        MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(if (onRetry != null) "Coba Lagi" else "Tutup") { dialog, _ ->
                isNetworkErrorDialogShowing = false
                dialog.dismiss()
                onRetry?.invoke()
            }
            .apply {
                if (onRetry != null) {
                    setNegativeButton("Tutup") { dialog, _ ->
                        isNetworkErrorDialogShowing = false
                        dialog.dismiss()
                    }
                }
            }
            .setOnDismissListener {
                isNetworkErrorDialogShowing = false
            }
            .show()
    }

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
