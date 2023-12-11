package com.ivantrykosh.app.budgettracker.client

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ivantrykosh.app.budgettracker.client.databinding.ActivityAddIncomeBinding
import com.ivantrykosh.app.budgettracker.client.databinding.ActivityMainBinding

class AddIncomeActivity : AppCompatActivity() {
    // Value for binding activity_add_income.xml
    private lateinit var binding: ActivityAddIncomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.setNavigationOnClickListener {
            this.finish()
        }
    }
}