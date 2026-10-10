package com.linkbit.billrt

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_ID = "USER_ID"
        const val USER_NAME = "USER_NAME"
        const val USER_USERNAME = "USER_USERNAME"
        const val AUTH_TOKEN = "AUTH_TOKEN"
        const val USER_LEVEL = "USER_LEVEL"
        const val TEKNISI_ID = "TEKNISI_ID"
        const val DEFAULT_ROUTER_ID = "DEFAULT_ROUTER_ID"
    }

    /**
     * Simpan data user ke SharedPreferences.
     */
    fun saveUser(userData: UserData) {
        val editor = prefs.edit()
        editor.putInt(USER_ID, userData.idUser)
        editor.putString(USER_NAME, userData.nama)
        editor.putString(USER_USERNAME, userData.username)
        editor.putString(AUTH_TOKEN, userData.token)
        editor.putInt(USER_LEVEL, userData.level ?: -1)
        editor.putInt(TEKNISI_ID, userData.teknisiId ?: -1)
        editor.apply()
    }

    /**
     * Ambil ID user yang tersimpan.
     */
    fun getUserId(): String? {
        val id = prefs.getInt(USER_ID, -1)
        return if (id != -1) id.toString() else null
    }

    /**
     * Ambil Nama user yang tersimpan.
     */
    fun getUserName(): String {
        return prefs.getString(USER_NAME, "Admin") ?: "Admin"
    }
    
    /**
     * Ambil Level user yang tersimpan.
     */
    fun getUserLevel(): Int {
        return prefs.getInt(USER_LEVEL, -1)
    }

    /**
     * Ambil ID Teknisi jika ada.
     */
    fun getTeknisiId(): Int {
        return prefs.getInt(TEKNISI_ID, -1)
    }

    /**
     * Ambil Token jika ada.
     */
    fun getToken(): String? {
        return prefs.getString(AUTH_TOKEN, null)
    }

    /**
     * Simpan ID Router Default.
     */
    fun saveDefaultRouterId(id: Int) {
        prefs.edit().putInt(DEFAULT_ROUTER_ID, id).apply()
    }

    /**
     * Ambil ID Router Default.
     */
    fun getDefaultRouterId(): Int {
        return prefs.getInt(DEFAULT_ROUTER_ID, 0)
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
