package com.dev.spendwise

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CatAdaptor(var context: Context, private val labels: MutableList<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            keyTextView = itemView.findViewById(R.id.cat_tv)
            imgview = itemView.findViewById(R.id.del_cat)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.cat_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val key = labels[position]
        keyTextView!!.text = key
        holder.setIsRecyclable(false)
        imgview!!.setOnClickListener {
            delete_cat(key)
        }
    }

    private fun delete_cat(label: String) {
        val db = DatabaseHelper(context)
        db.deleteLabel(label)
        Toast.makeText(context, "$label Category deleted", Toast.LENGTH_SHORT).show()
        labels.remove(label)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return labels.size
    }

    fun clear() {
        labels.clear()
        notifyDataSetChanged()
    }

    companion object {
        var keyTextView: TextView? = null
        var imgview: ImageView? = null
    }
}