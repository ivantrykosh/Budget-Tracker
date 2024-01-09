package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions

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
                hideKeyboard(binding.transactionsDialog.transactionDetailsInputValue.windowToken)
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
        binding.transactionsDialog.transactionDetailsInputDateText.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                if (!datePicker.isAdded) {
                    datePicker.show(parentFragmentManager, "datePicker")
                }
            } else {
                if (binding.transactionsDialog.transactionDetailsInputDateText.text?.isBlank() != false) {
                    binding.transactionsDialog.transactionDetailsInputDate.error = resources.getString(R.string.invalid_date)
                } else {
                    binding.transactionsDialog.transactionDetailsInputDate.error = null
                }
            }
        }
        binding.transactionsDialog.transactionDetailsInputDateText.setOnClickListener {
            if (!datePicker.isAdded) {
                datePicker.show(parentFragmentManager, "datePicker")
            }
        }
        datePicker.addOnPositiveButtonClickListener {
            binding.transactionsDialog.transactionDetailsInputDateText.setText(
                SimpleDateFormat(AppPreferences.dateFormat, Locale.getDefault()).format(
                    Date(datePicker.selection!!)
                ))
        }

        binding.transactionsDialog.transactionDetailsInputFromToEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(binding.transactionsDialog.transactionDetailsInputFromTo.windowToken)
            }
        }

        binding.transactionsDialog.transactionDetailsInputNoteEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(binding.transactionsDialog.transactionDetailsInputNote.windowToken)
            }
        }

        binding.transactionsError.errorOk.setOnClickListener {
            binding.transactionsError.root.visibility = View.GONE
        }
    }

    /**
     * On delete transaction click
     */
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
                        showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
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
                        showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
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
        binding.transactionsDialog.root.visibility = View.GONE

        progressStart()

        viewModel.getTransaction(id)
        viewModel.getTransactionState.observe(requireActivity()) { getTransaction ->
            if (!getTransaction.isLoading) {
                progressEnd()

                if (getTransaction.error == null) {
                    if (getTransaction.transaction == null) {
                        showError(resources.getString(R.string.error), resources.getString(R.string.invalid_transaction_id))
                    } else {
                        binding.transactionsDialog.root.visibility = View.VISIBLE

                        if (getTransaction.transaction.value > 0) {
                            setTextAndScaleForToFromWhom(resources.getString(R.string.from), resources.getString(R.string.from_optional), -1f, -1f)
                        } else {
                            setTextAndScaleForToFromWhom(resources.getString(R.string.to), resources.getString(R.string.to_optional), 1f, 1f)
                        }

                        val drawableBackground = binding.transactionsDialog.transactionDetailsBackground.background as GradientDrawable
                        if (getTransaction.transaction.value > 0) {
                            drawableBackground.setColor(Color.GREEN)
                        } else {
                            drawableBackground.setColor(Color.RED)
                        }
                        binding.transactionsDialog.transactionDetailsBackground.background = drawableBackground

                        val sign = when {
                            getTransaction.transaction.value > 0 -> ""
                            else -> "-"
                        }
                        binding.transactionsDialog.transactionDetailsInputValue.prefixText = sign + Constants.CURRENCIES[AppPreferences.currency]
                        binding.transactionsDialog.transactionDetailsInputValueEditText.filters = arrayOf(InputFilter.LengthFilter(13), DecimalDigitsInputFilter(10, 2))
                        binding.transactionsDialog.transactionDetailsInputValueEditText.setText(getTransaction.transaction.value.absoluteValue.toString())

                        binding.transactionsDialog.transactionDetailsInputAccountText.setText(getTransaction.transaction.accountName)
                        setAccounts()
                        
                        binding.transactionsDialog.transactionDetailsInputCategoryText.setText(getTransaction.transaction.category)
                        val categories = when {
                            getTransaction.transaction.value > 0 -> resources.getStringArray(R.array.income_categories)
                            else -> resources.getStringArray(R.array.expense_categories)
                        }
                        val adapterCategory = ArrayAdapter(requireContext(), R.layout.list_item_name_item, categories)
                        (binding.transactionsDialog.transactionDetailsInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapterCategory)

                        binding.transactionsDialog.transactionDetailsInputDateText.setText(viewModel.reformatDate(getTransaction.transaction.date))

                        binding.transactionsDialog.transactionDetailsInputFromToEdit.setText(getTransaction.transaction.toFromWhom)

                        binding.transactionsDialog.transactionDetailsInputNoteEdit.setText(getTransaction.transaction.note)

                        binding.transactionsDialog.root.requestLayout()
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
                        showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
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
            val value = binding.transactionsDialog.transactionDetailsInputValueEditText.text.toString().toDouble().absoluteValue
            val transactionDto = viewModel.toTransactionDto(
                viewModel.getTransactionState.value!!.transaction?.transactionId,
                binding.transactionsDialog.transactionDetailsInputAccountText.text.toString(),
                binding.transactionsDialog.transactionDetailsInputCategoryText.text.toString(),
                value,
                viewModel.parseStringToDate(binding.transactionsDialog.transactionDetailsInputDateText.text.toString()),
                binding.transactionsDialog.transactionDetailsInputFromToEdit.text.toString(),
                binding.transactionsDialog.transactionDetailsInputNoteEdit.text.toString()
            )
            if (transactionDto == null) {
                showError(resources.getString(R.string.error), resources.getString(R.string.invalid_account))
            } else {
                progressStart()

                viewModel.updateTransaction(transactionDto)
                viewModel.updateTransactionState.observe(requireActivity()) { updateTransaction ->
                    if (!updateTransaction.isLoading) {
                        progressEnd()
                        binding.transactionsDialog.root.visibility = View.GONE

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
                                showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
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
        binding.transactionsDialog.root.visibility = View.GONE

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
                            showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
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
        (binding.transactionsDialog.transactionDetailsInputAccount.editText as? AutoCompleteTextView)?.setAdapter(adapter)
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
        binding.transactionsDialog.transactionDetailsTextFromTo.text = text
        binding.transactionsDialog.transactionDetailsInputFromTo.hint = hint
        binding.transactionsDialog.transactionDetailsFromToIcon.scaleX = scaleX
        binding.transactionsDialog.transactionDetailsFromToIcon.scaleY = scaleY
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}