package com.thekrimo.nafes

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thekrimo.nafes.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    lateinit var userName: String
    lateinit var userEmail: String
    private lateinit var bottomNavigation: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        fetchUserName()
        val (name, email) = loadLocally()
        userEmail = email
        userName = name


        // Check if user is not logged in, redirect to GetStarted activity
        if (auth.currentUser == null) {
            startActivity(Intent(this, GetStarted::class.java))
            finish()
            return
        }

        createNotificationChannel()









       bottomNavigation = binding.bottomNavigation

        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.navigation_home ->{
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navigation_profile ->{
                    val bundle = Bundle().apply {
                        putString("userName", userName)
                        putString("userEmail", userEmail)
                    }
                    replaceFragment(ProfileFragment())
                    true
                }
                R.id.navigation_community ->{
                    replaceFragment(CommunityFragment())
                    true
                }
                R.id.navigation_Chat ->{
                    replaceFragment(ConversationFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragment(HomeFragment())






    }








    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "appointment_channel"
            val channelName = "Appointment Reminder"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Reminder for today's appointment"
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification(context: Context, contentText: String) {
        val notificationManager = NotificationManagerCompat.from(context)

        val notification = NotificationCompat.Builder(context, "appointment_channel")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Appointment Reminder")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(0, notification)
    }


    private fun fetchUserName(){
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId = firebaseUser?.uid

        if (userId!=null){
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userName = snapshot.child("username").getValue(String::class.java) ?: ""
                        userEmail = snapshot.child("email").getValue(String::class.java) ?: ""
                        // images and what they do
                        saveLocally(userName,userEmail)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if data retrieval fails Kash nhar
            }

        })
        }


    }


    private fun saveLocally(name: String,email:String) {
        val sharedPreferences = getSharedPreferences("username", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", name)
        editor.apply()

        getSharedPreferences("email", Context.MODE_PRIVATE).edit().putString("email",email).apply()

    }


    private fun loadLocally(): Pair<String, String> {
        val sharedPreferences = getSharedPreferences("username", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""
        val useremail = sharedPreferences.getString("email", "") ?: ""
        return Pair(username, useremail)
    }




    private fun replaceFragment(fragment: Fragment, args: Bundle? = null) {
        fragment.arguments = args
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        val conversationFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (conversationFragment is ConversationFragment && conversationFragment.isVisible &&conversationFragment.isWebVisible()) {
            // If the ConversationFragment is visible and WebView is visible, hide the WebView
            conversationFragment.hideWebView()
        } else {
            // Perform default back button behavior
            super.onBackPressed()
        }
    }

}
