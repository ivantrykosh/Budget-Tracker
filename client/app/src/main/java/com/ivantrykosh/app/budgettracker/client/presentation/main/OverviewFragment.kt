package com.ivantrykosh.app.budgettracker.client.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.adapter.OperationItemAdapter
import com.ivantrykosh.app.budgettracker.client.data.Datasource
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentOverviewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class OverviewFragment : Fragment() {

    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OverviewViewModel by viewModels()

    private val rotateClockWise: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_clock_wise)
    }

    private val rotateAntiClockWise: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_anti_clock_wise)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.overviewTopAppBar.setNavigationOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        binding.overviewMainFloatingActionButton.setOnClickListener {
            binding.overviewMainFloatingActionButton.isExpanded = !binding.overviewMainFloatingActionButton.isExpanded
            if (binding.overviewMainFloatingActionButton.isExpanded) {
                binding.overviewMainFloatingActionButton.startAnimation(rotateClockWise)
            } else {
                binding.overviewMainFloatingActionButton.startAnimation(rotateAntiClockWise)
            }
        }

        val stringDate = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())
        binding.overviewTextViewBalanceFor.text = getString(R.string.balance_for, stringDate)

        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        format.currency = Currency.getInstance("USD")

        // Set text for incomes, expenses and total values
        val myDatasetBalanceOverview = Datasource().loadBalanceOverview()
        binding.overviewMainIncomesValue.text = format.format(myDatasetBalanceOverview.incomes)
        binding.overviewMainExpensesValue.text = format.format(myDatasetBalanceOverview.expenses)
        binding.mainTotalValue.text = format.format(myDatasetBalanceOverview.total)

        // Load data about incomes to recycler view
        val myDatasetIncomes = Datasource().loadOperations()
        binding.overviewLastIncomesRecyclerView.adapter = OperationItemAdapter(requireContext(), myDatasetIncomes, 2)
        binding.overviewLastIncomesRecyclerView.setHasFixedSize(true)

        // Load data about expenses to recycler view
        val myDatasetExpenses = Datasource().loadOperations()
        binding.overviewLastExpensesRecyclerView.adapter = OperationItemAdapter(requireContext(), myDatasetExpenses, 2)
        binding.overviewLastExpensesRecyclerView.setHasFixedSize(true)

        // Set on click listener for "Add income" FAB
        binding.overviewMainFabAddIncome.setOnClickListener {
            // todo add navigate

        }

        // Set on click listener for "Add expense" FAB
        binding.overviewMainFabAddExpense.setOnClickListener {
            // todo add navigate

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}