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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.thekrimo.nafes.databinding.ActivityLoginBinding

class Login : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

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

        startWaveAnimation()







        binding.signup.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))

            finish()
        }
    }

    private fun startWaveAnimation() {

        val anim2 = createAnimation(binding.imageView2, 30f)
        val anim3 = createAnimation(binding.imageView3, 60f)

        val animatorSet = AnimatorSet()
        animatorSet.play(anim2)
        animatorSet.play(anim3)
        animatorSet.duration = 2000
        animatorSet.interpolator = LinearInterpolator()
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                // Animation start
            }

            override fun onAnimationEnd(animation: Animator) {
                // Animation end
                animatorSet.start()
            }

            override fun onAnimationCancel(animation: Animator) {
                // Animation cancel
            }

            override fun onAnimationRepeat(animation: Animator) {
                // Animation repeat
            }
        })

        animatorSet.start()
    }

    private fun createAnimation(view: View, translationY: Float): ObjectAnimator {
        val animation = ObjectAnimator.ofFloat(view, "translationY", translationY)
        animation.repeatCount = 1
        animation.repeatMode = ObjectAnimator.REVERSE
        return animation
    }

    private fun showToast(text:String){
        Toast.makeText(baseContext,text,Toast.LENGTH_SHORT).show()
    }



}
