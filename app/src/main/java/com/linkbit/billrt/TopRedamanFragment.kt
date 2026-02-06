package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentTopRedamanBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopRedamanFragment : BaseFragment() {

    private var _binding: FragmentTopRedamanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopRedamanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.navBarSpacer.layoutParams.height = systemBars.bottom
            insets
        }

        binding.rvTopRedaman.layoutManager = LinearLayoutManager(context)

        binding.btnToDetailOlt.setOnClickListener {
            findNavController().navigate(R.id.action_topRedamanFragment_to_pencarianCepatFragment)
        }

        binding.swipeRefreshTopRedaman.setOnRefreshListener {
            fetchTopRedaman()
        }

        fetchTopRedaman()
    }

    private fun fetchTopRedaman() {
        binding.swipeRefreshTopRedaman.isRefreshing = true
        binding.progressBar.visibility = View.VISIBLE
        apiService.getOltData().enqueue(object : Callback<OltApiResponse> {
            override fun onResponse(call: Call<OltApiResponse>, response: Response<OltApiResponse>) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshTopRedaman.isRefreshing = false

                if (response.isSuccessful) {
                    val topRedaman = response.body()?.top10RedamanUp
                    if (topRedaman != null && topRedaman.isNotEmpty()) {
                        binding.rvTopRedaman.adapter = TopRedamanAdapter(topRedaman)
                    } else {
                        Toast.makeText(context, "Tidak ada data redaman", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OltApiResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshTopRedaman.isRefreshing = false
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}