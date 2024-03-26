package com.thekrimo.nafes

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.thekrimo.nafes.databinding.ActivityLoginBinding
import okhttp3.OkHttpClient

class Login : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val okHttpClient = OkHttpClient()





       binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            showToast("Login Failed")
                        }

                    }
            } else {
               showToast("Email Or Password Cannot Be Empty")
            }
        }

        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Perform login action here
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                showToast("Login Failed")
                            }
                        }
                } else {
                    showToast("Email or Password cannot be empty")
                }
                true // Return true to indicate that the action has been handled
            } else {
                false // Return false if the action is not handled
            }
        }






        binding.forgotPasswordTextView.setOnClickListener {
            val email = binding.emailEditText.text.toString()
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

        binding.showPassword.setOnCheckedChangeListener { _, isChecked ->
            binding.passwordEditText.inputType = if (isChecked) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }








        binding.SignUp.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))

            finish()
        }
    }



    private fun showToast(text:String){
        Toast.makeText(baseContext,text,Toast.LENGTH_SHORT).show()
    }



}
