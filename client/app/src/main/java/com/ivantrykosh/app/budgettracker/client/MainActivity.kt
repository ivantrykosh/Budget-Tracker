package com.ivantrykosh.app.budgettracker.client

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.ivantrykosh.app.budgettracker.client.adapter.OperationItemAdapter
import com.ivantrykosh.app.budgettracker.client.data.Datasource
import com.ivantrykosh.app.budgettracker.client.databinding.ActivityMainBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set on click listener for hamburger menu
        binding.topAppBar.setNavigationOnClickListener {
            Toast.makeText(this, "Hello!", Toast.LENGTH_SHORT).show()
        }

        // Set on click listener for main FAB
        binding.mainFloatingActionButton.setOnClickListener {
            Toast.makeText(this, "Hello!", Toast.LENGTH_SHORT).show()
        }

        // Set text for balance
        val stringDate = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())
        binding.textViewBalanceFor.text = getString(R.string.balance_for, stringDate)

        // Set text for incomes, expenses and total values
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        format.currency = Currency.getInstance("USD")

        val myDatasetBalanceOverview = Datasource().loadBalanceOverview()
        binding.mainIncomesValue.text = format.format(myDatasetBalanceOverview.incomes)
        binding.mainExpensesValue.text = format.format(myDatasetBalanceOverview.expenses)
        binding.mainTotalValue.text = format.format(myDatasetBalanceOverview.total)

        // Load data about incomes to recycler view
        val myDatasetIncomes = Datasource().loadOperations()
        binding.lastIncomesRecyclerView.adapter = OperationItemAdapter(this, myDatasetIncomes, 2)
        binding.lastIncomesRecyclerView.setHasFixedSize(true)

        // Load data about expenses to recycler view
        val myDatasetExpenses = Datasource().loadOperations()
        binding.lastExpensesRecyclerView.adapter = OperationItemAdapter(this, myDatasetExpenses, 2)
        binding.lastExpensesRecyclerView.setHasFixedSize(true)

//        TODO("Add FAB button for extensible FABs (add income and add expense)")
    }
}