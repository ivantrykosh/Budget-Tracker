package com.ivantrykosh.app.budgettracker.client.presentation.main.add_transaction.add_income

import android.content.Context
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
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentAddIncomeBinding
import com.ivantrykosh.app.budgettracker.client.presentation.main.add_transaction.AddTransactionViewModel
import com.ivantrykosh.app.budgettracker.client.presentation.main.filter.DecimalDigitsInputFilter
import dagger.hilt.android.AndroidEntryPoint

/**
 * Add income fragment
 */
@AndroidEntryPoint
class AddIncomeFragment : Fragment() {

    private var _binding: FragmentAddIncomeBinding? = null
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
        _binding = FragmentAddIncomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAccounts()

        binding.root.setOnRefreshListener {
            getAccounts()
        }

        binding.addIncomeTopAppBar.setOnClickListener {
            findNavController().navigate(R.id.action_addIncomeFragment_to_overviewFragment)
        }

        binding.addIncomeInputValue.prefixText = Constants.CURRENCIES[AppPreferences.currency]
        binding.addIncomeInputValueEditText.filters = arrayOf(InputFilter.LengthFilter(13), DecimalDigitsInputFilter(10, 2))
        binding.addIncomeInputValueEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkValue(binding.addIncomeInputValueEditText.text.toString())) {
                    binding.addIncomeInputValue.error = resources.getString(R.string.invalid_value)
                } else {
                    binding.addIncomeInputValue.error = null
                }
                hideKeyboard(binding.addIncomeInputValue.windowToken)
            }
        }

        binding.addIncomeInputAccountText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.addIncomeInputAccountText.text.isBlank()) {
                    binding.addIncomeInputAccount.error = resources.getString(R.string.invalid_account)
                } else {
                    binding.addIncomeInputAccount.error = null
                }
            }
        }

        val categories = resources.getStringArray(R.array.income_categories)
        val adapterCategory = ArrayAdapter(requireContext(), R.layout.list_item_name_item, categories)
        (binding.addIncomeInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapterCategory)
        binding.addIncomeInputCategoryText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.addIncomeInputCategoryText.text.isBlank()) {
                    binding.addIncomeInputCategory.error = resources.getString(R.string.invalid_category)
                } else {
                    binding.addIncomeInputCategory.error = null
                }
            }
        }

        binding.addIncomeInputDateText.keyListener = null
        binding.addIncomeInputDateText.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                if (!datePicker.isAdded) {
                    datePicker.show(parentFragmentManager, "datePicker")
                }
            } else {
                if (binding.addIncomeInputDateText.text?.isBlank() != false) {
                    binding.addIncomeInputDate.error = resources.getString(R.string.invalid_date)
                } else {
                    binding.addIncomeInputDate.error = null
                }
            }
        }
        binding.addIncomeInputDateText.setOnClickListener {
            if (!datePicker.isAdded) {
                datePicker.show(parentFragmentManager, "datePicker")
            }
        }
        datePicker.addOnPositiveButtonClickListener {
            binding.addIncomeInputDateText.setText(viewModel.parseDateToString(datePicker.selection!!))
        }

        binding.addIncomeInputFromEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(binding.addIncomeInputFrom.windowToken)
            }
        }

        binding.addIncomeInputNoteEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(binding.addIncomeInputNote.windowToken)
            }
        }

        binding.addIncomeAddIncomeButton.setOnClickListener {
            addIncome()
        }

        binding.addIncomeError.errorOk.setOnClickListener {
            binding.addIncomeError.root.visibility = View.GONE
        }
    }

    /**
     * Get all user accounts
     */
    private fun getAccounts() {
        binding.addIncomeError.root.visibility = View.GONE
        progressStart()

        viewModel.getAccounts()
        viewModel.getAccountsState.observe(requireActivity()) { getAccounts ->
            if (!getAccounts.isLoading) {
                progressEnd()

                when (getAccounts.error) {
                    null -> {
                        if (getAccounts.accounts.isEmpty()) {
                            Toast.makeText(requireContext(), resources.getString(R.string.no_accounts), Toast.LENGTH_SHORT).show()
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
        val items = viewModel.getAccountsState.value?.accounts?.map { it.name } ?: emptyList()
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, items)
        (binding.addIncomeInputAccount.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.addIncomeInputAccountText.text = null
    }

    /**
     * Add income
     */
    private fun addIncome() {
        binding.addIncomeError.root.visibility = View.GONE
        clearFocusOfFields()

        if (!viewModel.checkValue(binding.addIncomeInputValueEditText.text.toString())) {
            binding.addIncomeInputValue.error = resources.getString(R.string.invalid_value)
        } else if (binding.addIncomeInputAccountText.text.isBlank()) {
            binding.addIncomeInputAccount.error = resources.getString(R.string.invalid_account)
        } else if (binding.addIncomeInputCategoryText.text.isBlank()){
            binding.addIncomeInputCategory.error = resources.getString(R.string.invalid_category)
        } else if (binding.addIncomeInputDateText.text?.isBlank() != false) {
            binding.addIncomeInputDate.error = resources.getString(R.string.invalid_date)
        } else {
            val transactionDto = viewModel.createTransactionInstance(
                binding.addIncomeInputAccountText.text.toString(),
                binding.addIncomeInputCategoryText.text.toString(),
                binding.addIncomeInputValueEditText.text.toString().toDouble(),
                binding.addIncomeInputDateText.text.toString(),
                binding.addIncomeInputFromEdit.text.toString(),
                binding.addIncomeInputNoteEdit.text.toString()
            )
            if (transactionDto == null) {
                showError(resources.getString(R.string.error), resources.getString(R.string.invalid_account))
            } else {
                progressStart()

                viewModel.createTransaction(transactionDto)
                viewModel.createTransactionState.observe(requireActivity()) { createTransaction ->
                    if (!createTransaction.isLoading) {
                        progressEnd()

                        when (createTransaction.error) {
                            null -> {
                                Toast.makeText(requireContext(), resources.getString(R.string.transaction_is_added), Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_addIncomeFragment_to_overviewFragment)
                            }
                            else -> {
                                showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occurred))
                            }
                        }

                        viewModel.createTransactionState.removeObservers(requireActivity())
                    }
                }
            }
        }
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
        binding.addIncomeInputValueEditText.clearFocus()
        binding.addIncomeInputAccount.clearFocus()
        binding.addIncomeInputCategoryText.clearFocus()
        binding.addIncomeInputDateText.clearFocus()
        binding.addIncomeInputFromEdit.clearFocus()
        binding.addIncomeInputNoteEdit.clearFocus()
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
        binding.addIncomeError.root.visibility = View.VISIBLE
        binding.addIncomeError.errorTitle.text = title
        binding.addIncomeError.errorText.text = text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}