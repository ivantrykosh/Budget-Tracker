package com.ivantrykosh.app.budgettracker.client.presentation.main.add_transaction.add_income

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
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentAddIncomeBinding
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

        loadData()

        binding.root.setOnRefreshListener {
            loadData()
        }

        binding.addIncomeTopAppBar.setOnClickListener {
            findNavController().navigate(R.id.action_addIncomeFragment_to_overviewFragment)
        }

        // todo add pref instead of usd
        binding.addIncomeInputValue.prefixText = Currency.getInstance(AppPreferences.currency).symbol
        binding.addIncomeInputValueEditText.filters = arrayOf(InputFilter.LengthFilter(13), DecimalDigitsInputFilter(10, 2))
        binding.addIncomeInputValueEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkValue(binding.addIncomeInputValueEditText.text.toString())) {
                    binding.addIncomeInputValue.error = resources.getString(R.string.invalid_value)
                } else {
                    binding.addIncomeInputValue.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.addIncomeInputValue.windowToken, 0)
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

        // todo retrieve categories
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

        // todo date picker
        binding.addIncomeInputDateText.keyListener = null
        binding.addIncomeInputDateText.setOnFocusChangeListener { v, isFocus ->
            if (isFocus) {
                datePicker.show(parentFragmentManager, "datePicker")
            } else {
                if (binding.addIncomeInputDateText.text?.isBlank() != false) {
                    binding.addIncomeInputDate.error = resources.getString(R.string.invalid_date)
                } else {
                    binding.addIncomeInputDate.error = null
                }
            }
        }
        binding.addIncomeInputDateText.setOnClickListener {
            datePicker.show(parentFragmentManager, "datePicker")
        }
        datePicker.addOnPositiveButtonClickListener {
            // todo retrieve date format
            binding.addIncomeInputDateText.setText(SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(Date(datePicker.selection!!)))
        }

        binding.addIncomeInputFromEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.addIncomeInputFrom.windowToken, 0)
            }
        }

        binding.addIncomeInputNoteEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.addIncomeInputNote.windowToken, 0)
            }
        }

        binding.addIncomeAddIncomeButton.setOnClickListener {
            addIncome()
        }

        binding.addIncomeError.errorOk.setOnClickListener {
            binding.addIncomeError.root.visibility = View.GONE
        }
    }

    private fun loadData() {
        binding.addIncomeError.root.visibility = View.GONE
        binding.root.isRefreshing = true

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.getAccounts()
        viewModel.isLoadingGetAccounts.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.root.isRefreshing = false
                if (viewModel.getAccountsState.value.error.isBlank()) {
                    if (viewModel.getAccountsState.value.accounts.isEmpty()) {
                        binding.addIncomeError.root.visibility = View.VISIBLE
                        binding.addIncomeError.errorTitle.text = resources.getString(R.string.error)
                        binding.addIncomeError.errorText.text = resources.getString(R.string.no_accounts)
                    } else {
                        setAccounts()
                    }
                } else {
                    if (viewModel.getAccountsState.value.error.startsWith("403") || viewModel.getAccountsState.value.error.startsWith("401")) {
                        startAuthActivity()
                    } else if (viewModel.getAccountsState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.addIncomeError.root.visibility = View.VISIBLE
                        binding.addIncomeError.errorTitle.text = resources.getString(R.string.error)
                        binding.addIncomeError.errorText.text = viewModel.getAccountsState.value.error
                    } else {
                        binding.addIncomeError.root.visibility = View.VISIBLE
                        binding.addIncomeError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.addIncomeError.errorText.text = resources.getString(R.string.connection_failed_message)
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
        (binding.addIncomeInputAccount.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.addIncomeInputAccountText.text = null
    }

    private fun addIncome() {
        binding.addIncomeError.root.visibility = View.GONE
        binding.addIncomeInputValueEditText.clearFocus()
        binding.addIncomeInputAccount.clearFocus()
        binding.addIncomeInputCategoryText.clearFocus()
        binding.addIncomeInputDateText.clearFocus()
        binding.addIncomeInputFromEdit.clearFocus()
        binding.addIncomeInputNoteEdit.clearFocus()

        if (!viewModel.checkValue(binding.addIncomeInputValueEditText.text.toString())) {
            binding.addIncomeInputValue.error = resources.getString(R.string.invalid_value)
        } else if (binding.addIncomeInputAccountText.text.isBlank()) {
            binding.addIncomeInputAccount.error = resources.getString(R.string.invalid_account)
        } else if (binding.addIncomeInputCategoryText.text.isBlank()){
            binding.addIncomeInputCategory.error = resources.getString(R.string.invalid_category)
        } else if (binding.addIncomeInputDateText.text?.isBlank() != false) {
            binding.addIncomeInputDate.error = resources.getString(R.string.invalid_date)
        } else {
            val transactionDto = viewModel.createTransactionDto(
                binding.addIncomeInputAccountText.text.toString(),
                binding.addIncomeInputCategoryText.text.toString(),
                binding.addIncomeInputValueEditText.text.toString().toDouble(),
                // todo retrieve date format
                viewModel.parseToCorrectDate(binding.addIncomeInputDateText.text.toString()),
                binding.addIncomeInputFromEdit.text.toString(),
                binding.addIncomeInputNoteEdit.text.toString()
            )
            if (transactionDto == null) {
                binding.addIncomeError.root.visibility = View.VISIBLE
                binding.addIncomeError.errorTitle.text = resources.getString(R.string.error)
                binding.addIncomeError.errorText.text = resources.getString(R.string.invalid_account)
            } else {
                binding.root.isRefreshing = true

                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                viewModel.createTransaction(transactionDto)
                viewModel.isLoadingCreateTransaction.observe(requireActivity()) { isLoading ->
                    if (!isLoading) {
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                        if (viewModel.createTransactionState.value.error.isBlank()) {
                            Toast.makeText(requireContext(), resources.getString(R.string.transaction_is_added), Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_addIncomeFragment_to_overviewFragment)
                        } else if (viewModel.createTransactionState.value.error.contains("Email is not verified", ignoreCase = true) || viewModel.getAccountsState.value.error.startsWith("401")) {
                            startAuthActivity()
                        } else if (viewModel.createTransactionState.value.error.contains("HTTP", ignoreCase = true)) {
                            binding.addIncomeError.root.visibility = View.VISIBLE
                            binding.addIncomeError.errorTitle.text = resources.getString(R.string.error)
                            binding.addIncomeError.errorText.text = viewModel.createTransactionState.value.error
                        } else {
                            binding.addIncomeError.root.visibility = View.VISIBLE
                            binding.addIncomeError.errorTitle.text = resources.getString(R.string.network_error)
                            binding.addIncomeError.errorText.text = resources.getString(R.string.connection_failed_message)
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