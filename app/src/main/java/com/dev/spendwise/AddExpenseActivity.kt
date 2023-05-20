package com.dev.spendwise

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.dev.spendwise.databinding.ActivityAddExpenseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddExpenseActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    var binding: ActivityAddExpenseBinding? = null
    private var type: String? = null
    private var ct: String? = null
    private val expenseModel: ExpenseModel? = null
    var spinner: Spinner? = null
    var setListener: DatePickerDialog.OnDateSetListener? = null
    private var stmp: Long = 0
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater())
        setContentView(binding!!.getRoot())
        spinner = findViewById<Spinner>(R.id.category)
        val cal = Calendar.getInstance()
        val year = cal[Calendar.YEAR]
        val month = cal[Calendar.MONTH]
        val day = cal[Calendar.DAY_OF_MONTH]
        stmp = System.currentTimeMillis()
        spinner!!.setOnItemSelectedListener(this)
        loadSpinnerData()
        binding!!.saveExpense.setOnClickListener(View.OnClickListener { createExpense() })
        binding!!.cancelExpense.setOnClickListener(View.OnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        })
        binding!!.anc.setOnClickListener(View.OnClickListener {
            val i = Intent(this@AddExpenseActivity, ManageCatActivity::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        })
        type = getIntent().getStringExtra("type")
        if (type == "Income") {
            binding!!.incomRadio.setChecked(true)
        } else {
            binding!!.expenseRadio.setChecked(true)
        }
        binding!!.incomRadio.setOnClickListener(View.OnClickListener { type = "Income" })
        binding!!.expenseRadio.setOnClickListener(View.OnClickListener { type = "Expense" })
        binding!!.startdate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this@AddExpenseActivity,
                android.R.style.Theme_Material_Dialog_Alert,
                setListener,
                year,
                month,
                day
            )
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis())
            datePickerDialog.getWindow()
            datePickerDialog.show()
        }
        setListener =
            DatePickerDialog.OnDateSetListener { view: DatePicker?, year1: Int, month1: Int, dayOfMonth: Int ->
                val cal1 = Calendar.getInstance()
                cal1[Calendar.YEAR] = year1
                cal1[Calendar.MONTH] = month1
                cal1[Calendar.DAY_OF_MONTH] = dayOfMonth
                cal1[Calendar.HOUR_OF_DAY] = 12
                cal1[Calendar.MINUTE] = 0
                stmp = cal1.timeInMillis
                val mo = month1+1
                val date = "$dayOfMonth/$mo/$year1"
                binding!!.startdate.text = date
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun createExpense() {
        val expenseId = UUID.randomUUID().toString()
        val amount: String = binding!!.amount.getText().toString()
        val note: String = binding!!.note.getText().toString()
        //String category = binding.category.getText().toString();
        val incomeChecked: Boolean = binding!!.incomRadio.isChecked()
        type = if (incomeChecked) {
            "Income"
        } else {
            "Expense"
        }
        if (amount.trim { it <= ' ' }.length == 0) {
            binding!!.amount.setError("Empty")
            return
        }
        val expenseModel = ExpenseModel(
            expenseId, note, ct, type, amount.toLong(), stmp,
            FirebaseAuth.getInstance().getUid()
        )
        FirebaseFirestore
            .getInstance()
            .collection("expenses")
            .document(expenseId)
            .set(expenseModel)
        Toast.makeText(this@AddExpenseActivity, "Added", Toast.LENGTH_SHORT).show()
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        ct = parent.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    private fun loadSpinnerData() {
        val db = DatabaseHelper(getApplicationContext())
        val labels = db.allLabels
        val dataAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.setAdapter(dataAdapter)
        spinner?.setOnItemSelectedListener(this)
    }
}