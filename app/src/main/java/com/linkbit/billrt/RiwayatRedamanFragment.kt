package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.linkbit.billrt.adapter.RiwayatRedamanAdapter
import com.linkbit.billrt.databinding.FragmentRiwayatRedamanBinding
import com.linkbit.billrt.model.RiwayatRedamanItem

class RiwayatRedamanFragment : BaseFragment() {

    private var _binding: FragmentRiwayatRedamanBinding? = null
    private val binding get() = _binding!!

    private val args: RiwayatRedamanFragmentArgs by navArgs()
    private lateinit var adapter: RiwayatRedamanAdapter
    private var fullHistoryList: List<RiwayatRedamanItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatRedamanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RiwayatRedamanAdapter(emptyList())
        binding.rvRiwayatRedaman.layoutManager = LinearLayoutManager(context)
        binding.rvRiwayatRedaman.adapter = adapter

        setupSearchView()
        fetchDataFromFirebase()
    }

    private fun setupSearchView() {
        binding.searchViewRiwayat.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterHistory(newText)
                return true
            }
        })
    }

    private fun filterHistory(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            fullHistoryList
        } else {
            fullHistoryList.filter {
                it.signal.contains(query, ignoreCase = true) ||
                it.timestamp.contains(query, ignoreCase = true)
            }
        }
        adapter.updateData(filteredList)
    }

    private fun fetchDataFromFirebase() {
        binding.progressBarRiwayat.visibility = View.VISIBLE
        val macAddress = args.macAddress.uppercase()

        val dbRef = FirebaseDatabase.getInstance("https://redaman-5b2ae-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("redaman")
        val query = dbRef.orderByChild("mac").equalTo(macAddress)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return
                
                val historyList = mutableListOf<RiwayatRedamanItem>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val signal = child.child("signal").getValue(String::class.java)
                        val timestamp = child.child("timestamp").getValue(String::class.java)

                        if (signal != null && timestamp != null) {
                            historyList.add(RiwayatRedamanItem(signal, timestamp))
                        }
                    }
                }

                fullHistoryList = historyList.sortedByDescending { it.timestamp }
                
                if (fullHistoryList.isEmpty()) {
                    binding.tvEmptyRiwayat.visibility = View.VISIBLE
                } else {
                    adapter.updateData(fullHistoryList)
                }
                binding.progressBarRiwayat.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                if (_binding != null) {
                    binding.progressBarRiwayat.visibility = View.GONE
                    binding.tvEmptyRiwayat.text = "Gagal memuat data: ${error.message}"
                    binding.tvEmptyRiwayat.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
