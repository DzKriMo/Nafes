package com.thekrimo.nafes

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.thekrimo.nafes.databinding.FragmentCommunityBinding
import java.text.SimpleDateFormat
import java.util.*

class CommunityFragment : Fragment() {

    private lateinit var binding: FragmentCommunityBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var database: DatabaseReference
    private lateinit var userName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference.child("posts")

        setupRecyclerView()
        fetchUserName()
        userName = loadLocally()

        binding.buttonSend.setOnClickListener {
            val postContent = binding.editTextPost.text.toString().trim()
            if (postContent.isNotEmpty()) {
                val currentTime = System.currentTimeMillis()
                val postId = currentTime.toString()
                val post = Post(userName, postId, postContent, getCurrentDateTime())

                // Save to Firebase
                database.child(postId).setValue(post)
                    .addOnSuccessListener {
                        binding.editTextPost.text.clear()
                        scrollToTop() // Scroll to top after adding a new item
                    }
                    .addOnFailureListener {
                        // Handle failure maybe :)
                    }
            }
        }

        // Retrieve posts from Firebase
        val postsQuery = database.orderByChild("timestamp").limitToLast(50)
        postsQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val postsList = mutableListOf<Post>()
                for (postSnapshot in dataSnapshot.children.reversed()) { // Reverse to display newest posts first
                    val post = postSnapshot.getValue(Post::class.java)
                    post?.let { postsList.add(it) }
                }
                postAdapter.submitList(postsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()
        binding.recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun scrollToTop() {
        binding.recyclerViewPosts.scrollToPosition(0)
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun fetchUserName() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId = firebaseUser?.uid

        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userName = snapshot.child("username").getValue(String::class.java) ?: ""
                        saveLocally(userName)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if data retrieval fails Kash nhar
                }

            })
        }
    }

    private fun saveLocally(name: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", name)
        editor.apply()
    }

    private fun loadLocally(): String {
        val sharedPreferences = requireActivity().getSharedPreferences("username", Context.MODE_PRIVATE)
        return sharedPreferences.getString("username", "") ?: ""
    }
}
