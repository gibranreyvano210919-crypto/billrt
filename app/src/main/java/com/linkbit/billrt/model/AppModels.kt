package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// --- SATU-SATUNYA SUMBER KEBENARAN UNTUK SEMUA MODEL DATA ---

// region Dashboard & Responses
data class DashboardResponse(
    val status: Boolean,
    val data: List<PelangganData>?,
    val rekap: RekapData?
)

data class SearchAutoCompleteResponse(
    val status: Boolean,
    val data: List<AutoCompleteItem>?
)

data class AutoCompleteItem(
    @SerializedName("id") val idPelanggan: String?,
    @SerializedName("nama") val namaPelanggan: String?,
    @SerializedName("mac") val macAddress: String?
) : Serializable

data class RadiusByIdResponse(
    val status: Boolean,
    val message: String?,
    val pusat: PelangganPusat?,
    val tetangga: List<PelangganTetangga>?
) : Serializable

data class PelangganPusat(
    @SerializedName("id") val idPelanggan: String?,
    @SerializedName("nama") val nama: String?,
    @SerializedName("mac") val macAddress: String?,
    @SerializedName("lat") val latitude: Double?,
    @SerializedName("lng") val longitude: Double?,
    @SerializedName("status") val statusAktif: String?
) : Serializable

data class PelangganTetangga(
    @SerializedName("id") val idPelanggan: String?,
    @SerializedName("nama") val nama: String?,
    @SerializedName("mac") val macAddress: String?,
    @SerializedName("lat") val latitude: Double?,
    @SerializedName("lng") val longitude: Double?,
    val jarak: String?,
    @SerializedName("status") val statusAktif: String?
) : Serializable

data class PelangganTanpaLokasiResponse(
    val status: Boolean,
    val data: List<PelangganTanpaLokasi>?
) : Serializable

data class PelangganTanpaLokasi(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    val nama: String,
    val alamat: String
) : Serializable

data class SaveLocationResponse(val status: Boolean, val message: String)

data class SemuaPelangganResponse(
    val status: Boolean,
    val data_pelanggan_list: List<SemuaPelanggan>?
) : Serializable

data class SemuaPelanggan(
    @SerializedName("id_pelanggan") val idPelanggan: String?,
    val nama: String?,
    val alamat: String?
) : Serializable
// endregion

// region Pelanggan & Requests
data class PelangganData(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val nama: String,
    @SerializedName("nama_wilayah") val wilayah: String,
    val status: String?,
    @SerializedName("mac_address") val macAddress: String?,
    @SerializedName("mikrotik_username") val mikrotikUsername: String?,
    var isTercatat: Boolean = false
) : Serializable

data class RekapData(
    @SerializedName("total_pelanggan") val totalPelanggan: Int,
    @SerializedName("pelanggan_baru") val pelangganBaru: Int,
    @SerializedName("total_lunas") val totalLunas: Int,
    @SerializedName("total_belum_bayar") val totalBelumBayar: Int
)

data class SaveLocationRequest(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    val latitude: String,
    val longitude: String,
    val alamat: String,
    @SerializedName("image_url") val imageUrl: String
) : Serializable

data class InputCatatanRequest(
    @SerializedName("pelanggan_list") val pelangganList: List<String>,
    @SerializedName("id_teknisi") val idTeknisi: String,
    @SerializedName("id_teknisi_collection") val idTeknisiCollection: String? = null,
    @SerializedName("id_setoran") val idSetoran: Int? = null,
    @SerializedName("nama_setoran") val namaSetoran: String? = null,
    @SerializedName("tanggal_catat") val tanggalCatat: String,
    @SerializedName("bulan") val bulan: String? = null,
    @SerializedName("tahun") val tahun: Int? = null,
    @SerializedName("nominal") val nominal: Int? = null,
    @SerializedName("keterangan") val keterangan: String? = null,
    @SerializedName("verified") val verified: Int? = 0
) : Serializable
// endregion

