package com.thekrimo.nafes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thekrimo.nafes.databinding.ActivityTherapistProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TherapistProfile : BaseActivity() {
    private lateinit var binding: ActivityTherapistProfileBinding
    private lateinit var TherapistId: String
    private lateinit var TherapistName: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTherapistProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.session.setOnClickListener { startActivity(Intent(this,CalenderActivity::class.java)) }
        binding.message.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragmentTag", "Chat")
            startActivity(intent)
        }

        val therapistData = loadLocally()
        if (therapistData.isNotEmpty()) {
            binding.therapistName.text = therapistData[0]
            TherapistId = therapistData[1]
        }
        CoroutineScope(Dispatchers.IO).launch{fetchTherapist()}
    }


    private suspend fun fetchTherapist() {
        withContext(Dispatchers.IO){
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val userId = firebaseUser?.uid

            if (userId != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if ( snapshot.exists()) {
                            TherapistName = snapshot.child("therapistName").getValue(String::class.java) ?: ""
                            TherapistId = snapshot.child("therapistID").getValue(String::class.java) ?: ""

                                binding.therapistName.text = TherapistName

                           saveLocally(TherapistName, TherapistId)

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error if data retrieval fails Kash nhar
                    }

                })
            }
        }
    }

    private fun saveLocally(name: String, id: String) {
        val sharedPreferences = getSharedPreferences("therapist_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("therapist", name)
        editor.putString("id", id)
        editor.apply()
    }

    private fun loadLocally(): List<String> {
        val sharedPreferences = getSharedPreferences("therapist_data", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("therapist", "") ?: ""
        val id = sharedPreferences.getString("id", "") ?: ""
        return listOf(name, id)
    }
}