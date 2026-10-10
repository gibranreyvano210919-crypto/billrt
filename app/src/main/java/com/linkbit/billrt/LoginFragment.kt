package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : BaseFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // 1. Sesuaikan padding toolbar agar tidak tertutup status bar
        applyWindowInsets(binding.toolbar)

        // 2. Buat ikon status bar menjadi gelap (agar terlihat di background putih)
        val window = requireActivity().window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.isAppearanceLightStatusBars = true

        if (sessionManager.getUserId() != null) {
            navigateToBeranda()
            return
        }

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
        binding.btnLogin.isEnabled = false
        
        val request = LoginRequest(username, password)
        apiService.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (_binding == null) return
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                
                val loginResponse = response.body()
                if (response.isSuccessful && loginResponse?.success == true) {
                    val userData = loginResponse.user
                    if (userData != null) {
                        sessionManager.saveUser(userData)
                        navigateToBeranda()
                    }
                } else {
                    val errorMsg = loginResponse?.message ?: "Login Gagal"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                showNetworkErrorDialog(requireContext(), t) {
                    loginUser(username, password)
                }
            }
        })
    }

    private fun navigateToBeranda() {
        // Kembalikan ikon status bar ke warna terang jika beranda memiliki background gelap
        val window = requireActivity().window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.isAppearanceLightStatusBars = false
        
        findNavController().navigate(R.id.action_loginFragment_to_berandaFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
