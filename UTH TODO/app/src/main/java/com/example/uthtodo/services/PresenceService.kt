package com.example.uthtodo.services

import androidx.compose.runtime.mutableStateOf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.util.UUID

object PresenceService {
    val status = mutableStateOf("Đang kết nối")
    private val db by lazy { FirebaseDatabase.getInstance() }
    private var listener: ValueEventListener? = null
    private var uid: String? = null
    fun start(userId: String) {
        uid = userId
        db.goOnline()
        val session = db.getReference("presence").child(userId).child(UUID.randomUUID().toString())
        val observer = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (uid != userId) return
                if (snapshot.getValue(Boolean::class.java) == true) {
                    val offline = mapOf("online" to false, "updatedAt" to ServerValue.TIMESTAMP)
                    session.onDisconnect().setValue(offline).addOnSuccessListener {
                        if (uid == userId) session.setValue(mapOf("online" to true, "updatedAt" to ServerValue.TIMESTAMP))
                            .addOnCompleteListener { status.value = if (it.isSuccessful) "Trực tuyến" else "Chưa cập nhật trạng thái kết nối" }
                    }.addOnFailureListener { status.value = "Chưa cập nhật trạng thái kết nối" }
                } else status.value = "Ngoại tuyến"
            }
            override fun onCancelled(error: DatabaseError) { status.value = "Chưa cập nhật trạng thái kết nối" }
        }
        listener = observer
        db.getReference(".info/connected").addValueEventListener(observer)
    }
    fun stop() {
        uid = null
        listener?.let { db.getReference(".info/connected").removeEventListener(it) }
        listener = null
        // onDisconnect executes server-side even if the process is killed.
        db.goOffline()
        status.value = "Chưa đăng nhập"
    }
}
