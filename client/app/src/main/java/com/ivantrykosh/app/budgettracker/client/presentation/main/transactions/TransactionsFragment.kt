package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.databinding.DialogFilterBinding
import com.ivantrykosh.app.budgettracker.client.databinding.DialogTransactionDetailsBinding
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentTransactionsBinding
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.adapter.AccountItemAdapter
import com.ivantrykosh.app.budgettracker.client.presentation.main.adapter.TransactionItemAdapter
import com.ivantrykosh.app.budgettracker.client.presentation.main.filter.DecimalDigitsInputFilter
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * Transactions fragment
 */
@AndroidEntryPoint
class TransactionsFragment : Fragment(), OnTransactionClickListener {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var dialogFilterBinding: DialogFilterBinding

    private lateinit var dialogTransactionDetailsBinding: DialogTransactionDetailsBinding

    private val viewModel: TransactionsViewModel by viewModels()

    private val datePicker =
        MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

    private lateinit var dialogFilter: Dialog

    private lateinit var dialogTransactionDetails: Dialog

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

        dialogTransactionDetails = Dialog(requireContext(), R.style.TransactionDialogTheme)
        dialogTransactionDetailsBinding = DialogTransactionDetailsBinding.inflate(layoutInflater)
        dialogTransactionDetails.setContentView(dialogTransactionDetailsBinding.root)
        dialogTransactionDetails.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding.transactionsTopAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.transactions_refresh -> {
                    refresh()
                    true
                }
                R.id.transactions_filter -> {
                    showFilters()
                    true
                }
                else -> false
            }
        }

        dialogTransactionDetailsBinding.transactionDetailsButtonCancel.setOnClickListener {
            dialogTransactionDetails.hide()
            refresh()
        }

        dialogTransactionDetailsBinding.transactionDetailsButtonDeleteTransaction.setOnClickListener {
            onDeleteTransaction()
        }

        dialogTransactionDetailsBinding.transactionDetailsButtonOk.setOnClickListener {
            updateTransaction()
        }

        dialogTransactionDetailsBinding.transactionDetailsInputValueEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkValue(dialogTransactionDetailsBinding.transactionDetailsInputValueEditText.text.toString())) {
                    dialogTransactionDetailsBinding.transactionDetailsInputValue.error = resources.getString(R.string.invalid_value)
                } else {
                    dialogTransactionDetailsBinding.transactionDetailsInputValue.error = null
                }
                hideKeyboard(dialogTransactionDetailsBinding.transactionDetailsInputValue.windowToken)
            }
        }

        dialogTransactionDetailsBinding.transactionDetailsInputAccountText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (dialogTransactionDetailsBinding.transactionDetailsInputAccountText.text.isBlank()) {
                    dialogTransactionDetailsBinding.transactionDetailsInputAccount.error = resources.getString(R.string.invalid_account)
                } else {
                    dialogTransactionDetailsBinding.transactionDetailsInputAccount.error = null
                }
            }
        }

        dialogTransactionDetailsBinding.transactionDetailsInputCategoryText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (dialogTransactionDetailsBinding.transactionDetailsInputCategoryText.text.isBlank()) {
                    dialogTransactionDetailsBinding.transactionDetailsInputCategory.error = resources.getString(R.string.invalid_category)
                } else {
                    dialogTransactionDetailsBinding.transactionDetailsInputCategory.error = null
                }
            }
        }

        dialogTransactionDetailsBinding.transactionDetailsInputDateText.keyListener = null
        dialogTransactionDetailsBinding.transactionDetailsInputDateText.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                if (!datePicker.isAdded) {
                    datePicker.show(parentFragmentManager, "datePicker")
                }
            } else {
                if (dialogTransactionDetailsBinding.transactionDetailsInputDateText.text?.isBlank() != false) {
                    dialogTransactionDetailsBinding.transactionDetailsInputDate.error = resources.getString(R.string.invalid_date)
                } else {
                    dialogTransactionDetailsBinding.transactionDetailsInputDate.error = null
                }
            }
        }
        dialogTransactionDetailsBinding.transactionDetailsInputDateText.setOnClickListener {
            if (!datePicker.isAdded) {
                datePicker.show(parentFragmentManager, "datePicker")
            }
        }
        datePicker.addOnPositiveButtonClickListener {
            dialogTransactionDetailsBinding.transactionDetailsInputDateText.setText(
                SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(
                    Date(datePicker.selection!!)
                ))
        }

        dialogTransactionDetailsBinding.transactionDetailsInputFromToEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(dialogTransactionDetailsBinding.transactionDetailsInputFromTo.windowToken)
            }
        }

        dialogTransactionDetailsBinding.transactionDetailsInputNoteEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(dialogTransactionDetailsBinding.transactionDetailsInputNote.windowToken)
            }
        }

        binding.transactionsError.errorOk.setOnClickListener {
            binding.transactionsError.root.visibility = View.GONE
        }

        dialogFilter = Dialog(requireContext(), R.style.DialogTheme)
        dialogFilterBinding = DialogFilterBinding.inflate(layoutInflater)
        dialogFilter.setContentView(dialogFilterBinding.root)
        dialogFilter.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val types = resources.getStringArray(R.array.transaction_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, types)
        (dialogFilterBinding.filterInputTransactionType.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        dialogFilterBinding.filterInputTransactionTypeText.setText(types[0], false)
        dialogFilterBinding.filterInputTransactionTypeText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (dialogFilterBinding.filterInputTransactionTypeText.text.isBlank()) {
                    dialogFilterBinding.filterInputTransactionType.error = resources.getString(R.string.invalid_transaction_type)
                } else {
                    dialogFilterBinding.filterInputTransactionTypeText.error = null
                }
            }
        }

        dialogFilterBinding.filterInputAccountText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (dialogFilterBinding.filterInputAccountText.text.isBlank()) {
                    dialogFilterBinding.filterInputAccount.error = resources.getString(R.string.invalid_account)
                } else {
                    dialogFilterBinding.filterInputAccountText.error = null
                }
            }
        }
        var accountPosition = 0
        dialogFilterBinding.filterInputAccountText.setOnItemClickListener { _, _, position, _ ->
            accountPosition = position
        }

        dialogFilterBinding.filterButtonCancel.setOnClickListener {
            dialogFilter.hide()
        }

        dialogFilterBinding.filterButtonOk.setOnClickListener {
            dialogFilter.hide()

            val transactionType = when (dialogFilterBinding.filterInputTransactionTypeText.text.toString()) {
                resources.getString(R.string.incomes) -> 1
                resources.getString(R.string.expenses) -> -1
                else -> 0
            }
            viewModel.setTransactionType(transactionType)

            val account = when (accountPosition) {
                0 -> null
                else -> dialogFilterBinding.filterInputAccountText.text.toString()
            }
            viewModel.setAccount(account)

            viewModel.setIsDateChecked(dialogFilterBinding.filterCheckboxDate.isChecked)
            viewModel.setIsValueChecked(dialogFilterBinding.filterCheckboxValue.isChecked)

            refresh()
        }
    }

    /**
     * Show filters for transactions
     */
    private fun showFilters() {
        binding.transactionsError.root.visibility = View.GONE

        val transactionType = when {
            (viewModel.currentTransactionType.value ?: 0) > 0 -> resources.getString(R.string.incomes)
            (viewModel.currentTransactionType.value ?: 0) < 0 -> resources.getString(R.string.expenses)
            else -> resources.getString(R.string.all)
        }
        dialogFilterBinding.filterInputTransactionTypeText.setText(transactionType, false)
        val accountName = when (viewModel.currentAccount.value) {
            null -> resources.getString(R.string.select_all)
            else -> viewModel.currentAccount.value
        }
        dialogFilterBinding.filterInputAccountText.setText(accountName, false)
        dialogFilterBinding.filterCheckboxDate.isChecked = viewModel.isDateChecked.value ?: false
        dialogFilterBinding.filterCheckboxValue.isChecked = viewModel.isValueChecked.value ?: false

        dialogFilter.show()

        val items: MutableList<String> = viewModel.getAccountsState.value?.accounts?.map { it.name }?.toMutableList() ?: mutableListOf()
        items.add(0, resources.getString(R.string.select_all))
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, items)
        (dialogFilterBinding.filterInputAccount.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    /**
     * On delete transaction click
     */
    private fun onDeleteTransaction() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_transaction_question)
            .setMessage(R.string.delete_transaction_question_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                dialogTransactionDetails.hide()
                deleteTransaction()
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
    }

    /**
     * Refresh accounts
     */
    private fun refresh() {
        binding.transactionsError.root.visibility = View.GONE
        binding.transactionsRecyclerView.visibility = View.GONE
        binding.transactionsNoTransactionsText.visibility = View.VISIBLE
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
     * Load transactions
     */
    private fun loadTransactions() {
        binding.transactionsError.root.visibility = View.GONE
        binding.transactionsRecyclerView.visibility = View.GONE
        binding.transactionsNoTransactionsText.visibility = View.VISIBLE

        progressStart()

        viewModel.getTransactions(viewModel.getAccountsState.value!!.accounts.map { it.accountId }, viewModel.getStartMonth(), viewModel.getEndMonth())
        viewModel.getTransactionsState.observe(requireActivity()) { getTransactions ->
            if (!getTransactions.isLoading) {
                progressEnd()

                when (getTransactions.error) {
                    null -> {
                        if (getTransactions.transactions.isNotEmpty()) {
                            binding.transactionsRecyclerView.visibility = View.VISIBLE
                            binding.transactionsNoTransactionsText.visibility = View.GONE
                            val adapter = TransactionItemAdapter(
                                requireContext(),
                                getTransactions.transactions,
                                getTransactions.transactions.size
                            )
                            adapter.setOnTransactionClickListener(this)
                            binding.transactionsRecyclerView.adapter = adapter
                            binding.transactionsRecyclerView.setHasFixedSize(true)
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

    /**
     * Get transaction by its ID
     */
    private fun getTransaction(id: String) {
        binding.transactionsError.root.visibility = View.GONE

        progressStart()

        viewModel.getTransaction(id)
        viewModel.getTransactionState.observe(requireActivity()) { getTransaction ->
            if (!getTransaction.isLoading) {
                progressEnd()

                if (getTransaction.error == null) {
                    if (getTransaction.transaction == null) {
                        showError(resources.getString(R.string.error), resources.getString(R.string.invalid_transaction_id))
                    } else {
                        dialogTransactionDetails.show()

                        if (getTransaction.transaction.value > 0) {
                            setTextAndScaleForToFromWhom(resources.getString(R.string.from), resources.getString(R.string.from_optional), -1f, -1f)
                        } else {
                            setTextAndScaleForToFromWhom(resources.getString(R.string.to), resources.getString(R.string.to_optional), 1f, 1f)
                        }

                        val drawableBackground = dialogTransactionDetailsBinding.transactionDetailsBackground.background as GradientDrawable
                        if (getTransaction.transaction.value > 0) {
                            drawableBackground.setColor(Color.GREEN)
                        } else {
                            drawableBackground.setColor(Color.RED)
                        }
                        dialogTransactionDetailsBinding.transactionDetailsBackground.background = drawableBackground

                        val sign = when {
                            getTransaction.transaction.value > 0 -> ""
                            else -> "-"
                        }
                        dialogTransactionDetailsBinding.transactionDetailsInputValue.prefixText = sign + Constants.CURRENCIES[AppPreferences.currency]
                        dialogTransactionDetailsBinding.transactionDetailsInputValueEditText.filters = arrayOf(InputFilter.LengthFilter(13), DecimalDigitsInputFilter(10, 2))
                        dialogTransactionDetailsBinding.transactionDetailsInputValueEditText.setText(getTransaction.transaction.value.absoluteValue.toString())

                        dialogTransactionDetailsBinding.transactionDetailsInputAccountText.setText(getTransaction.transaction.accountName)
                        setAccounts()
                        
                        dialogTransactionDetailsBinding.transactionDetailsInputCategoryText.setText(getTransaction.transaction.category)
                        val categories = when {
                            getTransaction.transaction.value > 0 -> resources.getStringArray(R.array.income_categories)
                            else -> resources.getStringArray(R.array.expense_categories)
                        }
                        val adapterCategory = ArrayAdapter(requireContext(), R.layout.list_item_name_item, categories)
                        (dialogTransactionDetailsBinding.transactionDetailsInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapterCategory)

                        dialogTransactionDetailsBinding.transactionDetailsInputDateText.setText(viewModel.reformatDate(getTransaction.transaction.date))

                        dialogTransactionDetailsBinding.transactionDetailsInputFromToEdit.setText(getTransaction.transaction.toFromWhom)

                        dialogTransactionDetailsBinding.transactionDetailsInputNoteEdit.setText(getTransaction.transaction.note)

                        dialogTransactionDetailsBinding.root.requestLayout()
                    }
                } else when (getTransaction.error) {
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

                viewModel.getTransactionState.removeObservers(requireActivity())
            }
        }
    }

    /**
     * Update transaction
     */
    private fun updateTransaction() {
        binding.transactionsError.root.visibility = View.GONE
        dialogTransactionDetailsBinding.transactionDetailsInputValueEditText.clearFocus()
        dialogTransactionDetailsBinding.transactionDetailsInputAccountText.clearFocus()
        dialogTransactionDetailsBinding.transactionDetailsInputCategoryText.clearFocus()
        dialogTransactionDetailsBinding.transactionDetailsInputDateText.clearFocus()
        dialogTransactionDetailsBinding.transactionDetailsInputFromToEdit.clearFocus()
        dialogTransactionDetailsBinding.transactionDetailsInputNoteEdit.clearFocus()

        if (!viewModel.checkValue(dialogTransactionDetailsBinding.transactionDetailsInputValueEditText.text.toString())) {
            dialogTransactionDetailsBinding.transactionDetailsInputValue.error = resources.getString(R.string.invalid_value)
        } else if (dialogTransactionDetailsBinding.transactionDetailsInputAccountText.text.isBlank()) {
            dialogTransactionDetailsBinding.transactionDetailsInputAccount.error = resources.getString(R.string.invalid_account)
        } else if (dialogTransactionDetailsBinding.transactionDetailsInputCategoryText.text.isBlank()){
            dialogTransactionDetailsBinding.transactionDetailsInputCategory.error = resources.getString(R.string.invalid_category)
        } else if (dialogTransactionDetailsBinding.transactionDetailsInputDateText.text?.isBlank() != false) {
            dialogTransactionDetailsBinding.transactionDetailsInputDate.error = resources.getString(R.string.invalid_date)
        } else {
            val value = dialogTransactionDetailsBinding.transactionDetailsInputValueEditText.text.toString().toDouble().absoluteValue
            val transactionDto = viewModel.toTransactionDto(
                viewModel.getTransactionState.value!!.transaction?.transactionId,
                dialogTransactionDetailsBinding.transactionDetailsInputAccountText.text.toString(),
                dialogTransactionDetailsBinding.transactionDetailsInputCategoryText.text.toString(),
                value,
                viewModel.parseStringToDate(dialogTransactionDetailsBinding.transactionDetailsInputDateText.text.toString()),
                dialogTransactionDetailsBinding.transactionDetailsInputFromToEdit.text.toString(),
                dialogTransactionDetailsBinding.transactionDetailsInputNoteEdit.text.toString()
            )
            if (transactionDto == null) {
                showError(resources.getString(R.string.error), resources.getString(R.string.invalid_account))
            } else {
                dialogTransactionDetails.hide()
                progressStart()

                viewModel.updateTransaction(transactionDto)
                viewModel.updateTransactionState.observe(requireActivity()) { updateTransaction ->
                    if (!updateTransaction.isLoading) {
                        progressEnd()

                        when (updateTransaction.error) {
                            null -> {
                                Toast.makeText(requireContext(), resources.getString(R.string.transaction_is_updated), Toast.LENGTH_SHORT).show()
                                refresh()
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

                        viewModel.updateTransactionState.removeObservers(requireActivity())
                    }
                }
            }
        }
    }

    /**
     * Delete transaction
     */
    private fun deleteTransaction() {
        binding.transactionsError.root.visibility = View.GONE

        if (viewModel.getTransactionState.value?.transaction?.transactionId == null) {
            showError(resources.getString(R.string.error), resources.getString(R.string.invalid_account_id))
        } else {
            progressStart()

            viewModel.deleteTransaction(viewModel.getTransactionState.value!!.transaction!!.transactionId.toString())
            viewModel.deleteTransactionState.observe(requireActivity()) { deleteTransaction ->
                if (!deleteTransaction.isLoading) {
                    progressEnd()

                    when (deleteTransaction.error) {
                        null -> {
                            Toast.makeText(requireContext(), R.string.transaction_is_deleted, Toast.LENGTH_SHORT).show()
                            refresh()
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

                    viewModel.deleteTransactionState.removeObservers(requireActivity())
                }
            }
        }
    }

    /**
     * Set accounts to AutoCompleteTextView
     */
    private fun setAccounts() {
        val items = viewModel.getAccountsState.value?.accounts?.map { it.name } ?: emptyList()
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, items)
        (dialogTransactionDetailsBinding.transactionDetailsInputAccount.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    override fun onTransactionClick(transaction: Transaction) {
        getTransaction(transaction.transactionId.toString())
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
        binding.transactionsError.root.visibility = View.VISIBLE
        binding.transactionsError.errorTitle.text = title
        binding.transactionsError.errorText.text = text
    }

    /**
     * Set text, hint and scales for ToFromWhom
     *
     * @param text text of view
     * @param hint hint of input field
     * @param scaleX scale X for icon
     * @param scaleY scale Y for icon
     */
    private fun setTextAndScaleForToFromWhom(text: String, hint: String, scaleX: Float, scaleY: Float) {
        dialogTransactionDetailsBinding.transactionDetailsTextFromTo.text = text
        dialogTransactionDetailsBinding.transactionDetailsInputFromTo.hint = hint
        dialogTransactionDetailsBinding.transactionDetailsFromToIcon.scaleX = scaleX
        dialogTransactionDetailsBinding.transactionDetailsFromToIcon.scaleY = scaleY
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}