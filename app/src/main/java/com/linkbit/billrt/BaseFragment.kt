package com.linkbit.billrt

import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    val apiService: ApiService by lazy { ApiConfig.apiService }
}
