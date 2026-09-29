package com.linkbit.billrt

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
import com.linkbit.billrt.adapter.TransaksiLainAdapter
import com.linkbit.billrt.databinding.FragmentTransaksiLainBinding
import com.linkbit.billrt.model.TransaksiLain

class TransaksiLainFragment : BaseFragment() {

    private var _binding: FragmentTransaksiLainBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TransaksiLainAdapter

    private val dbRef = FirebaseDatabase.getInstance("https://transaksilain-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("transaksi_lain")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransaksiLainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Menyesuaikan toolbar agar tidak menabrak status bar (sama seperti fragment lain)
        applyWindowInsets(binding.appBarLayout)
        
        setupToolbar()
        setupRecyclerView()
        fetchData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = TransaksiLainAdapter()
        binding.rvTransaksiLain.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TransaksiLainFragment.adapter
        }
    }

    private fun fetchData() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                
                val list = mutableListOf<TransaksiLain>()
                try {
                    for (child in snapshot.children) {
                        // Membaca manual untuk menghindari error mismatch tipe data
                        val id = child.child("id").getValue(Long::class.java) ?: 0L
                        val jumlah = child.child("jumlah").getValue(Double::class.java) ?: 
                                     child.child("jumlah").getValue(Long::class.java)?.toDouble() ?: 0.0
                        val kategori = child.child("kategori").getValue(String::class.java) ?: ""
                        val keterangan = child.child("keterangan").getValue(String::class.java) ?: ""
                        val tanggal = child.child("tanggal").getValue(String::class.java) ?: ""
                        
                        list.add(TransaksiLain(id, tanggal, kategori, jumlah, keterangan))
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseError", "Error parsing data: ${e.message}")
                }

                if (list.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyState.text = "Tidak ada data di Firebase"
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                }
                
                adapter.submitList(list.reversed())
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                Log.e("FirebaseError", "Database error: ${error.message}")
                Toast.makeText(context, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
