package com.thekrimo.nafes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.thekrimo.nafes.databinding.ActivityMainBinding
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser == null){
            startActivity(Intent(this, GetStarted::class.java))
            finish()
        }

        binding.logout.setOnClickListener {
            auth.signOut()
            if (auth.currentUser == null){
                Toast.makeText(baseContext,"Logged Out!.",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,Login::class.java))
                finish()
            }
        }



    }
}