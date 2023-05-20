package com.dev.spendwise

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.dev.spendwise.databinding.ActivityPopupBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.*

class popup : AppCompatActivity() {
    var binding: ActivityPopupBinding? = null
    var d : DisplayMetrics? = DisplayMetrics()
    var mAuth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopupBinding.inflate(getLayoutInflater())
        setContentView(binding!!.getRoot())
        window.setLayout(850,700)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val currentUser: FirebaseUser? = mAuth.getCurrentUser()
        val email: String? = currentUser?.getEmail()

        val index = email?.indexOf("@")
        val shortEmail = index?.let { email?.substring(0, it)?.uppercase(Locale.getDefault()) }
        val shortEmail1 = index?.let { email?.substring(0, it) }
        val intial = shortEmail?.substring(0, 1)
        val capemail = intial + shortEmail1?.substring(1)

        binding!!.emailText.setText(email)
        binding!!.shortEmailText.setText(capemail)
        binding!!.initialText.setText(intial)


    }
    fun createSignOutDialog(view: View?) {
        val builder = MaterialAlertDialogBuilder(this@popup, R.style.RoundShapeTheme)
        builder.setTitle("Log Out")
            .setMessage("Are you sure you want to Log Out ?")
            .setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    mAuth.signOut()
                    startActivity(Intent(getApplicationContext(), MainActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            })
            .setNegativeButton("No", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.cancel()
                }
            })
        builder.create().show()
    }

}