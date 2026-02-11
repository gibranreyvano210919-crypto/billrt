package com.linkbit.billrt

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_ID = "user_id"
        const val IS_LOGGED_IN = "is_logged_in"
    }

    /**
     * Simpan data sesi login.
     */
    fun createLoginSession(userId: String) {
        val editor = prefs.edit()
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(USER_ID, userId)
        editor.apply()
    }

    /**
     * Cek status login.
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    /**
     * Ambil ID user yang tersimpan.
     */
    fun getUserId(): String? {
        return prefs.getString(USER_ID, null)
    }

    /**
     * Hapus data sesi (logout).
     */
    fun logoutUser() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
