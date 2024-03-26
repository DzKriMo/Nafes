package com.thekrimo.nafes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thekrimo.nafes.databinding.ActivityFoundBinding

class Found : BaseActivity() {
    private lateinit var binding: ActivityFoundBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoundBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.therapistProfile.setOnClickListener {
            startActivity(Intent(this,TherapistProfile::class.java))
        }
        binding.session.setOnClickListener {
            startActivity(Intent(this, CalenderActivity::class.java))
        }
    }
}