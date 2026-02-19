package com.linkbit.billrt

data class Periode(val bulan_tagihan: Int, val tahun_tagihan: Int)

data class PeriodeResponse(val status: Boolean, val data: List<Periode>)
