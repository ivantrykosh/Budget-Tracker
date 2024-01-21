package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.databinding.DialogAccountDetailsBinding
import com.ivantrykosh.app.budgettracker.client.databinding.DialogCreateAccountBinding
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentAccountsBinding
import com.ivantrykosh.app.budgettracker.client.domain.model.SubAccount
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.adapter.AccountItemAdapter
import dagger.hilt.android.AndroidEntryPoint

/**
 * Account fragment
 */
@AndroidEntryPoint
class AccountsFragment : Fragment(), OnAccountClickListener {
    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!

    private lateinit var dialogAccountDetailsBinding: DialogAccountDetailsBinding

    private lateinit var dialogCreateAccountBinding: DialogCreateAccountBinding

    private val viewModel: AccountsViewModel by viewModels()

    private lateinit var dialogAccountDetails: Dialog

    private lateinit var dialogCreateAccount: Dialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.isEnabled = false

        binding.accountsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.accountsRecyclerView.adapter = AccountItemAdapter(requireContext(), emptyList())

        refreshAccounts()

        binding.accountsTopAppBar.setNavigationOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        binding.accountsTopAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.accounts_refresh -> {
                    refreshAccounts()
                    true
                }
                else -> false
            }
        }

        dialogCreateAccount = Dialog(requireContext(), R.style.DialogTheme)
        dialogCreateAccountBinding = DialogCreateAccountBinding.inflate(layoutInflater)
        dialogCreateAccount.setContentView(dialogCreateAccountBinding.root)
        dialogCreateAccount.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialogAccountDetails = Dialog(requireContext(), R.style.DialogTheme)
        dialogAccountDetailsBinding = DialogAccountDetailsBinding.inflate(layoutInflater)
        dialogAccountDetails.setContentView(dialogAccountDetailsBinding.root)
        dialogAccountDetails.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding.accountsButtonAddAccount.setOnClickListener {
            binding.accountsError.root.visibility = View.GONE
            dialogCreateAccount.show()
            dialogCreateAccountBinding.createAccountInputNameEditText.text = null
            dialogCreateAccountBinding.createAccountInputName.error = null
        }

        dialogAccountDetailsBinding.accountDetailsButtonCancel.setOnClickListener {
            dialogAccountDetails.hide()
            refreshAccounts()
        }

        dialogAccountDetailsBinding.accountDetailsButtonDeleteAccount.setOnClickListener {
            onDeleteAccount()
        }

        dialogAccountDetailsBinding.accountDetailsButtonOk.setOnClickListener {
            updateAccount()
        }

        dialogAccountDetailsBinding.accountDetailsInputNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkName(dialogAccountDetailsBinding.accountDetailsInputNameEditText.text.toString(), viewModel.getAccountState.value?.account?.accountId ?: 0)) {
                    dialogAccountDetailsBinding.accountDetailsInputName.error = resources.getString(R.string.invalid_account_name)
                } else {
                    dialogAccountDetailsBinding.accountDetailsInputName.error = null
                }
                hideKeyboard(dialogAccountDetailsBinding.accountDetailsInputNameEditText.windowToken)
            }
        }

        dialogCreateAccountBinding.createAccountButtonCancel.setOnClickListener {
            dialogCreateAccount.hide()
            refreshAccounts()
        }

        dialogCreateAccountBinding.createAccountButtonOk.setOnClickListener {
            createAccount()
        }

        dialogCreateAccountBinding.createAccountInputNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkName(dialogCreateAccountBinding.createAccountInputNameEditText.text.toString())) {
                    dialogCreateAccountBinding.createAccountInputName.error = resources.getString(R.string.invalid_account_name)
                } else {
                    dialogCreateAccountBinding.createAccountInputName.error = null
                }
                hideKeyboard(dialogCreateAccountBinding.createAccountInputNameEditText.windowToken)
            }
        }

        binding.accountsError.errorOk.setOnClickListener {
            binding.accountsError.root.visibility = View.GONE
        }
    }

    /**
     * On delete account click
     */
    private fun onDeleteAccount() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_account_question)
            .setMessage(R.string.delete_account_question_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                dialogAccountDetails.hide()
                deleteAccount()
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
    }

    /**
     * Refresh accounts
     */
    private fun refreshAccounts() {
        progressStart()
        binding.accountsError.root.visibility = View.GONE

        viewModel.getAccounts()
        viewModel.getAccountsState.observe(requireActivity()) { getAccounts ->
            if (!getAccounts.isLoading) {
                progressEnd()

                when (getAccounts.error) {
                    null -> {
                        val adapter = AccountItemAdapter(requireContext(), getAccounts.accounts)
                        adapter.setOnAccountClickListener(this)
                        binding.accountsRecyclerView.adapter = adapter
                        binding.accountsRecyclerView.setHasFixedSize(true)

                        if (getAccounts.accounts.isEmpty()) {
                            Toast.makeText(requireContext(), R.string.you_do_not_have_any_accounts, Toast.LENGTH_SHORT).show()
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
     * Get account
     */
    private fun getAccount(id: Long) {
        binding.accountsError.root.visibility = View.GONE
        progressStart()

        viewModel.getAccount(id)
        viewModel.getAccountState.observe(requireActivity()) { getAccount ->
            if (!getAccount.isLoading) {
                progressEnd()

                if (getAccount.error == null) {
                    if (getAccount.account == null) {
                        showError(resources.getString(R.string.error), resources.getString(R.string.invalid_account_id))
                    } else {
                        dialogAccountDetails.show()
                        dialogAccountDetailsBinding.accountDetailsInputName.error = null
                        dialogAccountDetailsBinding.accountDetailsIncomesValue.text = viewModel.getFormat().format(getAccount.account.incomesSum)
                        dialogAccountDetailsBinding.accountDetailsExpensesValue.text = viewModel.getFormat().format(getAccount.account.expensesSum)
                        dialogAccountDetailsBinding.accountDetailsTotalValue.text = viewModel.getFormat().format(getAccount.account.incomesSum.plus(getAccount.account.expensesSum))
                        dialogAccountDetailsBinding.accountDetailsInputNameEditText.setText(getAccount.account.name)
                        dialogAccountDetailsBinding.root.requestLayout()
                    }
                } else {
                    showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occurred))
                }

                viewModel.getAccountState.removeObservers(requireActivity())
            }
        }
    }

    /**
     * Create account
     */
    private fun createAccount() {
        clearFocusOfCreateAccountFields()

        if (!viewModel.checkName(dialogCreateAccountBinding.createAccountInputNameEditText.text.toString())) {
            dialogCreateAccountBinding.createAccountInputName.error = resources.getString(R.string.invalid_account_name)
        } else {
            progressStart()
            dialogCreateAccount.hide()

            viewModel.createAccount(dialogCreateAccountBinding.createAccountInputNameEditText.text.toString())
            viewModel.createAccountState.observe(requireActivity()) { createAccount ->
                if (!createAccount.isLoading) {
                    progressEnd()

                    when (createAccount.error) {
                        null -> {
                            Toast.makeText(requireContext(), resources.getString(R.string.account_was_created), Toast.LENGTH_SHORT).show()
                            refreshAccounts()
                        }
                        else -> {
                            showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occurred))
                        }
                    }

                    viewModel.createAccountState.removeObservers(requireActivity())
                }
            }
        }
    }

    /**
     * Update account
     */
    private fun updateAccount() {
        clearFocusOfUpdateAccountFields()

        if (!viewModel.checkName(dialogAccountDetailsBinding.accountDetailsInputNameEditText.text.toString(), viewModel.getAccountState.value?.account?.accountId ?: 0)) {
            dialogAccountDetailsBinding.accountDetailsInputName.error = resources.getString(R.string.invalid_account_name)
        } else {
            progressStart()
            dialogAccountDetails.hide()

            viewModel.updateAccount(viewModel.getAccountState.value!!.account!!.accountId, dialogAccountDetailsBinding.accountDetailsInputNameEditText.text.toString())
            viewModel.updateAccountState.observe(requireActivity()) { updateAccount ->
                if (!updateAccount.isLoading) {
                    progressEnd()

                    when (updateAccount.error) {
                        null -> {
                            Toast.makeText(requireContext(), resources.getString(R.string.account_was_updated), Toast.LENGTH_SHORT).show()
                            refreshAccounts()
                        }
                        else -> {
                            showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occurred))
                        }
                    }

                    viewModel.updateAccountState.removeObservers(requireActivity())
                }
            }
        }
    }

    /**
     * Delete account
     */
    private fun deleteAccount() {
        progressStart()

        viewModel.deleteAccount(viewModel.getAccountState.value?.account!!.accountId)
        viewModel.deleteAccountState.observe(requireActivity()) { deleteAccount ->
            if (!deleteAccount.isLoading) {
                progressEnd()

                when (deleteAccount.error) {
                    null -> {
                        Toast.makeText(requireContext(), resources.getString(R.string.account_was_deleted), Toast.LENGTH_SHORT).show()
                        refreshAccounts()
                    }
                    else -> {
                        showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occurred))
                    }
                }

                viewModel.deleteAccountState.removeObservers(requireActivity())
            }
        }
    }

    override fun onAccountClick(account: SubAccount) {
        getAccount(account.accountId)
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
     * Clear focus of create account name and emails fields
     */
    private fun clearFocusOfCreateAccountFields() {
        dialogCreateAccountBinding.createAccountInputNameEditText.clearFocus()
    }

    /**
     * Clear focus of update account name and emails fields
     */
    private fun clearFocusOfUpdateAccountFields() {
        dialogAccountDetailsBinding.accountDetailsInputNameEditText.clearFocus()
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
        binding.accountsError.root.visibility = View.VISIBLE
        binding.accountsError.errorTitle.text = title
        binding.accountsError.errorText.text = text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialogCreateAccount.dismiss()
        dialogAccountDetails.dismiss()
        _binding = null
    }
}