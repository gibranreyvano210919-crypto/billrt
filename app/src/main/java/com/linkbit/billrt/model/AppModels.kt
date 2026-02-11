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
    @SerializedName("tanggal_catat") val tanggalCatat: String
) : Serializable
// endregion

// region Mikrotik & OLT
data class MikrotikAccountsResponse(
    val status: Boolean,
    val message: String,
    val data: List<MikrotikAccount>
) : Serializable

data class MikrotikAccount(
    val id: Int,
    @SerializedName("router_name") val routerName: String,
    @SerializedName("ip_address") val ipAddress: String,
    val username: String,
    val port: Int,
    @SerializedName("created_at") val createdAt: String
) : Serializable

data class PelangganStatusResponse(
    val status: Boolean,
    val message: String? = null,
    val info: RouterInfo?,
    val summary: PelangganSummary?,
    val data: PelangganStatusData?
) : Serializable

data class RouterInfo(
    @SerializedName("router_name") val routerName: String,
    @SerializedName("uptime") val uptimeRouter: String
) : Serializable

data class PelangganSummary(
    @SerializedName("total_secret") val totalPelanggan: Int,
    val online: Int,
    val offline: Int,
    val disabled: Int
) : Serializable

data class PelangganStatusData(
    val online: List<PelangganOnline>,
    val offline: List<PelangganOffline>,
    val disabled: List<PelangganOffline>
) : Serializable

data class PelangganOnline(
    val name: String, val profile: String, val comment: String, 
    val address: String, val uptime: String, @SerializedName("caller_id") val callerId: String
) : Serializable

data class PelangganOffline(
    val name: String, val profile: String, val comment: String, 
    @SerializedName("last_logged_out") val lastLoggedOut: String
) : Serializable

data class AuditUserResponse(
    val status: Boolean,
    val summary: AuditSummary?,
    val data: List<AuditResult>?
) : Serializable

data class AuditSummary(
    @SerializedName("total_audit_issue") val totalAuditIssue: Int,
    @SerializedName("total_mikrotik") val totalMikrotik: Int,
    @SerializedName("total_database") val totalDatabase: Int
) : Serializable

data class AuditResult(val username: String, val info: String, val status: String) : Serializable

data class StandardResponse(val status: Boolean, val message: String)

data class MasterTeknisiResponse(val status: Boolean, val data: List<MasterTeknisi>)

data class MasterTeknisi(@SerializedName("id_teknisi") val id: Int, @SerializedName("nama_teknisi") val namaTeknisi: String, val status: String)
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
    @SerializedName("id_pelanggan") val idPelanggan: String
) : Serializable
// endregion
