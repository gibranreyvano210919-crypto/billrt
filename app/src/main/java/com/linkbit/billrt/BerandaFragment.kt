package com.linkbit.billrt

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.linkbit.billrt.adapter.NotificationAdapter
import com.linkbit.billrt.databinding.FragmentBerandaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BerandaFragment : BaseFragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationAdapter: NotificationAdapter
    private val notificationList = mutableListOf<NotificationItem>()

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
        listenForLatestNotifications()
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
            cardLaporan.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_riwayatTagihanFragment) }
            cardMikrotik.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_mikrotikAccountsFragment) }
            cardKas.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_kasFragment) }
            cardMap.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_mapMenuFragment) }
            cardRekapTunggakan.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_rekapTunggakanFragment) }
            cardRekapTercatat.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_rekapTercatatFragment) }
            cardOlt.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_topRedamanFragment) }
            btnLihatSemuaNotifikasi.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_riwayatNotifikasiFragment) }
            cardSetoran.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_setoranFragment) }
            cardHistoriCatat.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_historiCatatFragment) }
            cardValidasiPeriode.setOnClickListener { navController.navigate(R.id.action_berandaFragment_to_validasiPeriodeFragment) }
        }
    }

    private fun setupNotificationRecyclerView() {
        notificationAdapter = NotificationAdapter(notificationList)
        binding.rvNotifikasiBeranda.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
            isNestedScrollingEnabled = false
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

                notificationList.clear()
                notificationList.addAll(tempList.reversed())
                notificationAdapter.notifyDataSetChanged()

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
