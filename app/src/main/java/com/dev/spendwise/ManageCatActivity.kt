package com.dev.spendwise

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.spendwise.databinding.ActivityManageCatBinding
import com.google.android.material.button.MaterialButton
import java.util.*

class ManageCatActivity : AppCompatActivity() {
    var binding: ActivityManageCatBinding? = null
    var mRecyclerView: RecyclerView? = null
    var btnAdd: MaterialButton? = null
    var inputLabel: EditText? = null
    var labels: List<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageCatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        btnAdd = findViewById(R.id.btn_add1)
        inputLabel = findViewById(R.id.input_label)
        loadSpinnerData()
        btnAdd!!.setOnClickListener(View.OnClickListener {
            val label = inputLabel!!.getText().toString().lowercase(Locale.getDefault())
            if (label.trim { it <= ' ' }.length > 0) {
                if (labels!!.contains(label)) {
                    Toast.makeText(this@ManageCatActivity, "Already Present", Toast.LENGTH_SHORT)
                        .show()
                    return@OnClickListener
                }
                val db = DatabaseHelper(applicationContext)
                db.insertLabel(label)
                inputLabel!!.setText("")
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(inputLabel!!.getWindowToken(), 0)
                Toast.makeText(this@ManageCatActivity, "$label Added", Toast.LENGTH_SHORT).show()
                loadSpinnerData()
            } else {
                Toast.makeText(
                    applicationContext, "Please enter label name",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun loadSpinnerData() {
        val db = DatabaseHelper(applicationContext)
        labels = db.allLabels
        mRecyclerView = findViewById(R.id.cat)
        mRecyclerView!!.setLayoutManager(LinearLayoutManager(this))
        Collections.sort(labels)
        val adapter = CatAdaptor(applicationContext, labels as MutableList<String>)
        mRecyclerView!!.setAdapter(adapter)
    }
}