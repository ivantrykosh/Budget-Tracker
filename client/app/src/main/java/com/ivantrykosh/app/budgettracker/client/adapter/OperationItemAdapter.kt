package com.ivantrykosh.app.budgettracker.client.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.model.Operation
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Currency

class OperationItemAdapter(
    private val context: Context,
    private val dataset: List<Operation>,
    private val size: Int
) : RecyclerView.Adapter<OperationItemAdapter.OperationItemViewHolder>() {

    class OperationItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val category: TextView = view.findViewById(R.id.item_operation_category)
        val value: TextView = view.findViewById(R.id.item_operation_value)
        val note: TextView = view.findViewById(R.id.item_operation_note)
        val date: TextView = view.findViewById(R.id.item_operation_date)
        val color: View = view.findViewById(R.id.item_operation_color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_operation_details, parent, false)
        return OperationItemViewHolder(adapterLayout)
    }

    override fun getItemCount() = size

    override fun onBindViewHolder(holder: OperationItemViewHolder, position: Int) {
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        format.currency = Currency.getInstance("USD")

        val item = dataset[position]
        holder.category.text = item.category
        holder.value.text = format.format(item.value)
        holder.note.text = item.note
        holder.date.text = item.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        if (item.value > 0) {
            holder.color.setBackgroundResource(R.color.green)
        } else {
            holder.color.setBackgroundResource(R.color.red)
        }
    }
}