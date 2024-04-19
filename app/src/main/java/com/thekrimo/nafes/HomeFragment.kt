package com.thekrimo.nafes

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.thekrimo.nafes.databinding.FragmentHomeBinding
import java.text.DateFormatSymbols
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var TherapistId: String
    private lateinit var TherapistName: String
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.therapistProfile.setOnClickListener {
            startActivity(Intent(requireActivity(), TherapistProfile::class.java))
        }
        binding.session.setOnClickListener {
            startActivity(Intent(requireActivity(), CalenderActivity::class.java))
        }
        val therapistData = loadLocally()
        if (therapistData.isNotEmpty()) {
            binding.therapistName.text = therapistData[0]
            TherapistId = therapistData[1]
        }
        fetchTherapist()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchTherapist() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId = firebaseUser?.uid

        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        TherapistName = snapshot.child("therapistName").getValue(String::class.java) ?: ""
                        TherapistId = snapshot.child("therapistID").getValue(String::class.java) ?: ""
                        binding.therapistName.text = TherapistName
                        saveLocaly(TherapistName, TherapistId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if data retrieval fails Kash nhar
                }

            })
        }
    }

    private fun saveLocaly(name: String, Id: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("therapist_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("therapist", name)
        editor.putString("id", Id)
        editor.apply()
    }

    private fun loadLocally(): List<String> {
        val sharedPreferences = requireActivity().getSharedPreferences("therapist_data", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("therapist", "") ?: ""
        val ID = sharedPreferences.getString("id", "") ?: ""
        return listOf(name, ID)
    }
}