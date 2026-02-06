package com.linkbit.billrt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

class RekapViewModel : ViewModel() {

    private val _month = MutableLiveData<Int>()
    val month: LiveData<Int> = _month

    private val _year = MutableLiveData<Int>()
    val year: LiveData<Int> = _year

    init {
        val calendar = Calendar.getInstance()
        _month.value = calendar.get(Calendar.MONTH) + 1
        _year.value = calendar.get(Calendar.YEAR)
    }

    fun setDate(month: Int, year: Int) {
        _month.value = month
        _year.value = year
    }
}