package com.linkbit.billrt

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.linkbit.billrt.adapter.NotificationAdapter
import com.linkbit.billrt.adapter.PembayaranHariIniAdapter
import com.linkbit.billrt.api.RetrofitClient
import com.linkbit.billrt.databinding.FragmentBerandaBinding
import com.linkbit.billrt.NotificationItem
import com.linkbit.billrt.model.PembayaranHariIni
import com.linkbit.billrt.model.PembayaranHariIniResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BerandaFragment : BaseFragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationAdapter: NotificationAdapter

    private lateinit var pembayaranHariIniAdapter: PembayaranHariIniAdapter
    private val pembayaranHariIniList = mutableListOf<PembayaranHariIni>()

    private val notificationsRef = FirebaseDatabase.getInstance("https://mikrotik-alert-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("logs/notifications")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isUserLoggedIn()) {
            findNavController().navigate(R.id.action_berandaFragment_to_loginFragment)
            return
        }

        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.mainContentScrollview.visibility = View.GONE

        setupWelcomeMessage()
        setupDateTime()
        setupCardListeners()
        setupNotificationRecyclerView()
        setupPembayaranHariIniRecyclerView()
        listenForLatestNotifications()
        fetchPembayaranHariIni()
    }

    private fun setupWelcomeMessage() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("USER_NAME", "Pengguna")
        binding.tvWelcome.text = "Selamat Datang, $username!"
    }

    private fun setupDateTime() {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        val currentDate = sdf.format(Date())
        binding.tvDateTime.text = currentDate
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("USER_ID", -1) != -1
    }

    private fun setupCardListeners() {
        val navController = findNavController()
        binding.apply {
            cardUser.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_menuPelangganFragment) }
            cardMikrotik.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_mikrotikAccountsFragment) }
            cardMap.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_mapMenuFragment) }
            cardOlt.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_smartOltMonitorFragment) }
            cardPembukuan.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_pembukuanFragment) }
            btnLihatSemuaNotifikasi.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_riwayatNotifikasiFragment) }
        }
    }

    private fun setupNotificationRecyclerView() {
        notificationAdapter = NotificationAdapter()
        binding.rvNotifikasiBeranda.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
            isNestedScrollingEnabled = true
        }
    }

    private fun setupPembayaranHariIniRecyclerView() {
        pembayaranHariIniAdapter = PembayaranHariIniAdapter(pembayaranHariIniList)
        binding.rvPembayaranHariIni.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pembayaranHariIniAdapter
            isNestedScrollingEnabled = true
        }
    }

    private fun listenForLatestNotifications() {
        notificationsRef.limitToLast(10).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return

                val tempList = mutableListOf<NotificationItem>()
                for (child in snapshot.children) {
                    val item = child.getValue(NotificationItem::class.java)
                    item?.let { tempList.add(it) }
                }

                notificationAdapter.submitList(tempList.reversed())

                binding.loadingProgressBar.visibility = View.GONE
                binding.mainContentScrollview.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                if (_binding != null) {
                    Log.w("BerandaFragment", "Gagal membaca notifikasi.", error.toException())
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.mainContentScrollview.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun fetchPembayaranHariIni() {
        RetrofitClient.instance.getPembayaranHariIni().enqueue(object : Callback<PembayaranHariIniResponse> {
            override fun onResponse(call: Call<PembayaranHariIniResponse>, response: Response<PembayaranHariIniResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.status) {
                            pembayaranHariIniList.clear()
                            pembayaranHariIniList.addAll(it.data)
                            pembayaranHariIniAdapter.notifyDataSetChanged()
                        } else {
                            if(isAdded && _binding != null) {
                                Toast.makeText(context, "Gagal memuat data pembayaran", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    if(isAdded && _binding != null) {
                        Toast.makeText(context, "Gagal memuat data pembayaran", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<PembayaranHariIniResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
