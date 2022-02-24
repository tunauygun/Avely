package com.tuygun.shoppinglist

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.lang.reflect.Array

class ListAdapter: RecyclerView.Adapter<ListAdapter.ViewHolder>(){
    var data = listOf<List<String>>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val itemName = item[0]
        val isItemCheckedString = item[1]
        var isItemCheckedBoolean = isItemCheckedString.equals("true")

        holder.checkBox.text = itemName
        holder.checkBox.setChecked(isItemCheckedBoolean)

        if(holder.checkBox.isChecked){
            Log.i("ListAdapter", holder.checkBox.paintFlags.toString())
            holder.checkBox.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        } else{
            holder.checkBox.paintFlags = 1283
        }

        holder.deleteButton.setOnClickListener {
            DatabaseOperation.deleteItem(holder.checkBox.text.toString())
        }

        holder.checkBox.setOnClickListener {
            val checkbutton = it as CheckBox
            DatabaseOperation.updateListItemCheckedStatus(checkbutton.text.toString(), checkbutton.isChecked)
            if(checkbutton.isChecked){
                Log.i("ListAdapter", checkbutton.paintFlags.toString())
                checkbutton.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            } else{
                checkbutton.paintFlags = 1283
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_shopping_list, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val deleteButton :ImageView = itemView.findViewById(R.id.delete_button)
    }
}