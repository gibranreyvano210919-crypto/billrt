package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentDaftarPelangganBinding

class DaftarPelangganFragment : Fragment(), PelangganFilterListener {

    private var _binding: FragmentDaftarPelangganBinding? = null
    private val binding get() = _binding!!

    private lateinit var pelangganAdapter: PelangganAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDaftarPelangganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pelangganAdapter = PelangganAdapter(emptyList()) // Click listener removed
        binding.recyclerViewPelanggan.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pelangganAdapter
        }

        // TODO: Load initial data for the list
    }

    override fun onFilterChanged(month: Int, year: Int) {
        // TODO: Implement your logic to filter the customer list by month and year
    }

    override fun onSearchQuery(query: String?) {
        // TODO: Implement your logic to search the customer list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): DaftarPelangganFragment {
            return DaftarPelangganFragment()
        }
    }
}
