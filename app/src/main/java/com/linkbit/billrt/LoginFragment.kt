package com.linkbit.billrt

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : BaseFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Menampilkan versi aplikasi
        val version = BuildConfig.VERSION_NAME
        binding.tvVersion.text = "Versi $version"

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Toast.makeText(context, "Username dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(username: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        val request = LoginRequest(username, password)
        apiService.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (_binding == null) return
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.success == true) {
                    val userData = response.body()?.user
                    if (userData != null) {
                        saveUserSession(userData)
                        // Navigate to BerandaFragment on successful login
                        findNavController().navigate(R.id.action_loginFragment_to_berandaFragment)
                    } else {
                        Toast.makeText(context, "Gagal mendapatkan data pengguna", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Login Gagal: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserSession(userData: UserData) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("USER_ID", userData.idUser)
            putString("USER_NAME", userData.nama)
            putString("USER_USERNAME", userData.username)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
