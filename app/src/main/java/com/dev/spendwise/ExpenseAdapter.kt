package com.dev.spendwise

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.dev.spendwise.ExpenseAdapter.MyViewHolder
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(private val context: Context, onItemsClick: OnItemsClick) :
    RecyclerView.Adapter<MyViewHolder>() {
    private val onItemsClick: OnItemsClick
    fun add(expenseModel: ExpenseModel) {
        expenseModelList.add(expenseModel)
        notifyDataSetChanged()
    }

    fun clear() {
        expenseModelList.clear()
        notifyDataSetChanged()
    }

    private val expenseModelList: MutableList<ExpenseModel>

    init {
        expenseModelList = ArrayList()
        this.onItemsClick = onItemsClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expense_row, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val expenseModel = expenseModelList[position]
        val t = expenseModel.time
        val date = Date(t)
        val dateFormat = SimpleDateFormat("dd MMM yy", Locale.getDefault())
        val formattedDate = dateFormat.format(date)

       // holder.note.text = expenseModel.note

        if(position==0)
        {
            holder.date_tv?.isVisible = true
            holder.date_tv?.setText(formattedDate)
        }
        else{
            val expenseModel1 = expenseModelList[position-1]
            val t1 = expenseModel1.time
            val date1 = Date(t1)
            val dateFormat1 = SimpleDateFormat("dd MMM yy", Locale.getDefault())
            val formattedDate1 = dateFormat1.format(date1)
            if(formattedDate.equals(formattedDate1))
            {
                holder.date_tv?.isVisible= false
            }
            else{
                holder.date_tv?.isVisible= true
                holder.date_tv?.setText(formattedDate)
            }
        }
        val cat = expenseModel.category?.substring(0, 1)?.toUpperCase() + expenseModel.category?.substring(1);

        holder.category.text = cat
        holder.amount.text = "₹ " + expenseModel.amount.toString()
        holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        val priority: String
        priority = expenseModel.type.toString()
        if (priority == "Expense") {
            holder.amount.setTextColor(Color.parseColor("#E57373"))
        } else {
            holder.amount.setTextColor(Color.parseColor("#81C784"))
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, UpdateActivity::class.java)
            intent.putExtra("expenseId", expenseModelList[position].expenseId)
            val a = expenseModelList[position].amount.toString()
            intent.putExtra("amount", a)
            intent.putExtra("uid", expenseModelList[position].uid)
            intent.putExtra("category", expenseModelList[position].category)
            intent.putExtra("note", expenseModelList[position].note)
            intent.putExtra("type", expenseModelList[position].type)
            context.startActivity(intent)

        }
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return expenseModelList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView
      //  val note: TextView
      //  val date: TextView
        val amount: TextView
        var date_tv: TextView? = null

        init {
           // note = itemView.findViewById(R.id.note)
            category = itemView.findViewById(R.id.category)
           // date = itemView.findViewById(R.id.date)
            amount = itemView.findViewById(R.id.amount)
            date_tv = itemView.findViewById(R.id.d)
        }
    }
}