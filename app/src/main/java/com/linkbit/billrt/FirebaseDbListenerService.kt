package com.linkbit.billrt

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseDbListenerService : ValueEventListener {

    private val database = Firebase.database.reference

    fun startListener() {
        database.child("alerts").addValueEventListener(this)
    }

    fun stopListener() {
        database.child("alerts").removeEventListener(this)
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        if (snapshot.exists()) {
            for (alertSnapshot in snapshot.children) {
                // The MikrotikAlert class does not exist in the provided code.
                // I will log the raw data for now.
                Log.d("FirebaseDbListener", "New Alert: ${alertSnapshot.value}")
            }
        }
    }

    override fun onCancelled(error: DatabaseError) {
        Log.w("FirebaseDbListener", "Failed to read value.", error.toException())
    }
}