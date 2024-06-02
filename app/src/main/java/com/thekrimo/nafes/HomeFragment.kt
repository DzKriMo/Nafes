package com.thekrimo.nafes


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thekrimo.nafes.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject
import java.io.*


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
        CoroutineScope(Dispatchers.IO).launch{fetchTherapist()}

        val sqlId =  getTherapistNameById(TherapistId.toInt())
        if (sqlId != null) {
              TherapistName = sqlId
            binding.therapistName.text = sqlId
            Log.d("API Response", sqlId)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun fetchTherapist() {
        withContext(Dispatchers.IO){
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId = firebaseUser?.uid

        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isAdded && snapshot.exists()) {
                        TherapistName = snapshot.child("therapistName").getValue(String::class.java) ?: ""
                        TherapistId = snapshot.child("therapistID").getValue(String::class.java) ?: ""
                        _binding?.let {
                            it.therapistName.text = TherapistName
                        }
                        if(isAdded){saveLocally(TherapistName, TherapistId)}

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if data retrieval fails Kash nhar
                }

            })
        }
    }}

    private fun saveLocally(name: String, id: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("therapist_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("therapist", name)
        editor.putString("id", id)
        editor.apply()
    }

    private fun loadLocally(): List<String> {
        val sharedPreferences = requireActivity().getSharedPreferences("therapist_data", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("therapist", "") ?: ""
        val id = sharedPreferences.getString("id", "") ?: ""
        return listOf(name, id)
    }


    fun getTherapistNameById(id: Int): String? {
        val urlString = "https://expert-duck-pleasing.ngrok-free.app/api/therapists/show/$id" // Replace with your actual API URL
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val jsonResponse = JSONObject(response)
                Log.d("API Response", jsonResponse.toString())
                val therapist = jsonResponse.getJSONObject("therapist")
                therapist.getString("name")
            } else {
                println("Error: ${connection.responseMessage}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection.disconnect()
        }
    }


}