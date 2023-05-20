package com.dev.spendwise

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.spendwise.databinding.ActivitySignUpBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity() : AppCompatActivity() {
    var binding: ActivitySignUpBinding? = null
    var firebaseAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        firebaseAuth = FirebaseAuth.getInstance()
        binding!!.goToLoginScreen.setOnClickListener {
            val intent = Intent(this@SignUpActivity, MainActivity::class.java)
            try {
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } catch (e: Exception) {
            }
        }
        binding!!.btnSignUp.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val email = binding!!.emailForSignUp.text.toString()
                val password = binding!!.passwordForSignUp.text.toString()
                if (email.trim { it <= ' ' }.length <= 0 || password.trim { it <= ' ' }.length <= 0) {
                    return
                }
                binding!!.pbar.visibility = View.VISIBLE
                firebaseAuth!!.createUserWithEmailAndPassword(email, password).addOnSuccessListener(
                    OnSuccessListener {
                        Toast.makeText(
                            this@SignUpActivity,
                            "User Created",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                    .addOnFailureListener(object : OnFailureListener {
                        override fun onFailure(e: Exception) {
                            binding!!.pbar.visibility = View.GONE
                            Toast.makeText(this@SignUpActivity, e.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
            }
        })
    }

    fun ShowHidePass(view: View) {
        val et = findViewById<EditText>(R.id.password_for_signUp)
        if (view.id == R.id.eye) {
            if ((et.transformationMethod == PasswordTransformationMethod.getInstance())) {
                // ((ImageView(view)).setImageResource(R.drawable.hide_password);
                //Show Password
                et.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                // ((ImageView)(view)).setImageResource(R.drawable.show_password);
                //Hide Password
                et.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
    }
}