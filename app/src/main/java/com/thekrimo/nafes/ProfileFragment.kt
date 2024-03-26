package com.thekrimo.nafes

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thekrimo.nafes.databinding.FragmentProfileBinding
import java.util.Calendar
import java.util.GregorianCalendar

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    lateinit var userName: String
    lateinit var userEmail: String
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchUserName()
        val (name, email) = loadLocally()
        userEmail = email


        userName = name
       // val userName = arguments?.getString("userName")

      //  val userEmail = arguments?.getString("userEmail")




        binding.logOut.setOnClickListener {
            auth.signOut()
            if (auth.currentUser == null) {
                Toast.makeText(
                    requireActivity(), "Logged Out",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(requireActivity(), GetStarted::class.java))
            }
        }

        binding.editPassword.setOnClickListener {
            val email = binding.emailEditText.hint.toString()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showToast("Password Reset link sent, Check Your Inbox.")
                        } else {
                            showToast("Unable To send Password Reset Link.")
                        }
                    }
            } else showToast("Email Is Empty!!.")
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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


                        binding.name.text = userName
                        binding.emailEditText.hint = userEmail
                        binding.nameEditText.hint = userName

                        saveLocally(userName,userEmail)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if data retrieval fails Kash nhar
                }

            })
        }


    }
    private fun showToast(text:String){
        Toast.makeText(requireActivity(),text,Toast.LENGTH_SHORT).show()
    }


    private fun saveLocally(name: String, email: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", name)
        editor.putString("email", email)
        editor.apply()
    }



    private fun loadLocally(): Pair<String, String> {
        val sharedPreferences = requireActivity().getSharedPreferences("username", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""
        val useremail = sharedPreferences.getString("email", "") ?: ""
        return Pair(username, useremail)
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
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

    fun openLink(link: String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(link)
        startActivity(intent)
    }
}