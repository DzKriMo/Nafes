package com.thekrimo.nafes


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import android.view.inputmethod.EditorInfo
import android.widget.EditText
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    lateinit var userName: String
    lateinit var userEmail: String
    lateinit var userPhone: String
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
        val name = loadLocally()[0]
        val email = loadLocally()[1]
        val phone = loadLocally()[2]
        userEmail = email
        userName = name
        userPhone = phone

        binding.name.text = userName
        binding.emailEditText.hint = userEmail
        binding.nameEditText.hint = userName
        if (userPhone!="")binding.phoneEditText.hint = userPhone
        saveLocally(userName,userEmail,userPhone)
        fetchUserName()





        binding.logOut.setOnClickListener {
            auth.signOut()
            if (auth.currentUser == null) {
                Toast.makeText(
                    requireActivity(), "Logged Out",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(requireActivity(), GetStarted::class.java))
            }
            clearSharedPreferences()
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



        binding.editPhone.setOnClickListener {
            binding.phoneEditText.hint = "Type Your New Phone Number"
            binding.phoneEditText.inputType = InputType.TYPE_CLASS_PHONE

        }
        binding.phoneEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                EditPhone(binding.phoneEditText.text.toString())
                binding.phoneEditText.hint = binding.phoneEditText.text.toString()
                binding.phoneEditText.inputType = InputType.TYPE_NULL

                true // Return true to indicate that the action has been handled
            } else {
                false // Return false if the action is not handled
            }
        }


        binding.editName.setOnClickListener {
            binding.nameEditText.hint = "Type Your Name"
            binding.nameEditText.inputType = InputType.TYPE_CLASS_TEXT

        }
        binding.nameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                EditName(binding.nameEditText.text.toString())
                binding.nameEditText.hint = binding.phoneEditText.text.toString()
                binding.nameEditText.inputType = InputType.TYPE_NULL

                true // Return true to indicate that the action has been handled
            } else {
                false // Return false if the action is not handled
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun EditPhone(Phone:String){
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val uid = firebaseUser?.uid
        if(uid!=null){
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("phone")
            databaseReference.setValue(Phone)
        }
    }


    private fun EditName(Name:String){
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val uid = firebaseUser?.uid
        if(uid!=null){
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("username")
            databaseReference.setValue(Name)
        }
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
                        userPhone = snapshot.child("phone").getValue(String::class.java) ?: ""

                        if (userPhone!="")binding.phoneEditText.hint = userPhone
                        saveLocally(userName,userEmail,userPhone)
                        binding.name.text = userName
                        binding.emailEditText.hint = userEmail
                        binding.nameEditText.hint = userName


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


    private fun saveLocally(name: String, email: String,phone: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", name)
        editor.putString("email", email)
        editor.putString("phone", phone)
        editor.apply()
    }

    private fun clearSharedPreferences() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    private fun loadLocally(): List<String> {
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""
        val useremail = sharedPreferences.getString("email", "") ?: ""
        val userphone = sharedPreferences.getString("phone", "") ?: ""
        return listOf(username,useremail,userphone)
    }




    fun openLink(link: String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(link)
        startActivity(intent)
    }
}