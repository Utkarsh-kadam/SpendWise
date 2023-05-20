package com.dev.spendwise

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.spendwise.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    var binding: ActivityMainBinding? = null
    var firebaseAuth: FirebaseAuth? = null
    private var backPresseed: Long = 0
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(getLayoutInflater())
        setContentView(binding!!.getRoot())
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth!!.addAuthStateListener(object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    try {
                        startActivity(Intent(this@MainActivity,DashBoardActivity::class.java))
                        finish()
                    } catch (e: Exception) {

                    }
                }
            }
        })
        binding!!.goToSignUpScreen.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@MainActivity, SignUpActivity::class.java)
            try {
                startActivity(intent)
            } catch (e: Exception) {
            }
        })
        binding!!.btnLogin.setOnClickListener(View.OnClickListener {
            val email: String = binding!!.emailLogin.getText().toString().trim()
            val password: String = binding!!.passwordLogin.getText().toString().trim()
            if (email.length <= 0 || password.length <= 0) {
                return@OnClickListener
            }
            binding!!.passwordLogin.onEditorAction(EditorInfo.IME_ACTION_DONE)
            binding!!.pbar.setVisibility(View.VISIBLE)
            firebaseAuth!!.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(object : OnSuccessListener<AuthResult?> {
                    override fun onSuccess(authResult: AuthResult?) {
                        try {
                            startActivity(Intent(this@MainActivity, DashBoardActivity::class.java))
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                .addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(e: Exception) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                        binding!!.pbar.setVisibility(View.GONE)
                    }
                })
        })
    }

    override fun onBackPressed() {
        if (backPresseed + 3000 > System.currentTimeMillis()) {
            this.finishAffinity()
        } else {
            Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
        backPresseed = System.currentTimeMillis()
    }

    fun ShowHidePass(view: View) {
        val et: EditText = findViewById<EditText>(R.id.password_login)
        if (view.id == R.id.eye) {
            if (et.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                // ((ImageView(view)).setImageResource(R.drawable.hide_password);
                //Show Password
                et.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
            } else {
                // ((ImageView)(view)).setImageResource(R.drawable.show_password);
                //Hide Password
                et.setTransformationMethod(PasswordTransformationMethod.getInstance())
            }
        }
    }
}