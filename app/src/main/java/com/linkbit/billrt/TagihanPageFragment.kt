package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.linkbit.billrt.databinding.FragmentTagihanPageBinding

private const val ARG_TAGIHAN_TYPE = "tagihan_type"

class TagihanPageFragment : Fragment() {

    private var _binding: FragmentTagihanPageBinding? = null
    private val binding get() = _binding!!
    private var tagihanType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tagihanType = it.getString(ARG_TAGIHAN_TYPE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagihanPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // For now, just display the type. In the future, you'll fetch data here.
        binding.tvTagihanContent.text = "Tampilan untuk: ${tagihanType?.replace("_", " ")?.uppercase()}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(tagihanType: String) = TagihanPageFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TAGIHAN_TYPE, tagihanType)
            }
        }
    }
}