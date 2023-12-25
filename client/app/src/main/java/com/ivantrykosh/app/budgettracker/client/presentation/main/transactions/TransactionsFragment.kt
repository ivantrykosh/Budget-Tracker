package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentTransactionsBinding
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.adapter.AccountItemAdapter
import com.ivantrykosh.app.budgettracker.client.presentation.main.adapter.TransactionItemAdapter
import com.ivantrykosh.app.budgettracker.client.presentation.main.filter.DecimalDigitsInputFilter
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@AndroidEntryPoint
class TransactionsFragment : Fragment(), OnTransactionClickListener {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionsViewModel by viewModels()

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
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.isEnabled = false
        binding.transactionsDialog.root.visibility = View.GONE

        binding.transactionsNoTransactionsText.visibility = View.VISIBLE
        binding.transactionsRecyclerView.visibility = View.GONE

        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsRecyclerView.adapter = AccountItemAdapter(requireContext(), emptyList())

        viewModel.updateCurrentDate()

        viewModel.currentDate.observe(requireActivity()) {
            binding.transactionsMonthYearText.text = viewModel.currentDate.value
        }

        binding.transactionsImageButtonBack.setOnClickListener {
            viewModel.minusMonth()
            refresh()
        }

        binding.transactionsImageButtonForward.setOnClickListener {
            viewModel.plusMonth()
            refresh()
        }

        refresh()

        binding.transactionsTopAppBar.setNavigationOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        binding.transactionsTopAppBar.setOnMenuItemClickListener { menuItem ->
            binding.transactionsDialog.root.visibility = View.GONE
            when (menuItem.itemId) {
                R.id.transactions_refresh -> {
                    refresh()
                    true
                }
                else -> false
            }
        }

        binding.transactionsDialog.transactionDetailsTextCancel.setOnClickListener {
            binding.transactionsDialog.root.visibility = View.GONE
            refresh()
        }

        binding.transactionsDialog.transactionDetailsDeleteTransaction.setOnClickListener {
            binding.transactionsDialog.root.visibility = View.GONE
            onDeleteTransaction()
        }

        binding.transactionsDialog.transactionDetailsTextOk.setOnClickListener {
            binding.transactionsDialog.root.visibility = View.GONE
            updateTransaction()
        }