// region Mikrotik & OLT
data class MikrotikAccountsResponse(
    val status: Boolean,
    val message: String,
    val data: List<MikrotikAccount>
) : Serializable

data class MikrotikAccountDetailResponse(
    val status: Boolean,
    val data: MikrotikAccount
) : Serializable

data class MikrotikAccount(
    val id: Int,
    @SerializedName("router_name") val routerName: String,
    @SerializedName("ip_address") val ipAddress: String,
    val username: String,
    val password: String? = null,
    val port: Int,
    @SerializedName("owner_id") val ownerId: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null
) : Serializable

data class StandardResponse(val status: Boolean, val message: String) : Serializable

data class MasterTeknisiResponse(val status: Boolean, val data: List<MasterTeknisi>)

data class MasterTeknisi(@SerializedName("id_teknisi") val id: Int, @SerializedName("nama_teknisi") val namaTeknisi: String, val status: String)

data class PppoeOfflineResponse(
    val status: Boolean,
    val data: List<PppoeOfflineUser>,
    val message: String?
) : Serializable

data class PppoeOfflineUser(
    @SerializedName(".id") val id: String,
    val name: String,
    val profile: String,
    val disabled: String,
    val comment: String?,
    @SerializedName("last_logged_out") val lastLoggedOut: String?
) : Serializable

data class PppoeOnlineResponse(
    val status: Boolean,
    val data: List<PppoeOnlineUser>,
    val message: String?
) : Serializable

data class PppoeOnlineUser(
    @SerializedName(".id") val id: String,
    val name: String,
    val address: String,
    val uptime: String,
    @SerializedName("caller_id") val callerId: String
) : Serializable

data class MikrotikBillingResponse(
    val status: Boolean,
    val message: String,
    @SerializedName("total_data") val totalData: Int,
    val data: List<MikrotikBillingItem>
) : Serializable

data class MikrotikBillingItem(
    @SerializedName("no_urut") val noUrut: Int,
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("telepon_pelanggan") val teleponPelanggan: String,
    @SerializedName("mikrotik_username") val mikrotikUsername: String,
    @SerializedName("mikrotik_password") val mikrotikPassword: String,
    @SerializedName("router_name") val routerName: String,
    @SerializedName("tgl_expired") val tglExpired: String,
    @SerializedName("status_aktif") val statusAktif: String,
    @SerializedName("mikrotik_disabled") val mikrotikDisabled: String,
    @SerializedName("mac_address") val macAddress: String,
    @SerializedName("is_online") val isOnline: Boolean,
    @SerializedName("is_disabled") val isDisabled: Boolean
) : Serializable

// endregion

// region Setoran
data class RiwayatSetoranResponse(
    val status: Boolean,
    val data: List<SetoranItem>
) : Serializable

data class SetoranItem(
    @SerializedName("id_setoran") val idSetoran: Int,
    @SerializedName("id_teknisi") val idTeknisi: String,
    @SerializedName("nama_teknisi") val namaTeknisi: String?,
    @SerializedName("nominal_setor") val nominalSetor: Float,
    @SerializedName("url_cloudinary") val urlCloudinary: String,
    @SerializedName("tgl_setoran") val tglSetoran: String,
    @SerializedName("waktu_input") val waktuInput: String,
    @SerializedName("catatan") val catatan: String
) : Serializable

data class TambahSetoranRequest(
    @SerializedName("id_teknisi") val idTeknisi: String,
    @SerializedName("nominal_setor") val nominalSetor: Float,
    @SerializedName("url_cloudinary") val urlCloudinary: String,
    @SerializedName("tgl_setoran") val tglSetoran: String,
    @SerializedName("catatan") val catatan: String
) : Serializable
// endregion

// region Riwayat Redaman
data class RiwayatRedamanItem(
    val signal: String,
    val timestamp: String
) : Serializable
// endregion

// region Catatan Tagihan
data class CatatanTagihanResponse(
    val status: Boolean,
    val data: List<CatatanTagihanGroup>?
) : Serializable

