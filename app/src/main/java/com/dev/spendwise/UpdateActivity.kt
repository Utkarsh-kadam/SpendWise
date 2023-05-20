package com.dev.spendwise

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.spendwise.databinding.ActivityUpdateBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class UpdateActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    var binding: ActivityUpdateBinding? = null
    var newType: String? = null
    var ct: String? = null
    var expenseId: String? = null
    var firebaseFirestore: FirebaseFirestore? = null
    var firebaseAuth: FirebaseAuth? = null
    var spinner: Spinner? = null
    var time:Long? =0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        spinner = findViewById(R.id.category)
        spinner!!.setOnItemSelectedListener(this)
        expenseId = intent.getStringExtra("expenseId")
        val amount = intent.getStringExtra("amount")
        val note = intent.getStringExtra("note")
        val category = intent.getStringExtra("category")
        val type = intent.getStringExtra("type")




        loadSpinnerData(category)
        binding!!.amount.inputType = InputType.TYPE_CLASS_NUMBER
        binding!!.amount.setText(amount)
        binding!!.note.setText(note)
        when (type) {
            "Income" -> {
                newType = "Income"
                binding!!.incomRadio.isChecked = true
            }
            "Expense" -> {
                newType = "Expense"
                binding!!.expenseRadio.isChecked = true
            }
        }
        binding!!.anc.setOnClickListener {
            val i = Intent(this@UpdateActivity, ManageCatActivity::class.java)
            startActivity(i)
        }
        binding!!.incomRadio.setOnClickListener {
            newType = "Income"
            binding!!.incomRadio.isChecked = true
            binding!!.expenseRadio.isChecked = false
        }
        binding!!.expenseRadio.setOnClickListener {
            newType = "Expense"
            binding!!.expenseRadio.isChecked = true
            binding!!.incomRadio.isChecked = false
        }
        binding!!.updateExpense.setOnClickListener {
            val amount = binding!!.amount.text.toString()
            val note = binding!!.note.text.toString()
            //String category = binding.category.getText().toString();
            FirebaseFirestore
                .getInstance()
                .collection("expenses")
                .document(expenseId!!)
                .update("amount", amount.toLong(), "note", note, "category", ct, "type", newType)
                .addOnSuccessListener {
                    onBackPressed()
                    Toast.makeText(this@UpdateActivity, "Updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@UpdateActivity,
                        e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        binding!!.deleteExpense.setOnClickListener { v -> createDeleteDialog(v) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        ct = parent.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    private fun loadSpinnerData(category: String?) {
        val db = DatabaseHelper(applicationContext)
        val labels = db.allLabels
        val dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner!!.adapter = dataAdapter
        spinner!!.onItemSelectedListener = this
        for (i in 0 until dataAdapter.count) {
            if (dataAdapter.getItem(i) == category) {
                spinner!!.setSelection(i)
                break
            }
        }
    }

    private fun delete() {
        FirebaseFirestore
            .getInstance()
            .collection("expenses")
            .document(expenseId!!)
            .delete()
        Toast.makeText(this@UpdateActivity, "Deleted", Toast.LENGTH_SHORT).show()
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun createDeleteDialog(view: View?) {
        val builder = MaterialAlertDialogBuilder(this@UpdateActivity, R.style.RoundShapeTheme)
        builder.setTitle("Delete")
            .setMessage("Are you sure you want to delete ?")
            .setPositiveButton("Yes") { dialog, which ->
                delete()
                startActivity(Intent(applicationContext, MainActivity::class.java))
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
            .setNegativeButton("No") { dialog, which -> dialog.cancel() }
        builder.create().show()
    }
}