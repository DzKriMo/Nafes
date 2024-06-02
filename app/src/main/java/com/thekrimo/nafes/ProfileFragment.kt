package com.thekrimo.nafes

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thekrimo.nafes.databinding.FragmentProfileBinding
import android.view.inputmethod.EditorInfo
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding ?: error("Binding not initialized")
    private lateinit var auth: FirebaseAuth
    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userPhone: String
    private lateinit var imagelink: String
    private  val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val (name, email, phone,image) = loadLocally()
        userName = name
        userEmail = email
        userPhone = phone
        imagelink = image



        binding.name.text = userName
        binding.emailEditText.hint = userEmail
        binding.nameEditText.hint = userName

        if (imagelink.isNotEmpty()) {
            Picasso.get().load(imagelink).into(binding.pfp)
        }

        if (userPhone.isNotEmpty()) binding.phoneEditText.hint = userPhone

        CoroutineScope(Dispatchers.IO).launch { fetchUserName() }

        binding.logOut.setOnClickListener {
            auth.signOut()
            if (auth.currentUser == null) {
                Toast.makeText(requireActivity(), "Logged Out", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireActivity(), GetStarted::class.java))
            }
            clearSharedPreferences()
        }

        binding.EditPicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
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
                CoroutineScope(Dispatchers.IO).launch { editPhone(binding.phoneEditText.text.toString()) }
                binding.phoneEditText.hint = binding.phoneEditText.text.toString()
                binding.phoneEditText.inputType = InputType.TYPE_NULL
                true
            } else {
                false
            }
        }

        binding.editName.setOnClickListener {
            binding.nameEditText.hint = "Type Your Name"
            binding.nameEditText.inputType = InputType.TYPE_CLASS_TEXT
        }

        binding.nameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                CoroutineScope(Dispatchers.IO).launch { editName(binding.nameEditText.text.toString()) }
                binding.nameEditText.hint = binding.phoneEditText.text.toString()
                binding.nameEditText.inputType = InputType.TYPE_NULL
                true
            } else {
                false
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!
            val imageFile = File(imageUri.path!!)

            uploadImageToStorage(imageFile)
        }
    }

    private fun uploadImageToStorage(imageFile: File) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId = firebaseUser?.uid

        if (userId != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("files/profile.jpg")
            val fileUri = Uri.fromFile(imageFile)

            Log.d("ProfileFragment", "Uploading image: $fileUri")

            storageRef.putFile(fileUri)
                .addOnSuccessListener { taskSnapshot ->
                    Log.d("ProfileFragment", "Image upload successful")
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        CoroutineScope(Dispatchers.IO).launch { saveImageUrlToDatabase(imageUrl) }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileFragment", "Image upload failed: ${e.message}")
                    showToast("Image upload failed: ${e.message}")
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private suspend fun saveImageUrlToDatabase(image: String) {
        withContext(Dispatchers.IO) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val uid = firebaseUser?.uid
            if (uid != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("image")
                databaseReference.setValue(image)
            }
        }
    }

    private suspend fun editPhone(phone: String) {
        withContext(Dispatchers.IO) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val uid = firebaseUser?.uid
            if (uid != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("phone")
                databaseReference.setValue(phone)
            }
        }
    }

    private suspend fun editName(name: String) {
        withContext(Dispatchers.IO) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val uid = firebaseUser?.uid
            if (uid != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("username")
                databaseReference.setValue(name)
            }
        }
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
                            userPhone = snapshot.child("phone").getValue(String::class.java) ?: ""
                            imagelink =  snapshot.child("image").getValue(String::class.java) ?: ""

                            if (userPhone.isNotEmpty()) binding.phoneEditText.hint = userPhone
                            saveLocally(userName, userEmail, userPhone,imagelink)
                            binding.name.text = userName
                            binding.emailEditText.hint = userEmail
                            binding.nameEditText.hint = userName
                            if (imagelink.isNotEmpty()) {
                                Picasso.get().load(imagelink).into(binding.pfp)
                            }
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

    private fun showToast(text: String) {
        Toast.makeText(requireActivity(), text, Toast.LENGTH_SHORT).show()
    }

    private fun saveLocally(name: String, email: String, phone: String,image : String) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", name)
        editor.putString("email", email)
        editor.putString("phone", phone)
        editor.putString("image", image)
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
        val image = sharedPreferences.getString("image", "") ?: ""
        return listOf(username, useremail, userphone, image)
    }
}
