package com.ivantrykosh.app.budgettracker.client.presentation.main.add_transaction.add_expense

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

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

        loadData()

        binding.root.setOnRefreshListener {
            loadData()
        }

        binding.addExpenseTopAppBar.setOnClickListener {
            findNavController().navigate(R.id.action_addExpenseFragment_to_overviewFragment)
        }

        // todo add pref instead of usd
        binding.addExpenseInputValue.prefixText = "-" + Constants.CURRENCIES[AppPreferences.currency]
        binding.addExpenseInputValueEditText.filters = arrayOf(InputFilter.LengthFilter(13), DecimalDigitsInputFilter(10, 2))
        binding.addExpenseInputValueEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkValue(binding.addExpenseInputValueEditText.text.toString())) {
                    binding.addExpenseInputValue.error = resources.getString(R.string.invalid_value)
                } else {
                    binding.addExpenseInputValue.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.addExpenseInputValue.windowToken, 0)
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

        // todo retrieve categories
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

        // todo date picker
        binding.addExpenseInputDateText.keyListener = null
        binding.addExpenseInputDateText.setOnFocusChangeListener { v, isFocus ->
            if (isFocus) {
                datePicker.show(parentFragmentManager, "datePicker")
            } else {
                if (binding.addExpenseInputDateText.text?.isBlank() != false) {
                    binding.addExpenseInputDate.error = resources.getString(R.string.invalid_date)
                } else {
                    binding.addExpenseInputDate.error = null
                }
            }
        }
        binding.addExpenseInputDateText.setOnClickListener {
            datePicker.show(parentFragmentManager, "datePicker")
        }
        datePicker.addOnPositiveButtonClickListener {
            // todo retrieve date format
            binding.addExpenseInputDateText.setText(
                SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(
                    Date(datePicker.selection!!)
                ))
        }

        binding.addExpenseInputToEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.addExpenseInputTo.windowToken, 0)
            }
        }

        binding.addExpenseInputNoteEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.addExpenseInputNote.windowToken, 0)
            }
        }

        binding.addExpenseAddExpenseButton.setOnClickListener {
            addExpense()
        }

        binding.addExpenseError.errorOk.setOnClickListener {
            binding.addExpenseError.root.visibility = View.GONE
        }
    }

    private fun loadData() {
        binding.addExpenseError.root.visibility = View.GONE
        binding.root.isRefreshing = true

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.getAccounts()
        viewModel.isLoadingGetAccounts.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.root.isRefreshing = false
                if (viewModel.getAccountsState.value.error.isBlank()) {
                    if (viewModel.getAccountsState.value.accounts.isEmpty()) {
                        binding.addExpenseError.root.visibility = View.VISIBLE
                        binding.addExpenseError.errorTitle.text = resources.getString(R.string.error)
                        binding.addExpenseError.errorText.text = resources.getString(R.string.no_accounts)
                    } else {
                        setAccounts()
                    }
                } else {
                    if (viewModel.getAccountsState.value.error.startsWith("403") || viewModel.getAccountsState.value.error.startsWith("401")) {
                        startAuthActivity()
                    } else if (viewModel.getAccountsState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.addExpenseError.root.visibility = View.VISIBLE
                        binding.addExpenseError.errorTitle.text = resources.getString(R.string.error)
                        binding.addExpenseError.errorText.text = viewModel.getAccountsState.value.error
                    } else {
                        binding.addExpenseError.root.visibility = View.VISIBLE
                        binding.addExpenseError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.addExpenseError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                }
                viewModel.isLoadingGetAccounts.removeObservers(requireActivity())
            }
        }
    }

    private fun setAccounts() {
        // todo retrieve accounts
        val items = viewModel.getAccountsState.value.accounts.map { it.name }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, items)
        (binding.addExpenseInputAccount.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.addExpenseInputAccountText.text = null
    }

    private fun addExpense() {
        binding.addExpenseError.root.visibility = View.GONE
        binding.addExpenseInputValueEditText.clearFocus()
        binding.addExpenseInputAccount.clearFocus()
        binding.addExpenseInputCategoryText.clearFocus()
        binding.addExpenseInputDateText.clearFocus()
        binding.addExpenseInputToEdit.clearFocus()
        binding.addExpenseInputNoteEdit.clearFocus()

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
                // todo retrieve date format
                viewModel.parseToCorrectDate(binding.addExpenseInputDateText.text.toString()),
                binding.addExpenseInputToEdit.text.toString(),
                binding.addExpenseInputNoteEdit.text.toString()
            )
            if (transactionDto == null) {
                binding.addExpenseError.root.visibility = View.VISIBLE
                binding.addExpenseError.errorTitle.text = resources.getString(R.string.error)
                binding.addExpenseError.errorText.text = resources.getString(R.string.invalid_account)
            } else {
                binding.root.isRefreshing = true

                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                viewModel.createTransaction(transactionDto)
                viewModel.isLoadingCreateTransaction.observe(requireActivity()) { isLoading ->
                    if (!isLoading) {
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                        if (viewModel.createTransactionState.value.error.isBlank()) {
                            Toast.makeText(requireContext(), resources.getString(R.string.transaction_is_added), Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_addExpenseFragment_to_overviewFragment)
                        } else if (viewModel.createTransactionState.value.error.contains("Email is not verified", ignoreCase = true) || viewModel.createTransactionState.value.error.startsWith("401")) {
                            startAuthActivity()
                        } else if (viewModel.createTransactionState.value.error.contains("HTTP", ignoreCase = true)) {
                            binding.addExpenseError.root.visibility = View.VISIBLE
                            binding.addExpenseError.errorTitle.text = resources.getString(R.string.error)
                            binding.addExpenseError.errorText.text = viewModel.createTransactionState.value.error
                        } else {
                            binding.addExpenseError.root.visibility = View.VISIBLE
                            binding.addExpenseError.errorTitle.text = resources.getString(R.string.network_error)
                            binding.addExpenseError.errorText.text = resources.getString(R.string.connection_failed_message)
                        }

                        binding.root.isRefreshing = false
                        viewModel.isLoadingCreateTransaction.removeObservers(requireActivity())
                    }
                }
            }
        }
    }

    private fun startAuthActivity() {
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        requireActivity().startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}