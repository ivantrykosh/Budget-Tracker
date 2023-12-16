package com.ivantrykosh.app.budgettracker.client.presentation.main.overview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.presentation.main.adapter.TransactionItemAdapter
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentOverviewBinding
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
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

    private val format by lazy {
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        format.currency = Currency.getInstance(
            AppPreferences.currency ?: "USD"
        )
        format
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
            // todo add navigate

        }

        binding.overviewError.errorOk.setOnClickListener {
            binding.overviewError.root.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        refreshBalance()

        binding.overviewMainFloatingActionButton.isExpanded = false
    }

    private fun refreshBalance() {
        val stringDate = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())
        binding.overviewTextViewBalanceFor.text = getString(R.string.balance_for, stringDate)

        binding.overviewMainIncomesValue.text = format.format(0.0)
        binding.overviewMainExpensesValue.text = format.format(0.0)
        binding.mainTotalValue.text = format.format(0.0)

        setIncomesVisible(true)
        setExpensesVisible(true)
        binding.overviewError.root.visibility = View.GONE
        binding.overviewCircularProgressIndicator.visibility = View.VISIBLE

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.getAccounts()
        viewModel.isLoadingGetAccounts.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                if (viewModel.getAccountsState.value.error.isBlank()) {
                    if (viewModel.getAccountsState.value.accounts.isNotEmpty()) {
                        loadTransactions()
                    } else {
                        setIncomesVisible(false)
                        setExpensesVisible(false)

                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        binding.root.isRefreshing = false
                    }
                } else {
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    setIncomesVisible(false)
                    setExpensesVisible(false)
                    binding.root.isRefreshing = false
                    if (viewModel.getAccountsState.value.error.startsWith("403") || viewModel.getAccountsState.value.error.startsWith("401")) {
                        startAuthActivity()
                    } else if (viewModel.getAccountsState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.overviewError.root.visibility = View.VISIBLE
                        binding.overviewError.errorTitle.text = resources.getString(R.string.error)
                        binding.overviewError.errorText.text = viewModel.getAccountsState.value.error
                    } else {
                        binding.overviewError.root.visibility = View.VISIBLE
                        binding.overviewError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.overviewError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                }
                binding.overviewCircularProgressIndicator.visibility = View.GONE
                viewModel.isLoadingGetAccounts.removeObservers(requireActivity())
            }
        }
    }

    private fun setIncomesVisible(visible: Boolean) {
        if (visible) {
            binding.overviewLastIncomesRecyclerView.visibility = View.VISIBLE
            binding.overviewTextViewNoLastIncomes.visibility = View.GONE
        } else {
            binding.overviewLastIncomesRecyclerView.visibility = View.GONE
            binding.overviewTextViewNoLastIncomes.visibility = View.VISIBLE
        }
    }

    private fun setExpensesVisible(visible: Boolean) {
        if (visible) {
            binding.overviewLastExpensesRecyclerView.visibility = View.VISIBLE
            binding.overviewTextViewNoLastExpenses.visibility = View.GONE
        } else {
            binding.overviewLastExpensesRecyclerView.visibility = View.GONE
            binding.overviewTextViewNoLastExpenses.visibility = View.VISIBLE
        }
    }

    private fun startAuthActivity() {
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        requireActivity().startActivity(intent)
    }

    private fun loadTransactions() {
        binding.overviewError.root.visibility = View.GONE

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.getTransactions(viewModel.getAccountsState.value.accounts.map { it.accountId }, viewModel.getStartMonth(), viewModel.getEndMonth())
        viewModel.isLoadingGetTransactions.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                if (viewModel.getTransactionsState.value.error.isBlank()) {

                    binding.overviewMainIncomesValue.text = format.format(viewModel.getSumOfIncomes())
                    binding.overviewMainExpensesValue.text = format.format(viewModel.getSumOfExpenses())
                    binding.mainTotalValue.text = format.format(viewModel.getTotalSum())

                    if (viewModel.getIncomes().isNotEmpty()) {
                        val incomes = viewModel.getIncomes()
                        binding.overviewLastIncomesRecyclerView.adapter = TransactionItemAdapter(requireContext(), incomes, incomes.size)
                        binding.overviewLastIncomesRecyclerView.setHasFixedSize(true)
                    } else {
                        setIncomesVisible(false)
                    }
                    if (viewModel.getExpenses().isNotEmpty()) {
                        val expenses = viewModel.getExpenses()
                        binding.overviewLastExpensesRecyclerView.adapter = TransactionItemAdapter(requireContext(), expenses, expenses.size)
                        binding.overviewLastExpensesRecyclerView.setHasFixedSize(true)
                    } else {
                        setExpensesVisible(false)
                    }
                } else {
                    setIncomesVisible(false)
                    setExpensesVisible(false)
                    binding.root.isRefreshing = false
                    if (viewModel.getAccountsState.value.error.contains("Email is not verified", ignoreCase = true) || viewModel.getAccountsState.value.error.startsWith("401")) {
                        startAuthActivity()
                    } else if (viewModel.getAccountsState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.overviewError.root.visibility = View.VISIBLE
                        binding.overviewError.errorTitle.text = resources.getString(R.string.error)
                        binding.overviewError.errorText.text = viewModel.getAccountsState.value.error
                    } else {
                        binding.overviewError.root.visibility = View.VISIBLE
                        binding.overviewError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.overviewError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                }
                binding.root.isRefreshing = false
                viewModel.isLoadingGetTransactions.removeObservers(requireActivity())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}