package com.dev.spendwise

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.spendwise.databinding.ActivityDashBoardBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DashBoardActivity : AppCompatActivity(), OnItemsClick {
    var binding: ActivityDashBoardBinding? = null
    private var expenseAdapter: ExpenseAdapter? = null
    private var income: Long = 0
    private var expense: Long = 0
    private var backPresseed: Long = 0
    private lateinit var popupLauncher: ActivityResultLauncher<Intent>
    private var overlayView: View? = null
    var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    var map = mutableMapOf<String, Long>()
    var map1: MutableMap<String, Long> = HashMap()
    var intent1: Intent? = null
    var i:Intent? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashBoardBinding.inflate(getLayoutInflater())
        setContentView(binding!!.getRoot())
        expenseAdapter = ExpenseAdapter(this, this)
        binding!!.recycler.setAdapter(expenseAdapter)
        binding!!.recycler.setLayoutManager(LinearLayoutManager(this))
        intent = Intent(this@DashBoardActivity, AddExpenseNew::class.java)
        intent1 = Intent(this@DashBoardActivity, summaryActivity::class.java)

        overlayView = View(this)
        overlayView?.setBackgroundColor(Color.parseColor("#70000000"))

        popupLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            binding!!.getRoot().removeView(overlayView)
        }

        val sdf = SimpleDateFormat("MMMM YYY")
        val month = sdf.format(Date())
        val currentUser: FirebaseUser? = mAuth.getCurrentUser()
        val email: String? = currentUser?.getEmail()
        val index = email?.indexOf("@")
        val shortEmail = index?.let { email?.substring(0, it)?.uppercase(Locale.getDefault()) }
        val shortEmail1 = index?.let { email.substring(0, it) }
        val intial = shortEmail?.substring(0, 1)
        val capemail = intial + shortEmail1?.substring(1)

        binding!!.tr.setText(month)
        binding!!.initialText1.setText(intial)
        binding!!.usercard.setOnClickListener(View.OnClickListener {
            binding!!.getRoot().addView(overlayView)
            i = Intent(this,popup::class.java)
            popupLauncher.launch(i)
        })
        val bottomNavigationView: BottomNavigationView = binding!!.bottonnav
        bottomNavigationView.setSelectedItemId(R.id.dashboard)
        bottomNavigationView.setOnNavigationItemSelectedListener(object :
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.dashboard -> return true
                    R.id.summary -> {
                        intent1!!.putExtra("income", income.toString())
                        intent1!!.putExtra("expense", expense.toString())
                        intent1!!.putExtra("balance", (income - expense).toString())
                        intent1!!.putExtra("month", month)
                        startActivity(intent1)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        return true
                    }
                }
                return false
            }
        })
        binding!!.addExpense.setOnClickListener(View.OnClickListener {
           binding!!.getRoot().addView(overlayView)
            intent!!.putExtra(
                "type", "Expense"
            )
            popupLauncher.launch(intent)
        })
    }

    override fun onBackPressed() {
        if (backPresseed + 3000 > System.currentTimeMillis()) {

            this.finishAffinity()
        } else {
            Toast.makeText(this@DashBoardActivity, "Press back again to exit", Toast.LENGTH_SHORT)
                .show()
        }
        backPresseed = System.currentTimeMillis()
    }

    protected override fun onStart() {
        val bottomNavigationView: BottomNavigationView = binding!!.bottonnav
        super.onStart()
        binding!!.pbar.setVisibility(View.VISIBLE)
        bottomNavigationView.setSelectedItemId(R.id.dashboard)
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(object : OnSuccessListener<AuthResult?> {
                    override fun onSuccess(p0: AuthResult?) {}
                })
                .addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(e: Exception) {
                        Toast.makeText(this@DashBoardActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    protected override fun onResume() {
        super.onResume()
        val bottomNavigationView: BottomNavigationView = binding!!.bottonnav
        bottomNavigationView.setSelectedItemId(R.id.dashboard)
        income = 0
        expense = 0
        getData()
    }

        private fun getData() {
            val cal = Calendar.getInstance()
            val year = cal[Calendar.YEAR]
            val month = cal[Calendar.MONTH]
            cal[Calendar.YEAR] = year
            cal[Calendar.MONTH] = month
            cal[Calendar.DAY_OF_MONTH] = 0
            val startTimestamp = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            val endTimestamp = cal.timeInMillis
            FirebaseFirestore
                .getInstance()
                .collection("expenses")
                .whereEqualTo("uid", FirebaseAuth.getInstance().getUid())
                .whereGreaterThanOrEqualTo("time", startTimestamp)
                .whereLessThan("time", endTimestamp)
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(object : OnSuccessListener<QuerySnapshot? > {
                    override fun onSuccess(queryDocumentSnapshots: QuerySnapshot?) {
                        binding!!.pbar.setVisibility(View.GONE)
                        expenseAdapter!!.clear()
                        val dslist: List<DocumentSnapshot> = queryDocumentSnapshots!!.getDocuments()
                        for (ds in dslist) {
                            val expenseModel: ExpenseModel? =
                                ds.toObject<ExpenseModel>(ExpenseModel::class.java)
                            val cat = expenseModel!!.category
                            val tt = expenseModel!!.type
                            if (expenseModel!!.type == "Expense") {
                                if (map.containsKey(cat)) {
                                    val existingValue = map[cat]
                                    val newValue = existingValue!! + expenseModel!!.amount
                                    map[cat.toString()] = newValue
                                } else {
                                    map[cat.toString()] = expenseModel!!.amount
                                }
                            } else {
                                if (map1.containsKey(cat)) {
                                    val existingValue = map1[cat]
                                    val newValue = existingValue!! + expenseModel!!.amount
                                    map1[cat.toString()] = newValue
                                } else {
                                    map1[cat.toString()] = expenseModel!!.amount
                                }
                            }
                            if (expenseModel!!.type == "Income") {
                                income += expenseModel!!.amount
                            } else {
                                expense += expenseModel.amount
                            }
                            expenseAdapter!!.add(expenseModel)
                        }
                        val total = income - expense
                        val formatter = NumberFormat.getInstance(Locale("en", "IN"))
                        binding!!.sBalance.setText("₹" + formatter.format(total))
                        binding!!.sExpense.setText("₹"+formatter.format(expense))
                        binding!!.sIncome.setText("₹"+formatter.format(income))
                        setUpGraph()
                        setUpGraph1()
                    }
                })
        }

    private fun setUpGraph() {
        val pieEntryList: MutableList<PieEntry> = ArrayList<PieEntry>()
        val colorsList: MutableList<Int> = ArrayList()
        for ((key, value) in map) {
            pieEntryList.add(PieEntry(value.toFloat(), key))
            val c = getrandomcolor()
            colorsList.add(c)
        }
        if (!pieEntryList.isEmpty()) {
            val pieDataSet = PieDataSet(pieEntryList, "")
            pieDataSet.setColors(colorsList)
            val pieData = PieData(pieDataSet)
            pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE)
            pieDataSet.setValueLineColor(Color.parseColor("#f8f8f8"))
            pieDataSet.setSliceSpace(3f)
            pieDataSet.setValueTextSize(12f)
            pieDataSet.setValueTextColor(Color.parseColor("#000000"))


            binding!!.pieChart.setData(pieData)
            binding!!.pieChart.invalidate()
            binding!!.pieChart.animateXY(1000, 1000)
            binding!!.pieChart.setHoleRadius(68f)
            binding!!.pieChart.setCenterText("Expense\n ₹$expense")
            binding!!.pieChart.setCenterTextSize(17f)
            binding!!.pieChart.setCenterTextColor(Color.parseColor("#2bb0c0"))
            binding!!.pieChart.setRotationAngle(90f)
            binding!!.pieChart.setExtraTopOffset(5f)
            binding!!.pieChart.setExtraBottomOffset(6f)
            binding!!.pieChart.getLegend().setEnabled(false)
            binding!!.pieChart.getDescription().setText(" ")
            binding!!.pieChart.setHoleColor(373535)
            binding!!.pieChart.setUsePercentValues(true)
        }
    }

    private fun getrandomcolor(): Int {
        val rand = Random()
        /* int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        return color;*/
        val red = 128 + rand.nextInt(128)
        val green = 128 + rand.nextInt(128)
        val blue = 128 + rand.nextInt(128)
        val alpha = 255
        return alpha shl 24 or (red shl 16) or (green shl 8) or blue
    }

    override fun onClick(expenseModel: ExpenseModel?) {
        intent!!.putExtra("model", expenseModel)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun setUpGraph1() {
        val pieEntryList: MutableList<PieEntry> = ArrayList<PieEntry>()
        val colorsList: MutableList<Int> = ArrayList()
        for ((key, value) in map1) {
            pieEntryList.add(PieEntry(value.toFloat(), key))
            val c = getrandomcolor()
            colorsList.add(c)
        }
        if (!pieEntryList.isEmpty()) {
            val pieDataSet = PieDataSet(pieEntryList, "")
            pieDataSet.setColors(colorsList)
            val pieData = PieData(pieDataSet)
            pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE)
            pieDataSet.setValueLineColor(Color.parseColor("#f8f8f8"))
            pieDataSet.setSliceSpace(3f)
            pieDataSet.setValueTextSize(10f)
            pieDataSet.setValueTextColor(Color.parseColor("#000000"))

            binding!!.pieChart1.setData(pieData)
            binding!!.pieChart1.invalidate()
            binding!!.pieChart1.animateXY(1000, 1000)
            binding!!.pieChart1.setHoleRadius(68f)
            binding!!.pieChart1.setCenterText("Income\n ₹$income")
            binding!!.pieChart1.setCenterTextColor(Color.parseColor("#2bb0c0"))
            binding!!.pieChart1.setCenterTextSize(17f)
            binding!!.pieChart1.setRotationAngle(102f)
            binding!!.pieChart1.setExtraTopOffset(5f)
            binding!!.pieChart1.setExtraBottomOffset(6f)
            binding!!.pieChart1.getLegend().setEnabled(false)
            binding!!.pieChart1.getDescription().setText(" ")
            binding!!.pieChart1.setHoleColor(373535)
            binding!!.pieChart1.setUsePercentValues(true)
        }
    }



}