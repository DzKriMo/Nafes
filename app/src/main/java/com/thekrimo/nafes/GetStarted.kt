package com.thekrimo.nafes

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.thekrimo.nafes.databinding.ActivityGetStartedBinding
import kotlin.math.log

class GetStarted : BaseActivity() {
    private lateinit var binding: ActivityGetStartedBinding

    private var backPressedOnce = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.Login.setOnClickListener {
            startActivity(Intent(this, Login::class.java))

        }

        binding.signup.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }



    }




    override fun onBackPressed() {
        if (backPressedOnce) {
            super.onBackPressed()
            return
        }
        backPressedOnce = true
        showToast("Press back again to exit")
        Handler(Looper.getMainLooper()).postDelayed({
            backPressedOnce = false
        }, 2000) // Reset the flag after 2 seconds
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    fun openLink(link: String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(link)
        startActivity(intent)
    }

}