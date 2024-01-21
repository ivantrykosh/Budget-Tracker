package com.ivantrykosh.app.budgettracker.client.presentation.main.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.presentation.main.adapter.TransactionItemAdapter
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentOverviewBinding
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Overview fragment
 */
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

        refreshBalance()

        binding.root.setOnRefreshListener {
            refreshBalance()
        }

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

        binding.overviewMainFabAddIncome.setOnClickListener {
            findNavController().navigate(R.id.action_overviewFragment_to_addIncomeFragment)

        }

        binding.overviewMainFabAddExpense.setOnClickListener {
            findNavController().navigate(R.id.action_overviewFragment_to_addExpenseFragment)

        }

        binding.overviewError.errorOk.setOnClickListener {
            binding.overviewError.root.visibility = View.GONE
        }

        binding.overviewNestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                // Scrolling down, hide the view
                binding.overviewLayoutFabs.visibility = View.GONE
            } else {
                // Scrolling up, show the view
                binding.overviewLayoutFabs.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()

        refreshBalance()

        binding.overviewMainFloatingActionButton.isExpanded = false
    }

    /**
     * Refresh balance
     */
    private fun refreshBalance() {
        val stringDate = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())
        binding.overviewTextViewBalanceFor.text = getString(R.string.balance_for, stringDate)

        binding.overviewMainIncomesValue.text = viewModel.getFormat().format(0.0)
        binding.overviewMainExpensesValue.text = viewModel.getFormat().format(0.0)
        binding.mainTotalValue.text = viewModel.getFormat().format(0.0)

        setIncomesVisible(false)
        setExpensesVisible(false)
        binding.overviewError.root.visibility = View.GONE
        progressStart()

        viewModel.getAccounts()
        viewModel.getAccountsState.observe(requireActivity()) { getAccounts ->
            if (!getAccounts.isLoading) {
                progressEnd()

                when (getAccounts.error) {
                    null -> {
                        if (getAccounts.accounts.isNotEmpty()) {
                            loadTransactions()
                        }
                    }
                    else -> {
                        showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occurred))
                    }
                }

                viewModel.getAccountsState.removeObservers(requireActivity())
            }
        }
    }

    /**
     * Set visibility of incomes
     *
     * @param visible are incomes visible
     */
    private fun setIncomesVisible(visible: Boolean) {
        if (visible) {
            binding.overviewLastIncomesRecyclerView.visibility = View.VISIBLE
            binding.overviewTextViewNoLastIncomes.visibility = View.GONE
        } else {
            binding.overviewLastIncomesRecyclerView.visibility = View.GONE
            binding.overviewTextViewNoLastIncomes.visibility = View.VISIBLE
        }
    }

    /**
     * Set visibility of expenses
     *
     * @param visible are expenses visible
     */
    private fun setExpensesVisible(visible: Boolean) {
        if (visible) {
            binding.overviewLastExpensesRecyclerView.visibility = View.VISIBLE
            binding.overviewTextViewNoLastExpenses.visibility = View.GONE
        } else {
            binding.overviewLastExpensesRecyclerView.visibility = View.GONE
            binding.overviewTextViewNoLastExpenses.visibility = View.VISIBLE
        }
    }

    /**
     * Load transactions
     */
    private fun loadTransactions() {
        binding.overviewError.root.visibility = View.GONE
        progressStart()

        viewModel.getTransactions(viewModel.getAccountsState.value!!.accounts.map { it.accountId }, viewModel.getStartMonth(), viewModel.getEndMonth())
        viewModel.getTransactionsState.observe(requireActivity()) { getTransactions ->
            if (!getTransactions.isLoading) {
                progressEnd()

                when (getTransactions.error) {
                    null -> {
                        binding.overviewMainIncomesValue.text = viewModel.getFormat().format(viewModel.getSumOfIncomes())
                        binding.overviewMainExpensesValue.text = viewModel.getFormat().format(viewModel.getSumOfExpenses())
                        binding.mainTotalValue.text = viewModel.getFormat().format(viewModel.getTotalSum())

                        if (viewModel.getIncomes().isNotEmpty()) {
                            val incomes = viewModel.getIncomes()
                            binding.overviewLastIncomesRecyclerView.adapter = TransactionItemAdapter(requireContext(), incomes, incomes.size, 3)
                            binding.overviewLastIncomesRecyclerView.setHasFixedSize(true)
                            setIncomesVisible(true)
                        }
                        if (viewModel.getExpenses().isNotEmpty()) {
                            val expenses = viewModel.getExpenses()
                            binding.overviewLastExpensesRecyclerView.adapter = TransactionItemAdapter(requireContext(), expenses, expenses.size, 3)
                            binding.overviewLastExpensesRecyclerView.setHasFixedSize(true)
                            setExpensesVisible(true)
                        }
                    }
                    else -> {
                        showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occurred))
                    }
                }

                viewModel.getTransactionsState.removeObservers(requireActivity())
            }
        }
    }

    /**
     * Show progress indicator and make screen not touchable
     */
    private fun progressStart() {
        binding.root.isRefreshing = true
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    /**
     * Hide progress indicator and make screen touchable
     */
    private fun progressEnd() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.root.isRefreshing = false
    }

    /**
     * Show error message
     *
     * @param title title of message
     * @param text text of message
     */
    private fun showError(title: String, text: String) {
        binding.overviewError.root.visibility = View.VISIBLE
        binding.overviewError.errorTitle.text = title
        binding.overviewError.errorText.text = text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}