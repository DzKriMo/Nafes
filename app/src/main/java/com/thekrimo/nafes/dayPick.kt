package com.thekrimo.nafes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thekrimo.nafes.databinding.ActivityDayPickBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class dayPick : AppCompatActivity() {
    private lateinit var binding: ActivityDayPickBinding
    private var availableTimeSlots = mutableListOf<String>()
    private var date : String = ""
    private var userEmail : String = ""
    private var userName : String = ""
    private var clientId: String =""
    private var therapistID : String =""
    private var clientEmail :String =""
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDayPickBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val (name, email, client,therapist,clientemail) = loadLocally()
        userName = name
        userEmail = email
        clientId = client
        therapistID = therapist
        clientEmail = clientemail

        CoroutineScope(Dispatchers.IO).launch { fetchUserName() }

        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        var day = intent.getStringExtra("day")
        if(day?.toInt()!! <10){
            day = "0$day"
        }

        var month = intent.getIntExtra("month", 0)
        val year = intent.getIntExtra("year", 0)

        val dayy = day + " " + months[month] +  " " + year.toString()
        binding.day.text = dayy
        month++
         date = "$year-0$month-$day"

        val thid = 1 // Sample therapist ID for testing
      // date = "2024-05-13"
        Log.d("datee",date)
        binding.back.setOnClickListener {
            startActivity(Intent(this, CalenderActivity::class.java))
            finish()
        }

        val url = "https://expert-duck-pleasing.ngrok-free.app/api/meeting/checkAvailability?therapist_id=$thid&date=$date"
        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connect()

                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                // Process the JSON response
                val responseData = response.toString()
                val jsonObject = JSONObject(responseData)
                val availableTimeSlotsJson = jsonObject.getJSONArray("available_time_slots")

                for (i in 0 until availableTimeSlotsJson.length()) {
                    val timeSlot = availableTimeSlotsJson.getString(i)
                    availableTimeSlots.add(timeSlot)
                    Log.d("timeslots", availableTimeSlots.toString())
                }


                runOnUiThread {
                    updateButtonsBackground()
                }


                reader.close()
                inputStream.close()
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("API", "Failed to make API call: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@dayPick, "Failed to make API call", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun updateButtonsBackground() {
        val buttonIds = listOf(R.id.btn8, R.id.btn9, R.id.btn10, R.id.btn11, R.id.btn13, R.id.btn14, R.id.btn15, R.id.btn16)
        for (buttonId in buttonIds) {
            val button = findViewById<Button>(buttonId)
            val buttonText = button.text.toString()

            button.setOnClickListener {
                if (availableTimeSlots.contains(buttonText)) {
                    scheduleMeeting(buttonText)
                } else {
                    Toast.makeText(this@dayPick, "This time slot is not available", Toast.LENGTH_SHORT).show()
                }
            }

            if (!availableTimeSlots.contains(buttonText)) {
                button.setBackgroundResource(R.drawable.buttonbackkk)
            }
        }
    }

    private fun scheduleMeeting(selectedTime: String) {
        Thread {
            try {

                date +="T"
                var tmp = ""
                when (selectedTime){
                    "08:00"->tmp="09:00"
                    "09:00"->tmp="10:00"
                    "10:00"->tmp="11:00"
                    "11:00"->tmp="12:00"
                    "12:00"->tmp="13:00"
                    "13:00"->tmp="14:00"
                    "14:00"->tmp="15:00"
                    "15:00"->tmp="16:00"
                    "16:00"->tmp="17:00"
                }


                val url = "https://expert-duck-pleasing.ngrok-free.app/api/meeting/schedule" +
                        "?therapist_id=$therapistID" +
                        "&client_id=$clientId" +
                        "&client_name=$userName" +
                        "&client_email=$clientEmail" +
                        "&start_time=$date$selectedTime:00" +
                        "&end_time=$date$tmp:00" +
                        "&duration=45" +
                        "&meeting_type=Initial%20Consultation" +
                        "&status=Scheduled" +
                        "&note=Please%20bring%20your%20meds"

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    Log.d("API", "Meeting scheduled successfully")
                    runOnUiThread {
                        Toast.makeText(this@dayPick, "Meeting scheduled successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("fragmentTag", "Chat")
                        startActivity(intent)
                    }
                } else {
                    Log.e("API", "HTTP Error: $responseCode")
                    runOnUiThread {
                        Toast.makeText(this@dayPick, "Failed to schedule meeting", Toast.LENGTH_SHORT).show()
                    }
                }

                // Close connections
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("API", "Failed to schedule meeting: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@dayPick, "Failed to schedule meeting", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private suspend fun fetchUserName() {
        withContext(Dispatchers.IO) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val userId = firebaseUser?.uid

            if (userId != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            userName = snapshot.child("username").getValue(String::class.java) ?: ""
                            userEmail = snapshot.child("email").getValue(String::class.java) ?: ""
                            clientId = userId
                            therapistID = snapshot.child("therapistID").getValue(String::class.java) ?: ""
                            clientEmail = snapshot.child("email").getValue(String::class.java) ?: ""

                            saveLocally(userName, userEmail,clientId,therapistID,clientEmail)

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error if data retrieval fails
                        Log.e("ProfileFragment", "Database error: ${error.message}")
                    }
                })
            }
        }
    }
    private fun saveLocally(name: String, email: String, client: String,therapist : String,clientemail:String) {
        val sharedPreferences = this.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", name)
        editor.putString("email", email)
        editor.putString("client", client)
        editor.putString("therapist", therapist)
        editor.putString("email", clientemail)
        editor.apply()
    }



    private fun loadLocally(): List<String> {
        val sharedPreferences = this.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""
        val useremail = sharedPreferences.getString("email", "") ?: ""
        val client = sharedPreferences.getString("client", "") ?: ""
        val therapist = sharedPreferences.getString("therapist", "") ?: ""
        val clientemail =sharedPreferences.getString("email", "") ?: ""
        return listOf(username, useremail, client,therapist,clientemail)
    }

}
