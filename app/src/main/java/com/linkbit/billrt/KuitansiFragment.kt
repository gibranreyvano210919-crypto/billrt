package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.linkbit.billrt.databinding.FragmentKuitansiBinding
import java.io.Serializable

class KuitansiFragment : Fragment() {

    private var _binding: FragmentKuitansiBinding? = null
    private val binding get() = _binding!!

    private var tagihanData: TagihanData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(ARG_TAGIHAN_DATA)) {
                tagihanData = it.getSerializable(ARG_TAGIHAN_DATA) as TagihanData
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKuitansiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayData()
    }

    private fun displayData() {
        tagihanData?.let {
            // The following lines are causing errors and need to be fixed
            // based on the new data structure in ApiData.kt

            // binding.tvNamaPelanggan.text = it.idPelanggan // Assuming idPelanggan is the name for now
            // binding.tvBulanTahun.text = "Tagihan Bulan: ${it.angka}"
            // binding.tvTotalBayar.text = "Total: Rp ${it.totalBayar}"
            // binding.tvMetodeBayar.text = "Metode: Belum Dibayar"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TAGIHAN_DATA = "tagihan_data"

        fun newInstance(data: Serializable): KuitansiFragment {
            val fragment = KuitansiFragment()
            val args = Bundle()
            if (data is TagihanData) {
                args.putSerializable(ARG_TAGIHAN_DATA, data)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
