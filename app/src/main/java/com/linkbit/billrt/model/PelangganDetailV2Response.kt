package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class PelangganDetailV2Response(
    @SerializedName("status") val status: Boolean,
    @SerializedName("sync_mikrotik") val syncMikrotik: SyncMikrotik,
    @SerializedName("data") val data: PelangganDetailV2Data
)

data class SyncMikrotik(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

data class PelangganDetailV2Data(
    @SerializedName("card_1_identitas") val identitas: CardIdentitas,
    @SerializedName("card_2_tagihan") val tagihan: CardTagihan,
    @SerializedName("card_3_jaringan") val jaringan: CardJaringan,
    @SerializedName("card_4_detail") val detail: CardDetail,
    @SerializedName("card_5_paket") val paket: CardPaket
)

data class CardIdentitas(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("telepon_pelanggan") val teleponPelanggan: String
)

data class CardTagihan(
    @SerializedName("rekap_status") val rekapStatus: String,
    @SerializedName("jumlah_nunggak") val jumlahNunggak: Int
)

data class CardJaringan(
    @SerializedName("username") val username: String,
    @SerializedName("status_koneksi") val statusKoneksi: String,
    @SerializedName("uptime") val uptime: String,
    @SerializedName("ip_aktif") val ipAktif: String,
    @SerializedName("mac_client_live") val macClientLive: String,
    @SerializedName("mac_address_db") val macAddressDb: String,
    @SerializedName("total_usage") val totalUsage: String,
    @SerializedName("last_logout") val lastLogout: String
)

data class CardDetail(
    @SerializedName("tgl_daftar") val tglDaftar: String,
    @SerializedName("installation_date") val installationDate: String,
    @SerializedName("tanggal_isolasi", alternate = ["tgl_isolasi", "tgl_jatuh_tempo", "jatuh_tempo"]) val tanggalIsolasi: String? = null,
    @SerializedName("alamat_pelanggan") val alamatPelanggan: String,
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String
)

data class CardPaket(
    @SerializedName("nama_paket") val namaPaket: String,
    @SerializedName("harga_paket") val hargaPaket: String,
    @SerializedName("profile_mikrotik") val profileMikrotik: String?,
    @SerializedName("wilayah") val wilayah: String
)
