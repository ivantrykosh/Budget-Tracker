package com.ivantrykosh.app.budgettracker.client.presentation.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.domain.model.Account
import com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.OnAccountClickListener
import java.text.DecimalFormat

/**
 * Account item adapter
 */
class AccountItemAdapter(
    private val context: Context,
    private val dataset: List<Account>,
) : RecyclerView.Adapter<AccountItemAdapter.AccountItemViewHolder>() {

    private var clickListener: OnAccountClickListener? = null
    fun setOnAccountClickListener(listener: OnAccountClickListener) {
        this.clickListener = listener
    }

    class AccountItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.item_account_name)
        val totalValue: TextView = view.findViewById(R.id.item_account_total_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)
        return AccountItemViewHolder(adapterLayout)
    }

    override fun getItemCount() = dataset.size
    override fun onBindViewHolder(holder: AccountItemViewHolder, position: Int) {
        val pattern = Constants.CURRENCIES[AppPreferences.currency] + "#,##0.00"
        val format = DecimalFormat(pattern)
        format.maximumFractionDigits = 2

        val item = dataset[position]
        holder.name.text = item.name
        holder.totalValue.text = format.format(item.total)

        holder.itemView.setOnClickListener {
            clickListener?.onAccountClick(item)
        }
    }
}