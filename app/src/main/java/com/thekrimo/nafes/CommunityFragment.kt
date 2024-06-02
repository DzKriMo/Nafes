package com.thekrimo.nafes

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
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
    ): View {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference.child("posts")

        setupRecyclerView()
        fetchUserName()
        userName = loadLocally()

        if(!isNetworkConnected()){
            loadLocalPosts()
        }

        binding.buttonSend.setOnClickListener {
            val postContent = binding.editTextPost.text.toString().trim()
            if (postContent.isNotEmpty()) {
                val currentTime = System.currentTimeMillis()
                val postId = currentTime.toString()
                val post = Post(userName, postId, postContent, getCurrentDateTime())


                savePostLocally(postId, post)


                database.child(postId).setValue(post)
                    .addOnSuccessListener {
                        binding.editTextPost.text.clear()
                        scrollToTop() // Scroll to top after adding a new item
                    }
                    .addOnFailureListener {
                        // Handle failure
                        Log.e("CommunityFragment", "Failed to save post to Firebase")
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
                // Handle database error maybe kash nhar
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
                    // Handle error if data retrieval fails
                    Log.e("CommunityFragment", "Database error: ${error.message}")
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
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        return sharedPreferences.getString("username", "") ?: ""
    }

    private fun savePostLocally(postId: String, post: Post) {
        val sharedPreferences = requireActivity().getSharedPreferences("local_posts", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val postJson = Gson().toJson(post)
        editor.putString(postId, postJson)
        editor.apply()
    }
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
    private fun loadLocalPosts(): List<Post> {
        val sharedPreferences = requireActivity().getSharedPreferences("local_posts", Context.MODE_PRIVATE)
        val postsMap = sharedPreferences.all
        val postsList = mutableListOf<Post>()
        for ((_, postJson) in postsMap) {
            try {
                if (postJson is String) {
                    val post = Gson().fromJson(postJson, Post::class.java)
                    postsList.add(post)
                }
            } catch (e: JsonSyntaxException) {
                Log.e("CommunityFragment", "Error parsing local post JSON: $e")
            }
        }
        return postsList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveLocally(userName)
    }
}
