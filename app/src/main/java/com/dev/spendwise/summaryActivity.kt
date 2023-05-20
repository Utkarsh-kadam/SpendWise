package com.dev.spendwise

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.spendwise.databinding.ActivitySummaryBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class summaryActivity : AppCompatActivity() {
    var binding: ActivitySummaryBinding? = null
    var t1: TextView? = null
    var t2: TextView? = null
    var t3: TextView? = null
    var t4: TextView? = null
    var firebaseAuth: FirebaseAuth? = null
    var firebaseFirestore: FirebaseFirestore? = null
    var map: MutableMap<String?, Long> = HashMap()
    var mRecyclerView: RecyclerView? = null
    private val expenseAdapter: ExpenseAdapter? = null
    var month: String? = null
    var total_expense :Long? = 0
    val NOTIFICATION_CHANNEL_ID = "10001"
    private val default_notification_channel_id = "default"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        getData()
        month = intent.getStringExtra("month")
        total_expense = intent.getStringExtra("expense")?.toLong()
        t4 = findViewById(R.id.month_tv)
        t4!!.setText("$month Summary")
        val switc = findViewById<MaterialSwitch>(R.id.reminder_toggle)
        val pref = getSharedPreferences("save", MODE_PRIVATE)
        switc.isChecked = pref.getBoolean("first", false)
        switc.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val notificationCompat = NotificationManagerCompat.from(applicationContext)
                if (notificationCompat.areNotificationsEnabled()) {
                    Toast.makeText(
                        this@summaryActivity,
                        "Daily reminder notification Turned on !",
                        Toast.LENGTH_SHORT
                    ).show()
                    val editor = getSharedPreferences("save", MODE_PRIVATE).edit()
                    editor.putBoolean("first", true)
                    editor.apply()
                    switc.isChecked = true
                    val c = Calendar.getInstance()
                    c[Calendar.HOUR_OF_DAY] = 22
                    c[Calendar.MINUTE] = 0
                    val delay = c.timeInMillis - System.currentTimeMillis()
                    scheduleNotification(getNotification("Add expense"), delay)
                } else {
                    val intent = Intent()
                    intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
                    startActivity(intent)
                    switc.isChecked = false
                }
            } else {
                Toast.makeText(
                    this@summaryActivity,
                    "Daily reminder notification turned off !",
                    Toast.LENGTH_SHORT
                ).show()
                val editor = getSharedPreferences("save", MODE_PRIVATE).edit()
                editor.putBoolean("first", false)
                editor.apply()
                switc.isChecked = false
            }
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottonnav)
        bottomNavigationView.selectedItemId = R.id.summary
        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard -> {
                    startActivity(Intent(applicationContext, DashBoardActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.summary -> return@OnNavigationItemSelectedListener true
            }
            false
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun all(view: View?) {
        val intent = Intent(this@summaryActivity, TransactionActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    fun manage(view: View?) {
        val intent = Intent(this@summaryActivity, ManageCatActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun scheduleNotification(notification: Notification, delay: Long) {
        val notificationIntent = Intent(this, MyNotificationPublisher::class.java)
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION_ID, 1)
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION, notification)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = (getSystemService(ALARM_SERVICE) as AlarmManager)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            delay,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun getNotification(content: String): Notification {
        val builder = NotificationCompat.Builder(this, default_notification_channel_id)
        builder.setContentTitle("Reminder: Add today's Expenses")
        builder.setContentText(content)
        builder.setSmallIcon(R.drawable.swlogo)
        builder.setAutoCancel(true)
        builder.setChannelId(NOTIFICATION_CHANNEL_ID)
        return builder.build()
    }

    // Set up the adapter

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
                .whereEqualTo("uid", FirebaseAuth.getInstance().uid)
                .whereGreaterThanOrEqualTo("time", startTimestamp)
                .whereLessThan("time", endTimestamp)
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    val dslist = queryDocumentSnapshots.documents
                    for (ds in dslist) {
                        val expenseModel = ds.toObject(ExpenseModel::class.java)
                        val cat = expenseModel!!.category
                        if (map.containsKey(cat)) {
                            val existingValue = map[cat]
                            val newValue = existingValue!! + expenseModel.amount
                            map[cat] = newValue
                        } else if(expenseModel.type=="Expense") {
                            map[cat] = expenseModel.amount
                        }
                    }
                    mRecyclerView = findViewById(R.id.listView)
                    mRecyclerView!!.setLayoutManager(LinearLayoutManager(applicationContext))
                    map = sortByValue(map)

                    // Set up the adapter
                    val adapter = MyAdapter(map,total_expense)
                    mRecyclerView!!.setAdapter(adapter)
                }
        }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "10001"
        private const val default_notification_channel_id = "default"
        fun sortByValue(hm: Map<String?, Long>): HashMap<String?, Long> {
            val list: List<Map.Entry<String?, Long>> = LinkedList(hm.entries)
            Collections.sort(list) { (_, value), (_, value1) ->
                value1.compareTo(
                    value
                )
            }
            val temp: HashMap<String?, Long> = LinkedHashMap()
            for ((key, value) in list) {
                temp[key] = value
            }
            return temp
        }
    }
}