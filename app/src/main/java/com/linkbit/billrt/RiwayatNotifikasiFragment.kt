package com.linkbit.billrt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.linkbit.billrt.adapter.NotificationAdapter
import com.linkbit.billrt.databinding.FragmentRiwayatNotifikasiBinding

class RiwayatNotifikasiFragment : Fragment() {

    private var _binding: FragmentRiwayatNotifikasiBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationAdapter: NotificationAdapter

    // Path yang benar sesuai dengan script Node.js
    private val notificationsRef = FirebaseDatabase.getInstance("https://mikrotik-alert-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("logs/notifications")

    private var listener: ValueEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRiwayatNotifikasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        listenForNotifications()

        binding.swipeRefreshLayout.setOnRefreshListener {
            listenForNotifications()
        }
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter()
        binding.rvNotifikasi.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }
    }

    private fun listenForNotifications() {
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBar.isVisible = true
        }
        binding.tvEmptyNotifikasi.isVisible = false

        listener?.let { notificationsRef.removeEventListener(it) }

        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return

                binding.progressBar.isVisible = false
                binding.swipeRefreshLayout.isRefreshing = false
                val tempList = mutableListOf<NotificationItem>()
                for (child in snapshot.children) {
                    try {
                        val item = child.getValue(NotificationItem::class.java)
                        if (item != null) {
                            tempList.add(item)
                        }
                    } catch (e: Exception) {
                        Log.e("RiwayatNotifikasi", "Gagal parse data: ${e.message}")
                    }
                }

                val reversedList = tempList.reversed()
                notificationAdapter.submitList(reversedList)

                binding.tvEmptyNotifikasi.isVisible = reversedList.isEmpty()
                binding.rvNotifikasi.isVisible = reversedList.isNotEmpty()
            }

            override fun onCancelled(error: DatabaseError) {
                if (_binding != null) {
                    binding.progressBar.isVisible = false
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.tvEmptyNotifikasi.isVisible = true
                    binding.tvEmptyNotifikasi.text = "Error: ${error.message}"
                    Log.w("RiwayatNotifikasi", "onCancelled", error.toException())
                }
            }
        }

        // Mengambil 100 log terakhir
        notificationsRef.limitToLast(100).addValueEventListener(listener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.let { notificationsRef.removeEventListener(it) }
        _binding = null
    }
}
