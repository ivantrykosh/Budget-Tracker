package com.ivantrykosh.app.budgettracker.client.presentation.main.report.time_report

import android.content.Intent
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
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentTimeReportBinding
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

/**
 * Time report fragment
 */
@AndroidEntryPoint
class TimeReportFragment : Fragment() {
    private var _binding: FragmentTimeReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TimeReportViewModel by activityViewModels()

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
        _binding = FragmentTimeReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadAccounts()

        binding.root.setOnRefreshListener {
            loadAccounts()
        }

        binding.timeReportTopAppBar.setNavigationOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        viewModel.dateRange.observe(requireActivity()) {
            binding.timeReportInputDatesText.setText(it)
        }

        val types = resources.getStringArray(R.array.transaction_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, types)
        (binding.timeReportInputTransactionType.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        binding.timeReportInputTransactionTypeText.text = null
        binding.timeReportInputTransactionTypeText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.timeReportInputTransactionTypeText.text.isBlank()) {
                    binding.timeReportInputTransactionType.error = resources.getString(R.string.invalid_transaction_type)
                } else {
                    binding.timeReportInputTransactionType.error = null
                }
            }
        }

        binding.timeReportInputAccountText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.timeReportInputAccountText.text.isBlank()) {
                    binding.timeReportInputAccount.error = resources.getString(R.string.invalid_account)
                } else {
                    binding.timeReportInputAccount.error = null
                }
            }
        }
        binding.timeReportInputAccountText.setOnItemClickListener { _, _, position, _ ->
            isAllAccounts = position == 0
        }

        val periods = resources.getStringArray(R.array.period_types)
        val adapterPeriods = ArrayAdapter(requireContext(), R.layout.list_item_name_item, periods)
        (binding.timeReportInputPeriod.editText as? AutoCompleteTextView)?.setAdapter(adapterPeriods)
        binding.timeReportInputPeriodText.setText(periods[0], false)
        viewModel.setPeriod(TimeReportViewModel.Period.DAY)
        binding.timeReportInputPeriodText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.timeReportInputPeriodText.text.isBlank()) {
                    binding.timeReportInputPeriod.error = resources.getString(R.string.invalid_period)
                } else {
                    binding.timeReportInputPeriod.error = null
                }
            }
        }
        binding.timeReportInputPeriodText.setOnItemClickListener { parent, _, position, _ ->
            viewModel.setPeriod(
                when (parent.getItemAtPosition(position).toString()) {
                    "Days" -> TimeReportViewModel.Period.DAY
                    "Weeks" -> TimeReportViewModel.Period.WEEK
                    "Months" -> TimeReportViewModel.Period.MONTH
                    else -> TimeReportViewModel.Period.YEAR
                }
            )
        }

        binding.timeReportInputDatesText.keyListener = null
        binding.timeReportInputDatesText.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                if (!datePicker.isAdded) {
                    datePicker.show(parentFragmentManager, "datePicker")
                }
            } else {
                if (binding.timeReportInputDatesText.text?.isBlank() != false) {
                    binding.timeReportInputDates.error = resources.getString(R.string.invalid_dates)
                } else {
                    binding.timeReportInputDates.error = null
                }
            }
        }
        binding.timeReportInputDatesText.setOnClickListener {
            if (!datePicker.isAdded) {
                datePicker.show(parentFragmentManager, "datePicker")
            }
        }
        datePicker.addOnPositiveButtonClickListener {
            viewModel.updateDateRange(Date(it.first), Date(it.second))
        }

        binding.timeReportButtonShowReport.setOnClickListener {
            createReport()
        }

        binding.timeReportError.errorOk.setOnClickListener {
            binding.timeReportError.root.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        val types = resources.getStringArray(R.array.transaction_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, types)
        (binding.timeReportInputTransactionType.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        val periods = resources.getStringArray(R.array.period_types)
        val adapterPeriods = ArrayAdapter(requireContext(), R.layout.list_item_name_item, periods)
        (binding.timeReportInputPeriod.editText as? AutoCompleteTextView)?.setAdapter(adapterPeriods)
        binding.timeReportInputPeriodText.setText(periods[0], false)
        viewModel.setPeriod(TimeReportViewModel.Period.DAY)

        loadAccounts()
    }

    /**
     * Load user accounts
     */
    private fun loadAccounts() {
        binding.timeReportError.root.visibility = View.GONE
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
                    Constants.ErrorStatusCodes.UNAUTHORIZED,
                    Constants.ErrorStatusCodes.FORBIDDEN,
                    Constants.ErrorStatusCodes.TOKEN_NOT_FOUND -> {
                        startAuthActivity()
                    }
                    Constants.ErrorStatusCodes.NETWORK_ERROR -> {
                        showError(resources.getString(R.string.network_error), resources.getString(R.string.connection_failed_message))
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
        (binding.timeReportInputAccount.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.timeReportInputAccountText.text = null
    }

    /**
     * Create time report
     */
    private fun createReport() {
        binding.timeReportError.root.visibility = View.GONE
        binding.timeReportInputTransactionTypeText.clearFocus()
        binding.timeReportInputAccountText.clearFocus()
        binding.timeReportInputPeriodText.clearFocus()
        binding.timeReportInputDatesText.clearFocus()

        if (binding.timeReportInputTransactionTypeText.text.isBlank()) {
            binding.timeReportInputTransactionType.error = resources.getString(R.string.invalid_transaction_type)
        } else if (binding.timeReportInputAccountText.text.isBlank()) {
            binding.timeReportInputAccount.error = resources.getString(R.string.invalid_account)
        } else if (binding.timeReportInputPeriodText.text.isBlank()) {
            binding.timeReportInputPeriod.error = resources.getString(R.string.invalid_period)
        } else if (binding.timeReportInputDatesText.text?.isBlank() == true) {
            binding.timeReportInputDates.error = resources.getString(R.string.invalid_dates)
        } else {
            progressStart()

            val accountIds: List<Long> = when (isAllAccounts) {
                true -> viewModel.getAccountsState.value!!.accounts.map { it.accountId }
                false -> listOf(viewModel.getAccountIdByName(binding.timeReportInputAccountText.text.toString()) ?: -1)
            }
            val type: Int = when (binding.timeReportInputTransactionTypeText.text.toString()) {
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
                                findNavController().navigate(R.id.action_timeReportFragment_to_createdTimeReportFragment)
                            }
                        }
                        Constants.ErrorStatusCodes.UNAUTHORIZED,
                        Constants.ErrorStatusCodes.FORBIDDEN,
                        Constants.ErrorStatusCodes.TOKEN_NOT_FOUND -> {
                            startAuthActivity()
                        }
                        Constants.ErrorStatusCodes.NETWORK_ERROR -> {
                            showError(resources.getString(R.string.network_error), resources.getString(R.string.connection_failed_message))
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
     * Start auth activity
     */
    private fun startAuthActivity() {
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        requireActivity().startActivity(intent)
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
        binding.timeReportError.root.visibility = View.VISIBLE
        binding.timeReportError.errorTitle.text = title
        binding.timeReportError.errorText.text = text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.dateRange.removeObservers(requireActivity())
        _binding = null
    }
}