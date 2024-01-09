package com.ivantrykosh.app.budgettracker.client.presentation.main.add_transaction.add_expense

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentAddExpenseBinding
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.add_transaction.AddTransactionViewModel
import com.ivantrykosh.app.budgettracker.client.presentation.main.filter.DecimalDigitsInputFilter
import dagger.hilt.android.AndroidEntryPoint

/**
 * Add expense fragment
 */
@AndroidEntryPoint
class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by viewModels()

    private val datePicker =
        MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAccounts()

        binding.root.setOnRefreshListener {
            getAccounts()
        }

        binding.addExpenseTopAppBar.setOnClickListener {
            findNavController().navigate(R.id.action_addExpenseFragment_to_overviewFragment)
        }

        binding.addExpenseInputValue.prefixText = "-" + Constants.CURRENCIES[AppPreferences.currency]
        binding.addExpenseInputValueEditText.filters = arrayOf(InputFilter.LengthFilter(13), DecimalDigitsInputFilter(10, 2))
        binding.addExpenseInputValueEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkValue(binding.addExpenseInputValueEditText.text.toString())) {
                    binding.addExpenseInputValue.error = resources.getString(R.string.invalid_value)
                } else {
                    binding.addExpenseInputValue.error = null
                }
                hideKeyboard(binding.addExpenseInputValue.windowToken)
            }
        }

        binding.addExpenseInputAccountText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.addExpenseInputAccountText.text.isBlank()) {
                    binding.addExpenseInputAccount.error = resources.getString(R.string.invalid_account)
                } else {
                    binding.addExpenseInputAccount.error = null
                }
            }
        }

        val categories = resources.getStringArray(R.array.expense_categories)
        val adapterCategory = ArrayAdapter(requireContext(), R.layout.list_item_name_item, categories)
        (binding.addExpenseInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapterCategory)
        binding.addExpenseInputCategoryText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.addExpenseInputCategoryText.text.isBlank()) {
                    binding.addExpenseInputCategory.error = resources.getString(R.string.invalid_category)
                } else {
                    binding.addExpenseInputCategory.error = null
                }
            }
        }

        binding.addExpenseInputDateText.keyListener = null
        binding.addExpenseInputDateText.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                if (!datePicker.isAdded) {
                    datePicker.show(parentFragmentManager, "datePicker")
                }
            } else {
                if (binding.addExpenseInputDateText.text?.isBlank() != false) {
                    binding.addExpenseInputDate.error = resources.getString(R.string.invalid_date)
                } else {
                    binding.addExpenseInputDate.error = null
                }
            }
        }
        binding.addExpenseInputDateText.setOnClickListener {
            if (!datePicker.isAdded) {
                datePicker.show(parentFragmentManager, "datePicker")
            }
        }
        datePicker.addOnPositiveButtonClickListener {
            binding.addExpenseInputDateText.setText(viewModel.parseDateToString(datePicker.selection!!))
        }

        binding.addExpenseInputToEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(binding.addExpenseInputTo.windowToken)
            }
        }

        binding.addExpenseInputNoteEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(binding.addExpenseInputNote.windowToken)
            }
        }

        binding.addExpenseAddExpenseButton.setOnClickListener {
            addExpense()
        }

        binding.addExpenseError.errorOk.setOnClickListener {
            binding.addExpenseError.root.visibility = View.GONE
        }
    }

    /**
     * Get all user accounts
     */
    private fun getAccounts() {
        binding.addExpenseError.root.visibility = View.GONE
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
                        showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
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
        val items = viewModel.getAccountsState.value?.accounts?.map { it.name } ?: emptyList()
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, items)
        (binding.addExpenseInputAccount.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.addExpenseInputAccountText.text = null
    }

    /**
     * Add income
     */
    private fun addExpense() {
        binding.addExpenseError.root.visibility = View.GONE
        clearFocusOfFields()

        if (!viewModel.checkValue(binding.addExpenseInputValueEditText.text.toString())) {
            binding.addExpenseInputValue.error = resources.getString(R.string.invalid_value)
        } else if (binding.addExpenseInputAccountText.text.isBlank()) {
            binding.addExpenseInputAccount.error = resources.getString(R.string.invalid_account)
        } else if (binding.addExpenseInputCategoryText.text.isBlank()){
            binding.addExpenseInputCategory.error = resources.getString(R.string.invalid_category)
        } else if (binding.addExpenseInputDateText.text?.isBlank() != false) {
            binding.addExpenseInputDate.error = resources.getString(R.string.invalid_date)
        } else {
            val transactionDto = viewModel.createTransactionDto(
                binding.addExpenseInputAccountText.text.toString(),
                binding.addExpenseInputCategoryText.text.toString(),
                -binding.addExpenseInputValueEditText.text.toString().toDouble(),
                binding.addExpenseInputDateText.text.toString(),
                binding.addExpenseInputToEdit.text.toString(),
                binding.addExpenseInputNoteEdit.text.toString()
            )
            if (transactionDto == null) {
                showError(resources.getString(R.string.error), resources.getString(R.string.invalid_account))
            } else {
                progressStart()

                viewModel.createTransaction(transactionDto)
                viewModel.createTransactionState.observe(requireActivity()) { createTransaction ->
                    if (!createTransaction.isLoading) {
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                        when (createTransaction.error) {
                            null -> {
                                Toast.makeText(requireContext(), resources.getString(R.string.transaction_is_added), Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_addExpenseFragment_to_overviewFragment)
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
                                showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
                            }
                        }

                        viewModel.createTransactionState.removeObservers(requireActivity())
                    }
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
     * Hide keyboard
     *
     * @param windowToken token of window
     */
    private fun hideKeyboard(windowToken: IBinder) {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    /**
     * Clear focus of Add Income fields
     */
    private fun clearFocusOfFields() {
        binding.addExpenseInputValueEditText.clearFocus()
        binding.addExpenseInputAccount.clearFocus()
        binding.addExpenseInputCategoryText.clearFocus()
        binding.addExpenseInputDateText.clearFocus()
        binding.addExpenseInputToEdit.clearFocus()
        binding.addExpenseInputNoteEdit.clearFocus()
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
        binding.addExpenseError.root.visibility = View.VISIBLE
        binding.addExpenseError.errorTitle.text = title
        binding.addExpenseError.errorText.text = text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}