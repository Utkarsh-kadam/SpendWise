package com.dev.spendwise

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val mMap: MutableMap<String?, Long>,private val total:Long?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            keyTextView = itemView.findViewById(R.id.textView1)
            valueView = itemView.findViewById(R.id.textView2)
            percentView = itemView.findViewById(R.id.textView3)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val key = mMap.keys.toTypedArray()[position]
        val cat = key?.substring(0, 1)?.toUpperCase() + key?.substring(1)
        val value = mMap[key]
        val percentage = (total?.let { value?.toFloat()?.div(total.toFloat()) })?.times(100)
        Log.d("total percent",percentage.toString())
        keyTextView!!.text = cat
        valueView!!.text = "₹" + value.toString()
        percentView!!.text =  String.format("%.1f", percentage)+"%"

    }

    override fun getItemCount(): Int {
        return mMap.size
    }

    companion object {
        var keyTextView: TextView? = null
        var valueView: TextView? = null
        var percentView: TextView? = null

    }
}