package com.ivantrykosh.app.budgettracker.client.presentation.main.report.category_report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentCategoryReportBinding
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

/**
 * Category report fragment
 */
@AndroidEntryPoint
class CategoryReportFragment : Fragment() {
    private var _binding: FragmentCategoryReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoryReportViewModel by activityViewModels()

    private val datePicker =
        MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select date")
            .setSelection(
                Pair(
                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    MaterialDatePicker.todayInUtcMilliseconds()
                )
            )
            .build()

    private var isAllAccounts = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCategoryReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadAccounts()

        binding.root.setOnRefreshListener {
            loadAccounts()
        }

        binding.categoryReportTopAppBar.setNavigationOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        viewModel.dateRange.observe(requireActivity()) {
            binding.categoryReportInputDatesText.setText(it)
        }

        val types = resources.getStringArray(R.array.transaction_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, types)
        (binding.categoryReportInputTransactionType.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        binding.categoryReportInputTransactionTypeText.text = null
        binding.categoryReportInputTransactionTypeText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.categoryReportInputTransactionTypeText.text.isBlank()) {
                    binding.categoryReportInputTransactionType.error = resources.getString(R.string.invalid_transaction_type)
                } else {
                    binding.categoryReportInputTransactionType.error = null
                }
            }
        }

        binding.categoryReportInputAccountText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.categoryReportInputAccountText.text.isBlank()) {
                    binding.categoryReportInputAccount.error = resources.getString(R.string.invalid_account)
                } else {
                    binding.categoryReportInputAccount.error = null
                }
            }
        }
        binding.categoryReportInputAccountText.setOnItemClickListener { _, _, position, _ ->
            isAllAccounts = position == 0
        }

        binding.categoryReportInputDatesText.keyListener = null
        binding.categoryReportInputDatesText.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                if (!datePicker.isAdded) {
                    datePicker.show(parentFragmentManager, "datePicker")
                }
            } else {
                if (binding.categoryReportInputDatesText.text?.isBlank() != false) {
                    binding.categoryReportInputDates.error = resources.getString(R.string.invalid_dates)
                } else {
                    binding.categoryReportInputDates.error = null
                }
            }
        }
        binding.categoryReportInputDatesText.setOnClickListener {
            if (!datePicker.isAdded) {
                datePicker.show(parentFragmentManager, "datePicker")
            }
        }
        datePicker.addOnPositiveButtonClickListener {
            viewModel.updateDateRange(Date(it.first), Date(it.second))
        }

        binding.categoryReportButtonShowReport.setOnClickListener {
            createReport()
        }

        binding.categoryReportError.errorOk.setOnClickListener {
            binding.categoryReportError.root.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        val types = resources.getStringArray(R.array.transaction_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, types)
        (binding.categoryReportInputTransactionType.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        loadAccounts()
    }

    /**
     * Load user accounts
     */
    private fun loadAccounts() {
        binding.categoryReportError.root.visibility = View.GONE
        progressStart()

        viewModel.getAccounts()
        viewModel.getAccountsState.observe(requireActivity()) { getAccounts ->
            if (!getAccounts.isLoading) {
                progressEnd()

                when (getAccounts.error) {
                    null -> {
                        if (getAccounts.accounts.isEmpty()) {
                            showError(resources.getString(R.string.error), resources.getString(R.string.no_accounts))
                        } else {
                            setAccounts()
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
     * Set accounts to AutoCompleteTextView
     */
    private fun setAccounts() {
        val items: MutableList<String> = viewModel.getAccountsState.value?.accounts?.map { it.name }?.toMutableList() ?: mutableListOf()
        items.add(0, resources.getString(R.string.select_all))
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, items)
        (binding.categoryReportInputAccount.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.categoryReportInputAccountText.text = null
    }

    /**
     * Create category report
     */
    private fun createReport() {
        binding.categoryReportError.root.visibility = View.GONE
        binding.categoryReportInputTransactionTypeText.clearFocus()
        binding.categoryReportInputAccountText.clearFocus()
        binding.categoryReportInputDatesText.clearFocus()

        if (binding.categoryReportInputTransactionTypeText.text.isBlank()) {
            binding.categoryReportInputTransactionType.error = resources.getString(R.string.invalid_transaction_type)
        } else if (binding.categoryReportInputAccountText.text.isBlank()) {
            binding.categoryReportInputAccount.error = resources.getString(R.string.invalid_account)
        } else if (binding.categoryReportInputDatesText.text?.isBlank() == true) {
            binding.categoryReportInputDates.error = resources.getString(R.string.invalid_dates)
        } else {
            progressStart()

            val accountIds: List<Long> = when (isAllAccounts) {
                true -> viewModel.getAccountsState.value!!.accounts.map { it.accountId }
                false -> listOf(viewModel.getAccountIdByName(binding.categoryReportInputAccountText.text.toString()) ?: -1)
            }
            val type: Int = when (binding.categoryReportInputTransactionTypeText.text.toString()) {
                resources.getString(R.string.incomes) -> 1
                resources.getString(R.string.expenses) -> -1
                else -> 0
            }
            viewModel.getTransactions(accountIds, type)
            viewModel.getTransactionsState.observe(requireActivity()) { getTransactions  ->
                if (!getTransactions.isLoading) {
                    progressEnd()

                    when (getTransactions.error) {
                        null -> {
                            if (getTransactions.transactions.isEmpty()) {
                                Toast.makeText(requireContext(), resources.getString(R.string.no_transactions), Toast.LENGTH_SHORT).show()
                            } else {
                                findNavController().navigate(R.id.action_categoryReportFragment_to_createdCategoryReport)
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
        binding.categoryReportError.root.visibility = View.VISIBLE
        binding.categoryReportError.errorTitle.text = title
        binding.categoryReportError.errorText.text = text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.dateRange.removeObservers(requireActivity())
        _binding = null
    }
}