        binding.transactionsDialog.transactionDetailsInputValueEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkValue(binding.transactionsDialog.transactionDetailsInputValueEditText.text.toString())) {
                    binding.transactionsDialog.transactionDetailsInputValue.error = resources.getString(R.string.invalid_value)
                } else {
                    binding.transactionsDialog.transactionDetailsInputValue.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.transactionsDialog.transactionDetailsInputValue.windowToken, 0)
            }
        }

        binding.transactionsDialog.transactionDetailsInputAccountText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.transactionsDialog.transactionDetailsInputAccountText.text.isBlank()) {
                    binding.transactionsDialog.transactionDetailsInputAccount.error = resources.getString(R.string.invalid_account)
                } else {
                    binding.transactionsDialog.transactionDetailsInputAccount.error = null
                }
            }
        }

        binding.transactionsDialog.transactionDetailsInputCategoryText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.transactionsDialog.transactionDetailsInputCategoryText.text.isBlank()) {
                    binding.transactionsDialog.transactionDetailsInputCategory.error = resources.getString(R.string.invalid_category)
                } else {
                    binding.transactionsDialog.transactionDetailsInputCategory.error = null
                }
            }
        }

        binding.transactionsDialog.transactionDetailsInputDateText.keyListener = null
        binding.transactionsDialog.transactionDetailsInputDateText.setOnFocusChangeListener { v, isFocus ->
            if (isFocus) {
                datePicker.show(parentFragmentManager, "datePicker")
            } else {
                if (binding.transactionsDialog.transactionDetailsInputDateText.text?.isBlank() != false) {
                    binding.transactionsDialog.transactionDetailsInputDate.error = resources.getString(R.string.invalid_date)
                } else {
                    binding.transactionsDialog.transactionDetailsInputDate.error = null
                }
            }
        }
        binding.transactionsDialog.transactionDetailsInputDateText.setOnClickListener {
            datePicker.show(parentFragmentManager, "datePicker")
        }
        datePicker.addOnPositiveButtonClickListener {
            binding.transactionsDialog.transactionDetailsInputDateText.setText(
                SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(
                    Date(datePicker.selection!!)
                ))
        }

        binding.transactionsDialog.transactionDetailsInputFromToEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.transactionsDialog.transactionDetailsInputFromTo.windowToken, 0)
            }
        }

        binding.transactionsDialog.transactionDetailsInputNoteEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.transactionsDialog.transactionDetailsInputNote.windowToken, 0)
            }
        }

        binding.transactionsError.errorOk.setOnClickListener {
            binding.transactionsError.root.visibility = View.GONE
        }
    }

    private fun onDeleteTransaction() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_transaction_question)
            .setMessage(R.string.delete_transaction_question_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                deleteTransaction()
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
    }

    private fun refresh() {
        binding.transactionsError.root.visibility = View.GONE
        binding.root.isRefreshing = true

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.getAccounts()
        viewModel.isLoadingGetAccounts.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                if (viewModel.getAccountsState.value.error.isBlank()) {
                    if (viewModel.getAccountsState.value.accounts.isNotEmpty()) {
                        loadTransactions()
                    } else {
                        binding.transactionsRecyclerView.visibility = View.GONE
                        binding.transactionsNoTransactionsText.visibility = View.VISIBLE
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        binding.root.isRefreshing = false
                    }
                } else {
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    binding.transactionsRecyclerView.visibility = View.GONE
                    binding.transactionsNoTransactionsText.visibility = View.VISIBLE
                    binding.root.isRefreshing = false
                    if (viewModel.getAccountsState.value.error.startsWith("403") || viewModel.getAccountsState.value.error.startsWith("401")) {
                        startAuthActivity()
                    } else if (viewModel.getAccountsState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.transactionsError.root.visibility = View.VISIBLE
                        binding.transactionsError.errorTitle.text = resources.getString(R.string.error)
                        binding.transactionsError.errorText.text = viewModel.getAccountsState.value.error
                    } else {
                        binding.transactionsError.root.visibility = View.VISIBLE
                        binding.transactionsError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.transactionsError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                }
                binding.root.isRefreshing = false
                viewModel.isLoadingGetAccounts.removeObservers(requireActivity())
            }
        }
    }

    private fun loadTransactions() {
        binding.transactionsError.root.visibility = View.GONE

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.getTransactions(viewModel.getAccountsState.value.accounts.map { it.accountId }, viewModel.getStartMonth(), viewModel.getEndMonth())
        viewModel.isLoadingGetTransactions.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                if (viewModel.getTransactionsState.value.error.isBlank()) {

                    if (viewModel.getTransactionsState.value.transactions.isEmpty()) {
                        binding.transactionsRecyclerView.visibility = View.GONE
                        binding.transactionsNoTransactionsText.visibility = View.VISIBLE
                    } else {
                        binding.transactionsRecyclerView.visibility = View.VISIBLE
                        binding.transactionsNoTransactionsText.visibility = View.GONE
                        val adapter = TransactionItemAdapter(
                            requireContext(),
                            viewModel.getTransactionsState.value.transactions,
                            viewModel.getTransactionsState.value.transactions.size
                        )
                        adapter.setOnTransactionClickListener(this)
                        binding.transactionsRecyclerView.adapter = adapter
                        binding.transactionsRecyclerView.setHasFixedSize(true)
                    }
                } else {
                    binding.transactionsRecyclerView.visibility = View.GONE
                    binding.transactionsNoTransactionsText.visibility = View.VISIBLE
                    binding.root.isRefreshing = false
                    if (viewModel.getTransactionsState.value.error.contains("Email is not verified", ignoreCase = true) || viewModel.getTransactionsState.value.error.startsWith("401")) {
                        startAuthActivity()
                    } else if (viewModel.getTransactionsState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.transactionsError.root.visibility = View.VISIBLE
                        binding.transactionsError.errorTitle.text = resources.getString(R.string.error)
                        binding.transactionsError.errorText.text = viewModel.getTransactionsState.value.error
                    } else {
                        binding.transactionsError.root.visibility = View.VISIBLE
                        binding.transactionsError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.transactionsError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                }
                binding.root.isRefreshing = false
                viewModel.isLoadingGetTransactions.removeObservers(requireActivity())
            }
        }
    }

    private fun getTransaction(id: String) {
        binding.transactionsError.root.visibility = View.GONE
        binding.transactionsDialog.root.visibility = View.GONE
        binding.root.isRefreshing = true

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.getTransaction(id)
        viewModel.isLoadingGetTransaction.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.root.isRefreshing = false

                if (viewModel.getTransactionState.value.error.isBlank()) {
                    if (viewModel.getTransactionState.value.transaction == null) {
                        binding.transactionsError.root.visibility = View.VISIBLE
                        binding.transactionsError.errorTitle.text = resources.getString(R.string.error)
                        binding.transactionsError.errorText.text = resources.getString(R.string.invalid_transaction_id)
                    } else {
                        binding.transactionsDialog.root.visibility = View.VISIBLE

                        if (viewModel.getTransactionState.value.transaction!!.value > 0) {
                            binding.transactionsDialog.transactionDetailsTextFromTo.text = resources.getString(R.string.from)
                            binding.transactionsDialog.transactionDetailsInputFromTo.hint = resources.getString(R.string.from_optional)
                            binding.transactionsDialog.transactionDetailsFromToIcon.scaleX = -1f
                            binding.transactionsDialog.transactionDetailsFromToIcon.scaleY = -1f
                        } else {
                            binding.transactionsDialog.transactionDetailsTextFromTo.text = resources.getString(R.string.to)
                            binding.transactionsDialog.transactionDetailsInputFromTo.hint = resources.getString(R.string.to_optional)
                            binding.transactionsDialog.transactionDetailsFromToIcon.scaleX = 1f
                            binding.transactionsDialog.transactionDetailsFromToIcon.scaleY = 1f
                        }

                        val drawableBackground = binding.transactionsDialog.transactionDetailsBackground.background as GradientDrawable
                        if (viewModel.getTransactionState.value.transaction!!.value > 0) {
                            drawableBackground.setColor(Color.GREEN)
                        } else {
                            drawableBackground.setColor(Color.RED)
                        }
                        binding.transactionsDialog.transactionDetailsBackground.background = drawableBackground

                        val sign = when {
                            viewModel.getTransactionState.value.transaction!!.value > 0 -> ""
                            else -> "-"
                        }
                        binding.transactionsDialog.transactionDetailsInputValue.prefixText = sign + Constants.CURRENCIES[AppPreferences.currency]
                        binding.transactionsDialog.transactionDetailsInputValueEditText.filters = arrayOf(InputFilter.LengthFilter(13), DecimalDigitsInputFilter(10, 2))
                        binding.transactionsDialog.transactionDetailsInputValueEditText.setText(viewModel.getTransactionState.value.transaction!!.value.absoluteValue.toString())

                        binding.transactionsDialog.transactionDetailsInputAccountText.setText(viewModel.getTransactionState.value.transaction!!.accountName)
                        setAccounts()
                        
                        binding.transactionsDialog.transactionDetailsInputCategoryText.setText(viewModel.getTransactionState.value.transaction!!.category)
                        val categories = when {
                            viewModel.getTransactionState.value.transaction!!.value > 0 -> resources.getStringArray(R.array.income_categories)
                            else -> resources.getStringArray(R.array.expense_categories)
                        }
                        val adapterCategory = ArrayAdapter(requireContext(), R.layout.list_item_name_item, categories)
                        (binding.transactionsDialog.transactionDetailsInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapterCategory)

                        binding.transactionsDialog.transactionDetailsInputDateText.setText(viewModel.reformatDate(viewModel.getTransactionState.value.transaction!!.date))

                        binding.transactionsDialog.transactionDetailsInputFromToEdit.setText(viewModel.getTransactionState.value.transaction!!.toFromWhom)

                        binding.transactionsDialog.transactionDetailsInputNoteEdit.setText(viewModel.getTransactionState.value.transaction!!.note)

                        binding.transactionsDialog.root.requestLayout()
                    }
                } else {
                    if (viewModel.getTransactionState.value.error.startsWith("403") || viewModel.getTransactionState.value.error.startsWith("401")) {
                        startAuthActivity()
                    } else if (viewModel.getTransactionState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.transactionsError.root.visibility = View.VISIBLE
                        binding.transactionsError.errorTitle.text = resources.getString(R.string.error)
                        binding.transactionsError.errorText.text = viewModel.getTransactionState.value.error
                    } else {
                        binding.transactionsError.root.visibility = View.VISIBLE
                        binding.transactionsError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.transactionsError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                }

                viewModel.isLoadingGetTransaction.removeObservers(requireActivity())
            }
        }
    }

    private fun updateTransaction() {
        binding.transactionsError.root.visibility = View.GONE
        binding.transactionsDialog.transactionDetailsInputValueEditText.clearFocus()
        binding.transactionsDialog.transactionDetailsInputAccountText.clearFocus()
        binding.transactionsDialog.transactionDetailsInputCategoryText.clearFocus()
        binding.transactionsDialog.transactionDetailsInputDateText.clearFocus()
        binding.transactionsDialog.transactionDetailsInputFromToEdit.clearFocus()
        binding.transactionsDialog.transactionDetailsInputNoteEdit.clearFocus()

        if (!viewModel.checkValue(binding.transactionsDialog.transactionDetailsInputValueEditText.text.toString())) {
            binding.transactionsDialog.transactionDetailsInputValue.error = resources.getString(R.string.invalid_value)
        } else if (binding.transactionsDialog.transactionDetailsInputAccountText.text.isBlank()) {
            binding.transactionsDialog.transactionDetailsInputAccount.error = resources.getString(R.string.invalid_account)
        } else if (binding.transactionsDialog.transactionDetailsInputCategoryText.text.isBlank()){
            binding.transactionsDialog.transactionDetailsInputCategory.error = resources.getString(R.string.invalid_category)
        } else if (binding.transactionsDialog.transactionDetailsInputDateText.text?.isBlank() != false) {
            binding.transactionsDialog.transactionDetailsInputDate.error = resources.getString(R.string.invalid_date)
        } else {
            val value = when {
                binding.transactionsDialog.transactionDetailsInputValue.prefixText.toString().contains("-") -> -binding.transactionsDialog.transactionDetailsInputValueEditText.text.toString().toDouble()
                else -> binding.transactionsDialog.transactionDetailsInputValueEditText.text.toString().toDouble()
            }
            val transactionDto = viewModel.toTransactionDto(
                viewModel.getTransactionState.value.transaction?.transactionId,
                binding.transactionsDialog.transactionDetailsInputAccountText.text.toString(),
                binding.transactionsDialog.transactionDetailsInputCategoryText.text.toString(),
                value,
                viewModel.parseToCorrectDate(binding.transactionsDialog.transactionDetailsInputDateText.text.toString()),
                binding.transactionsDialog.transactionDetailsInputFromToEdit.text.toString(),
                binding.transactionsDialog.transactionDetailsInputNoteEdit.text.toString()
            )
            if (transactionDto == null) {
                binding.transactionsError.root.visibility = View.VISIBLE
                binding.transactionsError.errorTitle.text = resources.getString(R.string.error)
                binding.transactionsError.errorText.text = resources.getString(R.string.invalid_account)
            } else {
                binding.root.isRefreshing = true

                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                viewModel.updateTransaction(transactionDto)
                viewModel.isLoadingUpdateTransaction.observe(requireActivity()) { isLoading ->
                    if (!isLoading) {
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        binding.transactionsDialog.root.visibility = View.GONE

                        if (viewModel.updateTransactionState.value.error.isBlank()) {
                            Toast.makeText(requireContext(), resources.getString(R.string.transaction_is_updated), Toast.LENGTH_SHORT).show()
                            refresh()
                        } else if (viewModel.updateTransactionState.value.error.contains("Email is not verified", ignoreCase = true) || viewModel.updateTransactionState.value.error.startsWith("401")) {
                            startAuthActivity()
                        } else if (viewModel.updateTransactionState.value.error.contains("HTTP", ignoreCase = true)) {
                            binding.transactionsError.root.visibility = View.VISIBLE
                            binding.transactionsError.errorTitle.text = resources.getString(R.string.error)
                            binding.transactionsError.errorText.text = viewModel.updateTransactionState.value.error
                        } else {
                            binding.transactionsError.root.visibility = View.VISIBLE
                            binding.transactionsError.errorTitle.text = resources.getString(R.string.network_error)
                            binding.transactionsError.errorText.text = resources.getString(R.string.connection_failed_message)
                        }

                        binding.root.isRefreshing = false
                        viewModel.isLoadingUpdateTransaction.removeObservers(requireActivity())
                    }
                }
            }
        }
    }

    private fun deleteTransaction() {
        binding.transactionsError.root.visibility = View.GONE
        binding.transactionsDialog.root.visibility = View.GONE

        if (viewModel.getTransactionState.value.transaction?.transactionId == null) {
            binding.transactionsError.root.visibility = View.VISIBLE
            binding.transactionsError.errorTitle.text = resources.getString(R.string.error)
            binding.transactionsError.errorText.text = resources.getString(R.string.invalid_account_id)
        } else {
            binding.root.isRefreshing = true
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            viewModel.deleteTransaction(viewModel.getTransactionState.value.transaction!!.transactionId.toString())
            viewModel.isLoadingDeleteTransaction.observe(requireActivity()) { isLoading ->
                if (!isLoading) {
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    binding.root.isRefreshing = false
                    if (viewModel.deleteTransactionState.value.error.isBlank()) {
                        Toast.makeText(requireContext(), R.string.transaction_is_deleted, Toast.LENGTH_SHORT).show()
                        refresh()
                    } else {
                        if (viewModel.deleteTransactionState.value.error.startsWith("403") || viewModel.deleteTransactionState.value.error.startsWith(
                                "401"
                            )
                        ) {
                            startAuthActivity()
                        } else if (viewModel.deleteTransactionState.value.error.contains(
                                "HTTP",
                                ignoreCase = true
                            )
                        ) {
                            binding.transactionsError.root.visibility = View.VISIBLE
                            binding.transactionsError.errorTitle.text =
                                resources.getString(R.string.error)
                            binding.transactionsError.errorText.text =
                                viewModel.deleteTransactionState.value.error
                        } else {
                            binding.transactionsError.root.visibility = View.VISIBLE
                            binding.transactionsError.errorTitle.text =
                                resources.getString(R.string.network_error)
                            binding.transactionsError.errorText.text =
                                resources.getString(R.string.connection_failed_message)
                        }
                    }
                    viewModel.isLoadingDeleteTransaction.removeObservers(requireActivity())
                }
            }
        }
    }

    private fun setAccounts() {
        val items = viewModel.getAccountsState.value.accounts.map { it.name }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, items)
        (binding.transactionsDialog.transactionDetailsInputAccount.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    override fun onTransactionClick(transaction: Transaction) {
        getTransaction(transaction.transactionId.toString())
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