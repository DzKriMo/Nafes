package com.thekrimo.nafes

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.thekrimo.nafes.databinding.ActivityGetStartedBinding

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




    @Deprecated("Deprecated in Java")
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


}