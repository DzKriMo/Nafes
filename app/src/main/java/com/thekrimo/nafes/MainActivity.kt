package com.thekrimo.nafes



import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thekrimo.nafes.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    lateinit var userName: String
    lateinit var userEmail: String
    private var currentFragmentTag: String? = null
    private lateinit var bottomNavigation: BottomNavigationView
    private val menuItems = arrayOf("HomeFragment", "ProfileFragment", "CommunityFragment", "ConversationFragment")
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




        val fragmentTag = intent.getStringExtra("fragmentTag")
        if (fragmentTag == "Chat") {
            replaceFragment(ConversationFragment(), "ConversationFragment")
        }










        bottomNavigation = binding.bottomNavigation
        replaceFragment(HomeFragment(), "HomeFragment")
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment(), "HomeFragment")
                    true
                }
                R.id.navigation_profile -> {
                    replaceFragment(ProfileFragment(), "ProfileFragment")
                    true
                }
                R.id.navigation_community -> {
                    replaceFragment(CommunityFragment(), "CommunityFragment")
                    true
                }
                R.id.navigation_Chat -> {
                    replaceFragment(ConversationFragment(), "ConversationFragment")
                    true
                }
                else -> false
            }
        }








    }













    private fun fetchUserName() {
        CoroutineScope(Dispatchers.IO).launch {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val userId = firebaseUser?.uid

            if (userId != null && !isFinishing) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            userName = snapshot.child("username").getValue(String::class.java) ?: ""
                            userEmail = snapshot.child("email").getValue(String::class.java) ?: ""

                            saveLocally(userName, userEmail)


                            val fragmentTag = intent.getStringExtra("fragmentTag")
                            if (fragmentTag == "Chat") {
                                bottomNavigation.selectedItemId = R.id.navigation_Chat
                                replaceFragment(ConversationFragment(), "ConversationFragment")
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error if data retrieval fails Kash nhar
                    }
                })
            }
        }
    }

    private fun getIndexForFragment(fragmentTag: String): Int {
        return menuItems.indexOf(fragmentTag)
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




    private fun replaceFragment(fragment: Fragment, tag: String) {
        if (currentFragmentTag != tag) {

            val transaction = supportFragmentManager.beginTransaction()
            val currentIndex = getIndexForFragment(currentFragmentTag ?: "")
            val targetIndex = getIndexForFragment(tag)
            currentFragmentTag = tag
            val directionTransition = if (currentIndex < targetIndex) {
                Pair(R.anim.animate_slide_left_enter, R.anim.animate_slide_left_exit)
            } else {
                Pair(R.anim.animate_slide_in_left, R.anim.animate_slide_out_right)
            }

            val (enterAnim, exitAnim) = directionTransition
            transaction.setCustomAnimations(enterAnim, exitAnim)
            transaction.replace(R.id.fragment_container, fragment, tag)
            transaction.commitAllowingStateLoss()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val conversationFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (conversationFragment is ConversationFragment && conversationFragment.isVisible &&conversationFragment.isWebVisible()) {
            conversationFragment.hideWebView()
        } else {
            // Perform default back button behavior
            super.onBackPressed()
        }
    }

}
