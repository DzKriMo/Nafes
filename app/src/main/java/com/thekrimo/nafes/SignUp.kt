package com.thekrimo.nafes

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.thekrimo.nafes.databinding.ActivitySignUpBinding

class SignUp : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val username=  binding.nameEditText.text.toString()
            val cpassword = binding.confirmpasswordEditText.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()&& cpassword.isNotEmpty()&& username.isNotEmpty()) {
                if(cpassword==password){

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this){task ->
                        if(task.isSuccessful){
                            showToast("User Created!.")
                            startActivity(Intent(this,MainActivity::class.java))
                            finish()
                        }
                        else{
                            showToast("Unable To Sign Up! Please Try Again Later!.")
                        }
                    }
            }else{
                showToast("Passwords Mismatch!.")
            }
            }else{
                showToast("Email Or Password Cannot Be Empty!.")
            }
        }

        binding.signup.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
            this.overridePendingTransition(
                R.anim.animate_slide_in_left,
                R.anim.animate_slide_out_right
            )
        }

        binding.showPassword.setOnCheckedChangeListener { _, isChecked ->
            binding.passwordEditText.inputType = if (isChecked) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.confirmpasswordEditText.inputType = if (isChecked) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        startWaveAnimation()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, Login::class.java))
        finish()
        this.overridePendingTransition(
            R.anim.animate_slide_in_left,
            R.anim.animate_slide_out_right
        )
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


    private fun showToast(text: String){
        Toast.makeText(baseContext,text,Toast.LENGTH_SHORT).show()
    }
}