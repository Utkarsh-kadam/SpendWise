package com.dev.spendwise

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.spendwise.databinding.ActivityTransactionBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.text.NumberFormat
import java.util.*

class TransactionActivity : AppCompatActivity(), OnItemsClick {
    var binding: ActivityTransactionBinding? = null
    private var expenseAdapter: ExpenseAdapter? = null
    private var income: Long = 0
    private var expense: Long = 0
    var setListener: DatePickerDialog.OnDateSetListener? = null
    var setListener1: DatePickerDialog.OnDateSetListener? = null
    private var sbutton: Button? = null
    private var ebutton: Button? = null
    private var stmp: Long = 0
    private var etmp: Long = 0

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionBinding.inflate(getLayoutInflater())
        setContentView(binding!!.getRoot())
        expenseAdapter = ExpenseAdapter(this, this)
        binding!!.recycler.setAdapter(expenseAdapter)
        binding!!.recycler.setLayoutManager(LinearLayoutManager(this))
        sbutton = findViewById<Button>(R.id.startdate)
        ebutton = findViewById<Button>(R.id.endDate)
        val cal = Calendar.getInstance()
        val year = cal[Calendar.YEAR]
        val month = cal[Calendar.MONTH]
        val day = cal[Calendar.DAY_OF_MONTH]
        val cal2 = Calendar.getInstance()
        etmp = cal2.timeInMillis
        sbutton!!.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this@TransactionActivity,
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
        ebutton!!.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this@TransactionActivity,
                android.R.style.Theme_Material_Dialog_Alert,
                setListener1,
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
                cal1[Calendar.HOUR_OF_DAY] = 0
                cal1[Calendar.MINUTE] = 0
                stmp = cal1.timeInMillis
                val mo = month1+1
                val date = "$dayOfMonth/$mo/$year1"
                sbutton!!.text = date
            }
        setListener1 =
            DatePickerDialog.OnDateSetListener { view: DatePicker?, year12: Int, month12: Int, dayOfMonth1: Int ->
                val cal1 = Calendar.getInstance()
                cal1[Calendar.YEAR] = year12
                cal1[Calendar.MONTH] = month12
                cal1[Calendar.DAY_OF_MONTH] = dayOfMonth1
                etmp = cal1.timeInMillis
                val m = month12+1
                val date = "$dayOfMonth1/$m/$year12"
                ebutton!!.text = date
            }
        binding!!.view.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                income = 0
                expense = 0
                getData()
            }
        })
    }

    protected override fun onStart() {
        super.onStart()
        binding!!.pbar.setVisibility(View.VISIBLE)
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(object : OnSuccessListener<AuthResult?> {
                    override fun onSuccess(p0: AuthResult?) {}
                })
                .addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(e: Exception) {
                        Toast.makeText(this@TransactionActivity, e.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                })
        }
    }

    protected override fun onResume() {
        super.onResume()
        income = 0
        expense = 0
        getData()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

   private fun getData(){
            FirebaseFirestore
                .getInstance()
                .collection("expenses")
                .whereEqualTo("uid", FirebaseAuth.getInstance().getUid())
                .whereGreaterThanOrEqualTo("time", stmp)
                .whereLessThan("time", etmp)
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(object : OnSuccessListener<QuerySnapshot? > {
                    override fun onSuccess(queryDocumentSnapshots: QuerySnapshot? ) {
                        binding!!.pbar.setVisibility(View.GONE)
                        expenseAdapter!!.clear()
                        val dslist: List<DocumentSnapshot> = queryDocumentSnapshots!!.getDocuments()
                        for (ds in dslist) {
                            val expenseModel: ExpenseModel? =
                                ds.toObject<ExpenseModel>(ExpenseModel::class.java)
                            if (expenseModel!!.type == "Income") {
                                income += expenseModel!!.amount
                            } else {
                                expense += expenseModel!!.amount
                            }
                            expenseAdapter!!.add(expenseModel!!)
                        }
                        val total = income - expense
                        val formatter = NumberFormat.getInstance(Locale("en", "IN"))
                        binding!!.sBalance.setText("₹" + formatter.format(total))
                        binding!!.sExpense.setText("₹"+formatter.format(expense))
                        binding!!.sIncome.setText("₹"+formatter.format(income))
                    }
                })
        }

    override fun onClick(expenseModel: ExpenseModel?) {
        var intent: Intent = Intent(this@TransactionActivity, AddExpenseActivity::class.java)
        intent.putExtra("model", expenseModel)
        startActivity(intent)
    }
}