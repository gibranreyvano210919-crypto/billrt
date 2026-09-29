package com.linkbit.billrt

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.io.Serializable

// --- SATU-SATUNYA SUMBER KEBENARAN UNTUK SEMUA MODEL DATA ---

// region Base & User
data class StandardResponse(val status: Boolean, val message: String)
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(
    @SerializedName("status") val success: Boolean,
    val message: String,
    @SerializedName("data") val user: UserData?
)
data class UserData(
    @SerializedName("id_user") val idUser: Int,
    val username: String,
    @SerializedName("nama_lengkap") val nama: String,
    val level: Int?,
    val status: String?,
    @SerializedName("teknisi_id") val teknisiId: Int?,
    val token: String?
) : Serializable
// endregion

// region Model untuk API wilayah_pelanggan_nested
data class WilayahPelangganNestedResponse(
    val status: Boolean,
    @SerializedName("total_wilayah")
    val totalWilayah: Int,
    val data: List<WilayahData>,
    val message: String? // Ditambahkan untuk pesan error
)

data class WilayahData(
    val id: Int,
    @SerializedName("nama_wilayah")
    val namaWilayah: String,
    val statistik: StatistikData,
    @SerializedName("daftar_pelanggan")
    val daftarPelanggan: List<PelangganItem>
)

data class StatistikData(
    @SerializedName("total_pelanggan_aktif")
    val totalPelangganAktif: Int
)

// Mewakili pelanggan dari daftar nested
data class PelangganItem(
    val id: String,
    @SerializedName("nama_pelanggan")
    val nama: String,
    val lat: Double?,
    @SerializedName("long")
    val lng: Double?
)

// Kelas data terpadu untuk tampilan peta dan adapter.
data class PelangganMapData(
    val id: String,
    val nama: String,
    val lat: Double?,
    val lng: Double?,
    val namaWilayah: String,
    val alamat: String? // Nullable karena tidak ada dalam respons API baru
)
// endregion

// region Pelanggan (from various APIs)

// Model untuk data_pelanggan_list
data class PelangganListResponse(val status: Boolean, val message: String?, val data: List<PelangganListItem>)

data class PelangganListItem(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("mikrotik_username") val mikrotikUsername: String?,
    @SerializedName("status_aktif") val statusAktif: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("mac_address") val macAddress: String?,
    @SerializedName("installation_date") val installationDate: String?,
    @SerializedName("tgl_daftar") val tglDaftar: String?
) : Serializable

data class PelangganResponse(
    val status: Boolean, 
    val data: List<PelangganData>?, 
    val rekap: RekapData?,
    @SerializedName("rekap_wilayah") val rekapWilayah: List<RekapWilayahItem>? = null,
    @SerializedName("summary_wilayah") val summaryWilayah: List<SummaryWilayahItem>? = null
)

data class RekapWilayahItem(
    val wilayah: String,
    val jumlah: Int
) : Serializable

data class SummaryWilayahItem(
    @SerializedName("id_wilayah") val idWilayah: String,
    @SerializedName("nama_wilayah") val namaWilayah: String,
    val total: Int
) : Serializable

