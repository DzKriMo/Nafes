package com.thekrimo.nafes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thekrimo.nafes.databinding.ActivityTherapistProfileBinding
class TherapistProfile : BaseActivity() {
    private lateinit var binding: ActivityTherapistProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTherapistProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.session.setOnClickListener { startActivity(Intent(this,CalenderActivity::class.java)) }
    }
}