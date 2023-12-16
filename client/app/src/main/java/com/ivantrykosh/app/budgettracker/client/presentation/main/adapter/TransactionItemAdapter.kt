package com.ivantrykosh.app.budgettracker.client.presentation.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Locale

class TransactionItemAdapter(
    private val context: Context,
    private val dataset: List<Transaction>,
    private val size: Int
) : RecyclerView.Adapter<TransactionItemAdapter.TransactionItemViewHolder>() {

    class TransactionItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val category: TextView = view.findViewById(R.id.item_operation_category)
        val value: TextView = view.findViewById(R.id.item_operation_value)
        val account: TextView = view.findViewById(R.id.item_operation_account)
        val date: TextView = view.findViewById(R.id.item_operation_date)
        val color: View = view.findViewById(R.id.item_operation_color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_operation_details, parent, false)
        return TransactionItemViewHolder(adapterLayout)
    }

    override fun getItemCount() = size

    override fun onBindViewHolder(holder: TransactionItemViewHolder, position: Int) {
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        format.currency = Currency.getInstance(
            AppPreferences.currency ?: "USD"
        )

        val item = dataset[position]
        holder.category.text = item.category
        holder.value.text = format.format(item.value)
        holder.account.text = item.accountName
        holder.date.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(item.date)
        if (item.value > 0) {
            holder.color.setBackgroundResource(R.color.green)
        } else {
            holder.color.setBackgroundResource(R.color.red)
        }
    }
}