data class CatatanTagihanGroup(
    val tanggal: String,
    val list: List<CatatanTagihanItem>
) : Serializable

data class CatatanTagihanItem(
    val id: Int,
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val namaPelanggan: String?,
    val nominal: Int?,
    val keterangan: String?,
    @SerializedName("id_setoran") val idSetoran: Int?,
    @SerializedName("nama_setoran") val namaSetoran: String?,
    val bulan: String?,
    val tahun: Int?,
    val verified: Int?,
    @SerializedName("nama_teknisi") val namaTeknisi: String? = null
) : Serializable
// endregion

// region Nota Data (Receipt Response)
data class NotaDataResponse(
    val status: Boolean,
    val message: String? = null,
    val data: NotaDataContainer? = null
) : Serializable

data class NotaDataContainer(
    val periode: String? = null,
    @SerializedName("nama_pencatat") val namaPencatat: String? = null,
    @SerializedName("jumlah_bayar") val jumlahBayar: Float? = null,
    val nota: NotaData? = null
) : Serializable

data class NotaData(
    @SerializedName("no_nota") val noNota: String? = null,
    @SerializedName("tanggal_bayar") val tanggalBayar: String? = null,
    @SerializedName("metode_bayar") val metodeBayar: String? = null,
    val keterangan: String? = null,
    val wilayah: NotaWilayah? = null,
    @SerializedName("periode_tagihan") val periodeTagihan: NotaPeriodeTagihan? = null,
    val perusahaan: NotaPerusahaan? = null,
    val pelanggan: NotaPelanggan? = null,
    @SerializedName("rincian_item") val rincianItem: List<NotaRincianItem>? = null,
    @SerializedName("total_bayar") val totalBayar: Float? = null,
    val kasir: String? = null
) : Serializable

data class NotaWilayah(
    @SerializedName("id_wilayah") val idWilayah: Any? = null,
    @SerializedName("nama_wilayah") val namaWilayah: String? = null
) : Serializable

data class NotaPeriodeTagihan(
    @SerializedName("bulan_tagihan") val bulanTagihan: Int? = null,
    @SerializedName("nama_bulan") val namaBulan: String? = null,
    @SerializedName("tahun_tagihan") val tahunTagihan: Int? = null,
    @SerializedName("periode_format") val periodeFormat: String? = null
) : Serializable

data class NotaPerusahaan(
    @SerializedName("nama_perusahaan") val namaPerusahaan: String? = null,
    @SerializedName("alamat_perusahaan") val alamatPerusahaan: String? = null,
    @SerializedName("telepon_perusahaan") val teleponPerusahaan: String? = null,
    @SerializedName("email_perusahaan") val emailPerusahaan: String? = null,
    @SerializedName("bank_nama") val bankNama: String? = null,
    @SerializedName("bank_atas_nama") val bankAtasNama: String? = null,
    @SerializedName("bank_no_rekening") val bankNoRekening: String? = null,
    @SerializedName("logo_perusahaan") val logoPerusahaan: String? = null
) : Serializable

data class NotaPelanggan(
    @SerializedName("id_pelanggan") val idPelanggan: Any? = null,
    @SerializedName("nama_pelanggan") val namaPelanggan: String? = null,
    @SerializedName("id_wilayah", alternate = ["idWilayah"]) val idWilayah: Any? = null,
    @SerializedName("no_hp", alternate = ["telepon_pelanggan", "telepon"]) val noHp: String? = null,
    @SerializedName("alamat", alternate = ["alamat_pelanggan"]) val alamat: String? = null
) : Serializable

data class NotaRincianItem(
    val deskripsi: String? = null,
    @SerializedName("id_tagihan") val idTagihan: Any? = null,
    @SerializedName("bulan_tagihan") val bulanTagihan: Int? = null,
    @SerializedName("nama_bulan") val namaBulan: String? = null,
    @SerializedName("tahun_tagihan") val tahunTagihan: Int? = null,
    val periode: String? = null,
    val harga: Float? = null
) : Serializable
// endregion
