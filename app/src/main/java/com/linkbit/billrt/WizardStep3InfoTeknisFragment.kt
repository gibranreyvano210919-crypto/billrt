package com.linkbit.billrt

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentWizardStep3InfoTeknisBinding
import com.linkbit.billrt.model.StandardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class WizardStep3InfoTeknisFragment : BaseFragment(), WizardValidator {

    private var _binding: FragmentWizardStep3InfoTeknisBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TambahPelangganViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWizardStep3InfoTeknisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restore data from ViewModel
        binding.etMikrotikPassword.setText(viewModel.mikrotikPassword.value)
        binding.tvInstallationDate.text = viewModel.installationDate.value

        setupListeners()

        binding.btnSimpan.setOnClickListener { 
            savePelanggan()
        }
    }

    private fun setupListeners() {
        binding.etMikrotikPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.mikrotikPassword.value = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnPilihInstallationDate.setOnClickListener { showDatePickerDialog() }
    }

    override fun validate(): String? {
        // Langkah ini tidak memiliki input wajib, jadi selalu dianggap valid.
        return null
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            viewModel.installationDate.value = selectedDate
            binding.tvInstallationDate.text = selectedDate
        }, year, month, day).show()
    }

    private fun savePelanggan() {
        // Sebelum menyimpan, kita validasi sekali lagi semua langkah
        if (!(parentFragment as TambahPelangganWizardFragment).validateAllSteps()) {
             Toast.makeText(context, "Harap lengkapi semua data wajib di setiap langkah.", Toast.LENGTH_SHORT).show()
             return
        }

        val request = viewModel.createSaveRequest()
        if (request == null) {
            Toast.makeText(context, "Harap isi semua field yang wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        apiService.simpanPelanggan(request = request).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(call: Call<StandardResponse>, response: Response<StandardResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, "Pelanggan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, "Gagal menyimpan: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
