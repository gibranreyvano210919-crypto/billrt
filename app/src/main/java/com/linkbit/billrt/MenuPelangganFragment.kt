package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentMenuPelangganBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuPelangganFragment : BaseFragment() {

    private var _binding: FragmentMenuPelangganBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarMenuPelanggan.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.cardPelangganAktif.setOnClickListener {
            findNavController().navigate(R.id.action_menuPelangganFragment_to_pelangganAktifFragment)
        }

        binding.cardPelangganNonaktif.setOnClickListener {
            findNavController().navigate(R.id.action_menuPelangganFragment_to_pelangganNonaktifFragment)
        }

        binding.cardPelangganIsolir.setOnClickListener {
            findNavController().navigate(R.id.action_menuPelangganFragment_to_pelangganIsolirFragment)
        }

        fetchCounts()
    }

    private fun fetchCounts() {
        // apiService is now inherited from BaseFragment

        // Fetch Aktif Count
        apiService.getDataPelanggan(status = "aktif").enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (_binding == null || !isAdded) return // Safety check
                if (response.isSuccessful) {
                    binding.tvCountAktif.text = response.body()?.data?.size?.toString() ?: "0"
                }
            }
            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return // Safety check
            }
        })

        // Fetch Nonaktif Count
        apiService.getDataPelanggan(status = "nonaktif").enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (_binding == null || !isAdded) return // Safety check
                if (response.isSuccessful) {
                    binding.tvCountNonaktif.text = response.body()?.data?.size?.toString() ?: "0"
                }
            }
            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return // Safety check
            }
        })

        // Fetch Isolir Count
        apiService.getDataPelanggan(status = "isolir").enqueue(object : Callback<PelangganResponse> {
            override fun onResponse(call: Call<PelangganResponse>, response: Response<PelangganResponse>) {
                if (_binding == null || !isAdded) return // Safety check
                if (response.isSuccessful) {
                    binding.tvCountIsolir.text = response.body()?.data?.size?.toString() ?: "0"
                }
            }
            override fun onFailure(call: Call<PelangganResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return // Safety check
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}