data class PembayaranTerakhirData(
    @SerializedName("tanggal_bayar") val tanggalBayar: String?,
    @SerializedName("jumlah_bayar") val jumlahBayar: Any?,
    @SerializedName("metode_bayar") val metodeBayar: String?,
    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("nama_pencatat") val namaPencatat: String?,
    @SerializedName("periode") val periode: String?,
    @SerializedName("detail_periode") val detailPeriode: DetailPeriodeData?
) : Serializable {
    fun getJumlahBayarDouble(): Double {
        return when (jumlahBayar) {
            is Number -> jumlahBayar.toDouble()
            is String -> jumlahBayar.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }
}

data class DetailPeriodeData(
    @SerializedName("bulan_tagihan") val bulanTagihan: Int?,
    @SerializedName("nama_bulan") val namaBulan: String?,
    @SerializedName("tahun_tagihan") val tahunTagihan: String?,
    @SerializedName("periode_format") val periodeFormat: String?
) : Serializable

data class PelangganData(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val nama: String,
    @SerializedName("alamat_pelanggan") val alamat: String?,
    @SerializedName("telepon_pelanggan") val telepon: String?,
    @SerializedName("tgl_daftar") val tglDaftar: String?,
    @SerializedName("nama_wilayah") val namaWilayah: String?,
    @SerializedName("mikrotik_username") val mikrotikUsername: String?,
    @SerializedName("mikrotik_password") val mikrotikPassword: String?,
    @SerializedName("static_ip") val staticIp: String?,
    @SerializedName("mac_address") val macAddress: String?,
    @SerializedName(value = "latitude", alternate = ["lat"]) val latitude: Double?,
    @SerializedName(value = "longitude", alternate = ["lng"]) val longitude: Double?,
    @SerializedName("status_aktif") val statusAktif: String?,
    var isTercatat: Boolean = false, // Properti baru untuk menandai
    @SerializedName("id_paket") val idPaket: Int?,
    @SerializedName("id_wilayah") val idWilayah: Int?,
    @SerializedName("installation_date") val installationDate: String?,
    @SerializedName("tgl_expired") val tglExpired: String?,
    @SerializedName("tgl_bayar_terakhir") val tglBayarTerakhir: String?,
    @SerializedName("pembayaran_terakhir") val pembayaranTerakhir: PembayaranTerakhirData? = null
) : Serializable

data class InputKasPelanggan(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val nama: String,
    @SerializedName("nama_wilayah") val wilayah: String?,
    val status: String?,
    @SerializedName("mac_address") val macAddress: String?,
    var isTercatat: Boolean = false,
    val mikrotikUsername: String?
) : Serializable

data class GetPelangganResponse(
    val status: Boolean,
    val total: Int? = null, // Nullable for single item response
    val message: String,
    val data: JsonElement, // Flexible data type
    val config: MapConfig? = null
)

// Model baru untuk response dari detail_pelanggan_map
data class PelangganDetailMapResponse(
    val status: Boolean,
    val data: PelangganDetailMap?,
    val message: String?
)

data class PelangganDetailMap(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("alamat_pelanggan") val alamatPelanggan: String,
    val latitude: String?,
    val longitude: String?,
    @SerializedName("status_aktif") val statusAktif: String,
    @SerializedName("nama_paket") val namaPaket: String,
    @SerializedName("nama_wilayah") val namaWilayah: String
)


data class MapConfig(
    @SerializedName("mapbox_token") val mapboxToken: String,
    val center: List<Double>,
    val zoom: Int
)


data class RekapData(
    @SerializedName("total_pelanggan") val totalPelanggan: Int,
    @SerializedName("aktif") val aktif: Int,
    @SerializedName("isolir") val isolir: Int,
    @SerializedName("nonaktif") val nonaktif: Int,
    @SerializedName("pelanggan_baru") val pelangganBaru: Int,
    @SerializedName("total_lunas") val totalLunas: Int,
    @SerializedName("total_belum_bayar") val totalBelumBayar: Int,
    @SerializedName("total_pendapatan") val totalPendapatan: Float
)

data class UpdateLokasiRequest(
    @SerializedName("id_pelanggan") val idPelanggan: Int, 
    val latitude: Double,
    val longitude: Double
)

data class UpdateStatusRequest(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("status_aktif") val statusAktif: String
)

data class SimpanPelangganRequest(
    @SerializedName("id_pelanggan") val idPelanggan: String? = null,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("alamat_pelanggan") val alamatPelanggan: String,
    @SerializedName("telepon_pelanggan") val teleponPelanggan: String?,
    @SerializedName("id_paket") val idPaket: Int,
    @SerializedName("id_wilayah") val idWilayah: Int,
    @SerializedName("id_router") val idRouter: Int? = null,
    @SerializedName("mikrotik_username") val mikrotikUsername: String,
    @SerializedName("mikrotik_password") val mikrotikPassword: String?,
    @SerializedName("mac_address") val macAddress: String?,
    @SerializedName("local_ip") val localIp: String? = null,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("tgl_daftar") val tglDaftar: String?,
    @SerializedName("tgl_expired") val tglExpired: String? = null,
    @SerializedName("installation_date") val installationDate: String?,
    @SerializedName("id_user") val idUser: Int? = null
)

data class TambahPelangganResponse(
    val status: Boolean,
    val message: String,
    @SerializedName("mikrotik_sync") val mikrotikSync: MikrotikSyncInfo?,
    val data: TambahPelangganData?
) : Serializable

data class MikrotikSyncInfo(
    val synced: Boolean,
    @SerializedName("status_code") val statusCode: String?,
    val message: String?,
    val detail: JsonElement?
) : Serializable

data class TambahPelangganData(
    @SerializedName("id_pelanggan") val idPelanggan: Int,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("mikrotik_user") val mikrotikUser: String?,
    @SerializedName("remote_ip") val remoteIp: String?,
    val router: String?
) : Serializable

data class PelangganIdRequest(val id_pelanggan: String)

data class PelangganDetailResponse(
    val status: Boolean,
    val message: String,
    val data: PelangganDetail?
) : Serializable

data class PelangganDetail(
    val id: Int,
    val nama: String,
    val alamat: String,
    val telp: String,
    val paket: String,
    val lat: Double,
    val lng: Double,
    val status: String,
    @SerializedName("tanggal_pasang") val tanggalPasang: String,
    val keterangan: String,
    val color: String
) : Serializable

data class UpdateTeleponRequest(
    @SerializedName("telepon_pelanggan") val teleponPelanggan: String
) : Serializable

// endregion

// region ODP
data class OdpResponse(val status: Boolean, val data: List<OdpData>)
data class OdpData(
    val id: Int,
    @SerializedName("nama_odp") val namaOdp: String,
    val lokasi: String?,
    @SerializedName(value="latitude", alternate=["odp_lat"]) val latitude: Double?,
    @SerializedName(value="longitude", alternate=["odp_lng"]) val longitude: Double?,
    @SerializedName("total_ports") val totalPorts: Int?,
    @SerializedName("available_ports") val availablePorts: Int?,
    @SerializedName("list_ports") val listPorts: List<OdpPortDetail>?
) : Serializable

data class OdpPortResponse(val status: Boolean, val data: List<OdpPort>)

data class OdpDetailResponse(val status: Boolean, val data: OdpDetailData?, val message: String?)

data class OdpDetailData(
    val id: Int,
    @SerializedName("nama_odp") val namaOdp: String,
    val lokasi: String?,
    @SerializedName("odp_lat") val odpLat: Double?,
    @SerializedName("odp_lng") val odpLng: Double?,
    @SerializedName("list_ports") val listPorts: List<OdpPortWithCabling>
) : Serializable

data class OdpPortWithCabling(
    @SerializedName("port_id") val portId: Int,
    @SerializedName("port_number") val portNumber: Int,
    val status: String?,
    @SerializedName("connected_customer_id") val customerId: String?,
    @SerializedName("nama_pelanggan") val namaPelanggan: String?,
    @SerializedName("cust_lat") val custLat: Double?,
    @SerializedName("cust_lng") val custLng: Double?,
    @SerializedName("signal_rx") val signalRx: String?,
    @SerializedName("jalur_kabel") val jalurKabel: String?
) : Serializable

data class SimpanJalurKabelRequest(
    @SerializedName("odp_id") val odpId: Int,
    @SerializedName("port_number") val portNumber: Int,
    @SerializedName("jalur_kabel") val jalurKabel: String
)

data class HapusJalurKabelRequest(
    @SerializedName("odp_id") val odpId: Int,
    @SerializedName("port_number") val portNumber: Int
)

data class CabutLayananPortRequest(
    @SerializedName("odp_id") val odpId: Int,
    @SerializedName("port_number") val portNumber: Int
)

data class OdpPort(
    val id: Int,
    @SerializedName("odp_id") val odpId: Int,
    @SerializedName("port_number") val portNumber: Int,
    @SerializedName("connected_customer_id") val customerId: String?,
    @SerializedName("nama_pelanggan") val namaPelanggan: String?,
    val status: String?,
    @SerializedName("signal_rx") val signalRx: String?,
    val technician: String?
) : Serializable

data class OdpPortDetail(
    @SerializedName("port_number") val portNumber: Int,
    val status: String?,
    @SerializedName("connected_customer_id") val customerId: String?,
    @SerializedName("nama_pelanggan") val namaPelanggan: String?,
    @SerializedName("cust_lat") val custLat: Double?,
    @SerializedName("cust_lng") val custLng: Double?,
    @SerializedName("signal_rx") val signalRx: String?
) : Serializable

data class OdpLokasiPelangganResponse(val status: Boolean, val data: List<OdpData>)

// endregion

// region Master Data (from index.php)
data class WilayahResponse(val status: Boolean, val data: List<Wilayah>)
data class Wilayah(@SerializedName("id_wilayah") val id_wilayah: Int, @SerializedName("nama_wilayah") val nama_wilayah: String) : Serializable
data class PaketResponse(val status: Boolean, val data: List<Paket>)
data class Paket(
    @SerializedName("id_paket") val id_paket: Int,
    @SerializedName("nama_paket") val nama_paket: String,
    val harga: Float,
    @SerializedName("harga_format") val hargaFormat: String?,
    val kecepatan: String?
) : Serializable
data class SimpanPaketRequest(
    val id_paket: Int?,
    val nama_paket: String,
    val harga: Float,
    val kecepatan: String?
)
data class HapusPaketRequest(val id_paket: Int)
data class SimpanWilayahRequest(val id_wilayah: Int?, val nama_wilayah: String)
data class HapusWilayahRequest(val id_wilayah: Int)
data class MasterTeknisiResponse(val status: Boolean, val data: List<MasterTeknisi>)
data class MasterTeknisi(@SerializedName("id_teknisi") val id: Int, @SerializedName("nama_teknisi") val namaTeknisi: String, val status: String)

// endregion

// region Kas & Catatan (from index.php)
data class InputCatatanRequest(
    @SerializedName("pelanggan_list") val pelangganList: List<String>,
    @SerializedName("id_teknisi") val idTeknisi: String,
    @SerializedName("tanggal_catat") val tanggalCatat: String
)
data class HapusCatatanRequest(val id: Int)
data class VerifyCatatanRequest(val id: Int)
data class UnverifyCatatanRequest(val id: Int)

data class CatatanTagihanResponse(val status: Boolean, val data: List<TanggalGroup>?)

data class GroupedKasResponse(
    val status: Boolean,
    @SerializedName("total_global") val totalGlobal: Int,
    @SerializedName("rekap_global") val rekapGlobal: List<RekapTeknisi>,
    val data: List<TanggalGroup>
)
data class TanggalGroup(
    @SerializedName("id_setoran") val idSetoran: Int,
    @SerializedName("nama_setoran") val namaSetoran: String?,
    @SerializedName("tanggal_catat") val tanggalCatat: String,
    @SerializedName("nama_teknisi") val namaTeknisi: String?,
    @SerializedName("total_nominal") val totalNominal: Int,
    @SerializedName("total_item") val totalItem: Int,
    val list: List<CatatanKasItem>
) : Serializable
data class CatatanKasItem(
    val id: Int, 
    @SerializedName("id_pelanggan") val idPelanggan: String?,
    @SerializedName("nama_pelanggan") val namaPelanggan: String, 
    @SerializedName("mikrotik_username") val mikrotikUsername: String?,
    val wilayah: String?, 
    @SerializedName("nama_teknisi") val namaTeknisi: String?,
    val verified: Int,
    @SerializedName("is_duplicate") val isDuplicate: Boolean = false,
    var tanggalCatat: String = "",
    var namaSetoran: String = ""
) : Serializable
data class RekapTeknisi(val nama: String, val jumlah: Int)

// endregion


// region Riwayat Catatan
data class RiwayatCatatanResponse(
    val status: Boolean,
    @SerializedName("total_global") val totalGlobal: Int,
    @SerializedName("total_duplikat_global") val totalDuplikatGlobal: Int,
    @SerializedName("rekap_global") val rekapGlobal: List<RekapTeknisi>,
    val data: List<TanggalGroup>,
    val message: String? = null
)

// endregion

// region Laporan (from index.php)
data class RekapTunggakanResponse(
    val status: Boolean,
    @SerializedName("total_tunggakan") val totalTunggakan: Int,
    val data: List<RekapTunggakanWilayah>
)
data class RekapTunggakanWilayah(
    @SerializedName("nama_wilayah") val nama_wilayah: String,
    val jumlah: Int,
    val pelanggan: List<RekapTunggakanPelanggan>
) : Serializable
data class RekapTunggakanPelanggan(
    @SerializedName("id_pelanggan") val id_pelanggan: String,
    @SerializedName("nama_pelanggan") val nama_pelanggan: String,
    @SerializedName("mikrotik_username") val mikrotik_username: String?
) : Serializable
data class RekapTercatatResponse(val status: Boolean, @SerializedName("total_tercatat") val totalTercatat: Int, val periode: String, val data: List<RekapTercatatMingguan>) : Serializable
data class RekapTercatatMingguan(val minggu: String, val rentang: String, @SerializedName("data_wilayah") val dataWilayah: List<RekapTercatatWilayah>) : Serializable
data class RekapTercatatWilayah(val nama_wilayah: String, val jumlah: Int, val pelanggan: List<RekapTercatatPelanggan>) : Serializable
data class RekapTercatatPelanggan(val id_pelanggan: String, val nama_pelanggan: String, val mikrotik_username: String, val tanggal_catat: String) : Serializable
data class DetailWilayah(val namaWilayah: String, val lunas: Int, val total: Int) : Serializable

// endregion

// region Mikrotik & OLT
data class OltApiResponse(
    @SerializedName("tabel_rekap_global") val rekapGlobal: RekapGlobal?,
    @SerializedName("tabel_detail_olt") val detailOlt: List<OltDetail>?,
    @SerializedName("tabel_top_10_redaman_up") val top10RedamanUp: List<TopRedamanItem>?
)
data class RekapGlobal(@SerializedName("total_online") val totalOnline: Int, @SerializedName("total_offline") val totalOffline: Int)
data class OltDetail(val olt_name: String, val address: String, val status: String, val summary: OltSummary?, val onus: List<Map<String, String>>?)
data class OltSummary(val total: Int?, val online: Int?, val offline: Int?)

// Model for Top Redaman page
data class TopRedamanItem(
    val olt: String?,
    val onu: String?,
    val mac: String?,
    val signal: Double?,
    val status: String?
)

// Model for Smart OLT flattened list
data class SmartOnuItem(
    val oltName: String,
    val onuIndex: String?,      // col_0
    var name: String?,          // col_1 (ONU Description)
    val sn: String?,            // col_2
    val macAddress: String?,    // also col_2
    val status: String?,        // col_3
    val power: String?,         // col_11
    var customerName: String? = null, // From Database
    var idPelanggan: String? = null   // From Database
)

// NEW High Speed OLT Monitoring Models
data class HighSpeedOltResponse(
    val status: Boolean,
    @SerializedName("total_online") val totalOnline: Int,
    @SerializedName("total_offline") val totalOffline: Int,
    @SerializedName("lama_proses") val lamaProses: String,
    val results: List<HighSpeedOltResult>,
    val message: String? = null
)

data class HighSpeedOltResult(
    @SerializedName("olt_host") val oltHost: String,
    val onus: List<HighSpeedOnuItem>
)

data class HighSpeedOnuItem(
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("mac_address") val macAddress: String,
    @SerializedName("status") val status: String,
    @SerializedName("signal_rx") val signalRx: String
)

data class OltResponse(
    val status: Boolean,
    val results: List<OltAccount>
) : Serializable

data class OltAccount(
    @SerializedName(value = "id", alternate = ["id_olt", "olt_id"]) val id: String?,
    @SerializedName("nama_olt") val namaOlt: String,
    @SerializedName("full_address") val fullAddress: String,
    @SerializedName("tipe_label") val tipeLabel: String,
    @SerializedName("last_sync") val lastSync: String,
    @SerializedName("keterangan") val keterangan: String,
    @SerializedName("status") val status: String
) : Serializable

data class OltDetailResponse(
    val status: Boolean,
    @SerializedName("info_olt") val infoOlt: InfoOlt,
    @SerializedName("total_onu") val totalOnu: Int,
    val results: List<OnuItemDetail>
) : Serializable

data class InfoOlt(
    val id: String,
    val nama: String,
    val host: String
) : Serializable

data class OnuItemDetail(
    @SerializedName("pon_port") val ponPort: String,
    val mac: String,
    val nama: String,
    val status: String,
    val signal: Float,
    val kategori: String
) : Serializable

// --- Model-model untuk API status pelanggan MikroTik ---
data class MikrotikAccountsResponse(
    val status: Boolean,
    val data: List<MikrotikAccount>,
    val message: String? = null // DI TAMBAHKAN
) : Serializable

data class MikrotikAccount(
    val id: Int,
    @SerializedName("router_name") val routerName: String,
    @SerializedName("ip_address") val ipAddress: String
) : Serializable

data class PelangganStatusResponse(
    val status: Boolean,
    val message: String? = null,
    val summary: PelangganSummary?,
    val data: PelangganStatusData?
) : Serializable

data class PelangganSummary(
    @SerializedName("total_secret") val totalSecret: Int,
    val online: Int,
    @SerializedName("logged_out")  val offline: Int,
    val disabled: Int
) : Serializable

data class PelangganStatusData(
    val online: List<PelangganOnline>,
    @SerializedName("logged_out")  val offline: List<PelangganOffline>,
    val disabled: List<PelangganOffline>
) : Serializable

data class PelangganOnline(
    val name: String,
    val profile: String,
    val comment: String,
    val address: String,
    val uptime: String,
    val callerId: String
) : Serializable

data class PelangganOnlineUser(
    @SerializedName(".id") val id: String,
    val name: String,
    val address: String,
    val uptime: String,
    @SerializedName("caller_id") val callerId: String
) : Serializable

data class PelangganOffline(
    val name: String,
    val profile: String,
    val comment: String,
    val status: String?,
    @SerializedName("last_logout") val lastLoggedOut: String?
) : Serializable

// endregion

// region Pembayaran
data class TagihanData(val id_tagihan: String, val periode: String, val nama_paket: String, val harga: Float, val status_bayar: String) : Serializable
// endregion

// region ApiDetailPelanggan

data class ApiDetailPelangganResponse(
    val status: Boolean,
    val data: ApiDetailPelangganData?,
    val message: String?
)

data class ApiDetailPelangganData(
    @SerializedName("card_1_identitas") val card1Identitas: Card1Identitas,
    @SerializedName("card_2_tagihan") val card2Tagihan: Card2Tagihan,
    @SerializedName("card_3_jaringan") val card3Jaringan: Card3Jaringan,
    @SerializedName("card_4_detail") val card4Detail: Card4Detail,
    @SerializedName("card_5_paket") val card5Paket: Card5Paket
)

data class Card1Identitas(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("telepon_pelanggan") val teleponPelanggan: String
)

data class Card2Tagihan(
    @SerializedName("rekap_status") val rekapStatus: String,
    @SerializedName("jumlah_nunggak") val jumlahNunggak: Int
)

data class Card3Jaringan(
    val username: String,
    @SerializedName("status_koneksi") val statusKoneksi: String,
    val uptime: String,
    @SerializedName("ip_aktif") val ipAktif: String,
    @SerializedName("mac_client_live") val macClientLive: String?,
    @SerializedName("mac_address_db") val macAddressDb: String?,
    @SerializedName("total_usage") val totalUsage: String,
    @SerializedName("last_logout") val lastLogout: String
)

data class Card4Detail(
    @SerializedName("tgl_daftar") val tglDaftar: String,
    @SerializedName("installation_date") val installationDate: String,
    @SerializedName("alamat_pelanggan") val alamatPelanggan: String,
    val latitude: String,
    val longitude: String,
    @SerializedName("google_maps_link") val googleMapsLink: String
)

data class Card5Paket(
    @SerializedName("nama_paket") val namaPaket: String,
    @SerializedName("harga_paket") val hargaPaket: String,
    @SerializedName("profile_mikrotik") val profileMikrotik: String?,
    val wilayah: String
)

data class UpdateMacRequest(
    @SerializedName("mac_address") val macAddress: String
)

// endregion
