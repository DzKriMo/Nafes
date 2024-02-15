package com.thekrimo.nafes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thekrimo.nafes.databinding.ActivityFoundBinding

class Found : AppCompatActivity() {
    private lateinit var binding: ActivityFoundBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoundBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}