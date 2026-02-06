package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.linkbit.billrt.databinding.FragmentBulanBinding
import java.io.Serializable

class BulanFragment : Fragment() {

    private var _binding: FragmentBulanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBulanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
        Toast.makeText(context, "Endpoint untuk fitur ini tidak ditemukan di API.", Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID_PELANGGAN = "id_pelanggan"

        fun newInstance(idPelanggan: String) = BulanFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ID_PELANGGAN, idPelanggan)
            }
        }
    }
}