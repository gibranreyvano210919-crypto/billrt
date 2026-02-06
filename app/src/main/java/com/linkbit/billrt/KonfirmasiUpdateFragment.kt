package com.linkbit.billrt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.linkbit.billrt.databinding.FragmentKonfirmasiUpdateBinding
import com.linkbit.billrt.model.SaveLocationRequest
import com.linkbit.billrt.model.SaveLocationResponse
import com.linkbit.billrt.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KonfirmasiUpdateFragment : Fragment() {

    private var _binding: FragmentKonfirmasiUpdateBinding? = null
    private val binding get() = _binding!!
    private val args: KonfirmasiUpdateFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKonfirmasiUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayData()

        binding.saveButton.setOnClickListener {
            saveDataToServer()
        }
    }

    private fun displayData() {
        binding.namaPelangganTextView.text = "Nama: ${args.namaPelanggan}"
        binding.pelangganIdTextView.text = "ID Pelanggan: ${args.idPelanggan}"
        binding.locationTextView.text = "Lokasi: ${args.latitude}, ${args.longitude}"
        binding.addressTextView.text = "Alamat: ${args.alamat}"
        binding.imageUrlTextView.text = args.imageUrl

        Glide.with(this)
            .load(args.imageUrl)
            .into(binding.imageView)
    }

    private fun saveDataToServer() {
        binding.saveProgressBar.isVisible = true
        binding.saveButton.isEnabled = false

        val request = SaveLocationRequest(
            idPelanggan = args.idPelanggan,
            latitude = args.latitude.toString(),
            longitude = args.longitude.toString(),
            alamat = args.alamat,
            imageUrl = args.imageUrl
        )

        RetrofitClient.instance.saveLocation(request).enqueue(object : Callback<SaveLocationResponse> {
            override fun onResponse(call: Call<SaveLocationResponse>, response: Response<SaveLocationResponse>) {
                binding.saveProgressBar.isVisible = false
                binding.saveButton.isEnabled = true

                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(context, response.body()?.message ?: "Data berhasil disimpan!", Toast.LENGTH_LONG).show()
                    // Kembali ke dua layar sebelumnya (ke daftar pelanggan)
                    findNavController().popBackStack(R.id.gantiLokasiFragment, false)
                } else {
                    val errorMessage = response.body()?.message ?: "Gagal menyimpan data ke server."
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("KonfirmasiUpdate", "API Error: $errorMessage, Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SaveLocationResponse>, t: Throwable) {
                binding.saveProgressBar.isVisible = false
                binding.saveButton.isEnabled = true
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("KonfirmasiUpdate", "Retrofit Failure", t)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
