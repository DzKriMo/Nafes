package com.thekrimo.nafes

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.thekrimo.nafes.databinding.ActivitySignUpBinding

@Suppress("DEPRECATION")
class SignUp : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInOptions: GoogleSignInOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("342707494946-alrvcdvl63ns0jc6hhrptec5n2np9n92.apps.googleusercontent.com")
            .requestEmail()
            .build()

        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val username=  binding.nameEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            saveUserToDatabase(username, email)
                            showToast("User Created!.")
                            startActivity(Intent(this, QuestionsActivity::class.java))
                            finish()
                        } else {
                            showToast("Unable To Sign Up! Please Try Again Later!.")
                        }
                    }
            } else {
                showToast("Email Or Password Cannot Be Empty!.")
            }
        }

        binding.google.setOnClickListener {
            signInWithGoogle()
        }

        binding.Login.setOnClickListener {
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
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = GoogleSignIn.getClient(this, googleSignInOptions).signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken!!)

                }
            } catch (e: ApiException) {
                // Sign in failed
                showToast("Google sign in failed.")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    showToast("Google sign in success.")
                    startActivity(Intent(this,MainActivity::class.java))
                } else {
                    // Sign in failed
                    showToast("Google sign in failed.")
                }
            }
    }

    private fun showToast(text: String) {
        Toast.makeText(baseContext, text, Toast.LENGTH_SHORT).show()
    }

    private fun saveUserToDatabase(username: String, email: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId = firebaseUser?.uid

        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            val userData = HashMap<String, Any>()
            userData["username"] = username
            userData["email"] = email
            userData["role"] = "Patient"
            userData["phone"] = ""
            userData["therapistID"] = ""
            userData["therapistName"] = ""
            userData["image"]="https://e7.pngegg.com/pngimages/171/655/png-clipart-tarek-hamed-2018-world-cup-egypt-national-football-team-liga-mx-club-tijuana-hector-herrera-tshirt-photography-thumbnail.png"
            userData["answers"]=""
            userData["matching"]=""
            databaseReference.setValue(userData)
                .addOnSuccessListener {
                    // Data successfully saved
                }
                .addOnFailureListener {
                    // Failed to save data
                }
        }
    }

    companion object {
        private const val RC_GOOGLE_SIGN_IN = 9001
    }
}
