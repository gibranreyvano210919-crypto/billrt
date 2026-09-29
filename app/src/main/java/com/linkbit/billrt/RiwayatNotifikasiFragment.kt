package com.linkbit.billrt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.linkbit.billrt.adapter.NotificationAdapter
import com.linkbit.billrt.databinding.FragmentRiwayatNotifikasiBinding

class RiwayatNotifikasiFragment : BaseFragment() {

    private var _binding: FragmentRiwayatNotifikasiBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationAdapter: NotificationAdapter
    private val notifications = mutableListOf<NotificationItem>()

    // Path yang benar sesuai dengan script Node.js
    private val notificationsRef = FirebaseDatabase.getInstance("https://mikrotik-alert-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("logs/notifications")

    private var lastKey: String? = null
    private var isLoading = false
    private var isLastPage = false
    private val PAGE_SIZE = 20

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRiwayatNotifikasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        applyWindowInsets(binding.appBarLayout)
        
        setupToolbar()
        setupRecyclerView()
        loadNotifications(true)

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadNotifications(true)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter()
        binding.rvNotifikasi.apply {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            adapter = notificationAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val visibleItemCount = linearLayoutManager.childCount
                    val totalItemCount = linearLayoutManager.itemCount
                    val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE
                        ) {
                            loadNotifications(false)
                        }
                    }
                }
            })
        }
    }

    private fun loadNotifications(isInitial: Boolean) {
        if (isLoading) return
        isLoading = true

        if (isInitial) {
            lastKey = null
            isLastPage = false
            if (!binding.swipeRefreshLayout.isRefreshing) {
                binding.progressBar.isVisible = true
            }
        }

        var query: Query = notificationsRef.orderByKey()
        
        if (isInitial) {
            query = query.limitToLast(PAGE_SIZE)
        } else {
            lastKey?.let {
                query = query.endAt(it).limitToLast(PAGE_SIZE + 1)
            }
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return

                binding.progressBar.isVisible = false
                binding.swipeRefreshLayout.isRefreshing = false
                isLoading = false

                val tempList = mutableListOf<NotificationItem>()
                val children = snapshot.children.toList()
                
                if (children.isEmpty() || (!isInitial && children.size <= 1)) {
                    if (!isInitial) isLastPage = true
                    if (isInitial) {
                        notifications.clear()
                        notificationAdapter.submitList(emptyList())
                        binding.tvEmptyNotifikasi.isVisible = true
                    }
                    return
                }

                // Simpan key terkecil (paling lama) untuk pagination berikutnya
                val currentBatchFirstKey = children.first().key
                
                for (child in children) {
                    try {
                        val item = child.getValue(NotificationItem::class.java)
                        if (item != null) {
                            tempList.add(item)
                        }
                    } catch (e: Exception) {
                        Log.e("RiwayatNotifikasi", "Gagal parse data: ${e.message}")
                    }
                }

                // Jika bukan load awal, hapus item terakhir yang merupakan duplikat dari lastKey
                if (!isInitial) {
                    tempList.removeAt(tempList.size - 1)
                }

                val reversedBatch = tempList.reversed()
                
                if (isInitial) {
                    notifications.clear()
                    notifications.addAll(reversedBatch)
                } else {
                    notifications.addAll(reversedBatch)
                }
                
                lastKey = currentBatchFirstKey
                
                notificationAdapter.submitList(notifications.toList())
                
                binding.tvEmptyNotifikasi.isVisible = notifications.isEmpty()
                binding.rvNotifikasi.isVisible = notifications.isNotEmpty()
                
                val expectedSize = if (isInitial) PAGE_SIZE else PAGE_SIZE + 1
                if (children.size < expectedSize) {
                    isLastPage = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (_binding != null) {
                    isLoading = false
                    binding.progressBar.isVisible = false
                    binding.swipeRefreshLayout.isRefreshing = false
                    Log.w("RiwayatNotifikasi", "onCancelled", error.toException())
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
