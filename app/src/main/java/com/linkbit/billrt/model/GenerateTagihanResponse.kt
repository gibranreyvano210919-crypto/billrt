package com.linkbit.billrt.model

data class GenerateTagihanResponse(
    val status: Boolean,
    val message: String,
    val detail: GenerateTagihanDetail? = null
)

data class GenerateTagihanDetail(
    val bulan: Int,
    val tahun: Int
)
