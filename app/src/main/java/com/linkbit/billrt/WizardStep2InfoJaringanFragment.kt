package com.linkbit.billrt

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.linkbit.billrt.databinding.FragmentWizardStep2InfoJaringanBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WizardStep2InfoJaringanFragment : BaseFragment(), WizardValidator {

    private var _binding: FragmentWizardStep2InfoJaringanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TambahPelangganViewModel by activityViewModels()

    private var paketList = listOf<Paket>()
    private var wilayahList = listOf<Wilayah>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWizardStep2InfoJaringanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etMikrotikUsername.setText(viewModel.mikrotikUsername.value)
        setupListeners()

        fetchPaket()
        fetchWilayah()
    }

    private fun setupListeners() {
        binding.etMikrotikUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.mikrotikUsername.value = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.spinnerPaket.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (paketList.isNotEmpty()) {
                    viewModel.idPaket.value = paketList[position].id_paket
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerWilayah.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (wilayahList.isNotEmpty()) {
                    viewModel.idWilayah.value = wilayahList[position].id_wilayah
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun validate(): String? {
        if (binding.etMikrotikUsername.text.isNullOrBlank()) {
            val errorMessage = "Username Mikrotik tidak boleh kosong"
            binding.etMikrotikUsername.error = errorMessage
            return errorMessage
        }
        binding.etMikrotikUsername.error = null
        return null
    }

    private fun fetchPaket() {
        setLoading(true)
        apiService.getPaket().enqueue(object : Callback<PaketResponse> {
            override fun onResponse(call: Call<PaketResponse>, response: Response<PaketResponse>) {
                if (_binding == null) return // Safety check
                setLoading(false)
                if (response.isSuccessful) {
                    val fetchedData = response.body()?.data
                    if (fetchedData.isNullOrEmpty()) {
                        Toast.makeText(context, "Tidak ada data paket internet ditemukan", Toast.LENGTH_SHORT).show()
                        return
                    }

                    paketList = fetchedData
                    val paketNames = paketList.map { it.nama_paket }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, paketNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerPaket.adapter = adapter

                    // Restore selection from ViewModel
                    viewModel.idPaket.value?.let { savedId ->
                        val position = paketList.indexOfFirst { it.id_paket == savedId }
                        if (position >= 0) {
                            binding.spinnerPaket.setSelection(position)
                        }
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil daftar paket (Error: ${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<PaketResponse>, t: Throwable) {
                if (_binding == null) return // Safety check
                setLoading(false)
                Toast.makeText(context, "Koneksi ke server paket gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun fetchWilayah() {
        setLoading(true)
        apiService.getWilayah().enqueue(object : Callback<WilayahResponse> {
            override fun onResponse(call: Call<WilayahResponse>, response: Response<WilayahResponse>) {
                if (_binding == null) return // Safety check
                setLoading(false)
                if (response.isSuccessful) {
                    val fetchedData = response.body()?.data
                    if (fetchedData.isNullOrEmpty()) {
                        Toast.makeText(context, "Tidak ada data wilayah ditemukan", Toast.LENGTH_SHORT).show()
                        return
                    }

                    wilayahList = fetchedData
                    val wilayahNames = wilayahList.map { it.nama_wilayah }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wilayahNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerWilayah.adapter = adapter

                    // Restore selection from ViewModel
                    viewModel.idWilayah.value?.let { savedId ->
                        val position = wilayahList.indexOfFirst { it.id_wilayah == savedId }
                        if (position >= 0) {
                            binding.spinnerWilayah.setSelection(position)
                        }
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil daftar wilayah (Error: ${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<WilayahResponse>, t: Throwable) {
                if (_binding == null) return // Safety check
                setLoading(false)
                Toast.makeText(context, "Koneksi ke server wilayah gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setLoading(isLoading: Boolean) {
        // Check binding to prevent crash
        if (_binding == null) return

        binding.progressBarStep2.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentLayout.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
