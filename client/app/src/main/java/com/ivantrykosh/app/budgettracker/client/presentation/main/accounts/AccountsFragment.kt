package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts

import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangeAccountDto
import com.ivantrykosh.app.budgettracker.client.databinding.DialogAccountDetailsBinding
import com.ivantrykosh.app.budgettracker.client.databinding.DialogCreateAccountBinding
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentAccountsBinding
import com.ivantrykosh.app.budgettracker.client.domain.model.Account
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.adapter.AccountItemAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

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

    private fun getFormat(): DecimalFormat {
        val pattern = Constants.CURRENCIES[AppPreferences.currency] + "#,##0.00"
        val format = DecimalFormat(pattern)
        format.maximumFractionDigits = 2
        return format
    }

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

        refresh()

        binding.accountsTopAppBar.setNavigationOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        binding.accountsTopAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.accounts_refresh -> {
                    refresh()
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
            dialogCreateAccountBinding.createAccountEditTextInputEmail1.text = null
            dialogCreateAccountBinding.createAccountEditTextInputEmail2.text = null
            dialogCreateAccountBinding.createAccountEditTextInputEmail3.text = null
        }

        dialogAccountDetailsBinding.accountDetailsButtonCancel.setOnClickListener {
            dialogAccountDetails.hide()
            refresh()
        }

        dialogAccountDetailsBinding.accountDetailsButtonDeleteAccount.setOnClickListener {
            onDeleteAccount()
        }

        dialogAccountDetailsBinding.accountDetailsButtonOk.setOnClickListener {
            updateAccount()
        }

        dialogAccountDetailsBinding.accountDetailsInputNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkName(dialogAccountDetailsBinding.accountDetailsInputNameEditText.text.toString())) {
                    dialogAccountDetailsBinding.accountDetailsInputName.error = resources.getString(R.string.invalid_account_name)
                } else {
                    dialogAccountDetailsBinding.accountDetailsInputName.error = null
                }
                hideKeyboard(dialogAccountDetailsBinding.accountDetailsInputNameEditText.windowToken)
            }
        }

        dialogAccountDetailsBinding.accountDetailsEditTextInputEmail1.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(dialogAccountDetailsBinding.accountDetailsEditTextInputEmail1.text.toString())) {
                    dialogAccountDetailsBinding.accountDetailsTextInputEmail1.error = resources.getString(R.string.invalid_email)
                } else {
                    dialogAccountDetailsBinding.accountDetailsTextInputEmail1.error = null
                }
                hideKeyboard(dialogAccountDetailsBinding.accountDetailsEditTextInputEmail1.windowToken)
            }
        }
        dialogAccountDetailsBinding.accountDetailsEditTextInputEmail2.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(dialogAccountDetailsBinding.accountDetailsEditTextInputEmail2.text.toString())) {
                    dialogAccountDetailsBinding.accountDetailsTextInputEmail2.error = resources.getString(R.string.invalid_email)
                } else {
                    dialogAccountDetailsBinding.accountDetailsTextInputEmail2.error = null
                }
                hideKeyboard(dialogAccountDetailsBinding.accountDetailsEditTextInputEmail2.windowToken)
            }
        }
        dialogAccountDetailsBinding.accountDetailsEditTextInputEmail3.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(dialogAccountDetailsBinding.accountDetailsEditTextInputEmail3.text.toString())) {
                    dialogAccountDetailsBinding.accountDetailsTextInputEmail3.error = resources.getString(R.string.invalid_email)
                } else {
                    dialogAccountDetailsBinding.accountDetailsTextInputEmail3.error = null
                }
                hideKeyboard(dialogAccountDetailsBinding.accountDetailsEditTextInputEmail3.windowToken)
            }
        }

        dialogCreateAccountBinding.createAccountButtonCancel.setOnClickListener {
            dialogCreateAccount.hide()
            refresh()
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

        dialogCreateAccountBinding.createAccountEditTextInputEmail1.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(dialogCreateAccountBinding.createAccountEditTextInputEmail1.text.toString())) {
                    dialogCreateAccountBinding.createAccountTextInputEmail1.error = resources.getString(R.string.invalid_email)
                } else {
                    dialogCreateAccountBinding.createAccountTextInputEmail1.error = null
                }
                hideKeyboard(dialogCreateAccountBinding.createAccountEditTextInputEmail1.windowToken)
            }
        }
        dialogCreateAccountBinding.createAccountEditTextInputEmail2.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(dialogCreateAccountBinding.createAccountEditTextInputEmail2.text.toString())) {
                    dialogCreateAccountBinding.createAccountTextInputEmail2.error = resources.getString(R.string.invalid_email)
                } else {
                    dialogCreateAccountBinding.createAccountTextInputEmail2.error = null
                }
                hideKeyboard(dialogCreateAccountBinding.createAccountEditTextInputEmail2.windowToken)
            }
        }
        dialogCreateAccountBinding.createAccountEditTextInputEmail3.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(dialogCreateAccountBinding.createAccountEditTextInputEmail3.text.toString())) {
                    dialogCreateAccountBinding.createAccountTextInputEmail3.error = resources.getString(R.string.invalid_email)
                } else {
                    dialogCreateAccountBinding.createAccountTextInputEmail3.error = null
                }
                hideKeyboard(dialogCreateAccountBinding.createAccountEditTextInputEmail3.windowToken)
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
     * Refresh user
     */
    private fun refresh() {
        progressStart()
        binding.accountsError.root.visibility = View.GONE

        viewModel.getUser()
        viewModel.getUserState.observe(requireActivity()) { getUser ->
            if (!getUser.isLoading) {
                progressEnd()

                if (getUser.user != null) {
                    refreshAccounts()
                }
                else when (getUser.error) {
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

                viewModel.getUserState.removeObservers(requireActivity())
            }
        }
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
     * Get account
     */
    private fun getAccount(id: String) {
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
                        dialogAccountDetailsBinding.accountDetailsIncomesValue.text = getFormat().format(getAccount.account.incomesSum)
                        dialogAccountDetailsBinding.accountDetailsExpensesValue.text = getFormat().format(getAccount.account.expensesSum)
                        dialogAccountDetailsBinding.accountDetailsTotalValue.text = getFormat().format(getAccount.account.incomesSum.plus(getAccount.account.expensesSum))
                        dialogAccountDetailsBinding.accountDetailsInputNameEditText.setText(getAccount.account.name)
                        dialogAccountDetailsBinding.accountDetailsOwnerLayout.visibility = View.GONE
                        if (getAccount.account.userId == (viewModel.getUserState.value?.user?.userId ?: -1)) {
                            dialogAccountDetailsBinding.accountDetailsOwnerLayout.visibility = View.VISIBLE
                            dialogAccountDetailsBinding.accountDetailsInputNameEditText.isFocusableInTouchMode = true
                            dialogAccountDetailsBinding.accountDetailsInputNameEditText.isFocusable = true
                            dialogAccountDetailsBinding.accountDetailsTextInputEmail1.error = null
                            dialogAccountDetailsBinding.accountDetailsEditTextInputEmail1.setText(
                                getAccount.account.email2
                            )
                            dialogAccountDetailsBinding.accountDetailsTextInputEmail2.error = null
                            dialogAccountDetailsBinding.accountDetailsEditTextInputEmail2.setText(
                                getAccount.account.email3
                            )
                            dialogAccountDetailsBinding.accountDetailsTextInputEmail3.error = null
                            dialogAccountDetailsBinding.accountDetailsEditTextInputEmail3.setText(
                                getAccount.account.email4
                            )
                        } else {
                            dialogAccountDetailsBinding.accountDetailsInputNameEditText.isFocusable = false
                        }
                        dialogAccountDetailsBinding.root.requestLayout()
                    }
                } else {
                    when (getAccount.error) {
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
        } else if (!viewModel.checkEmail(dialogCreateAccountBinding.createAccountEditTextInputEmail1.text.toString())) {
            dialogCreateAccountBinding.createAccountTextInputEmail1.error = resources.getString(R.string.invalid_email)
        } else if (!viewModel.checkEmail(dialogCreateAccountBinding.createAccountEditTextInputEmail2.text.toString())) {
            dialogCreateAccountBinding.createAccountTextInputEmail2.error = resources.getString(R.string.invalid_email)
        } else if (!viewModel.checkEmail(dialogCreateAccountBinding.createAccountEditTextInputEmail3.text.toString())) {
            dialogCreateAccountBinding.createAccountTextInputEmail3.error = resources.getString(R.string.invalid_email)
        } else {
            progressStart()
            dialogCreateAccount.hide()

            val changeAccountDto = ChangeAccountDto(
                dialogCreateAccountBinding.createAccountInputNameEditText.text.toString(),
                dialogCreateAccountBinding.createAccountEditTextInputEmail1.text.toString(),
                dialogCreateAccountBinding.createAccountEditTextInputEmail2.text.toString(),
                dialogCreateAccountBinding.createAccountEditTextInputEmail3.text.toString()
            )
            viewModel.createAccount(changeAccountDto)
            viewModel.createAccountState.observe(requireActivity()) { createAccount ->
                if (!createAccount.isLoading) {
                    progressEnd()

                    when (createAccount.error) {
                        null -> {
                            Toast.makeText(requireContext(), resources.getString(R.string.account_was_created), Toast.LENGTH_SHORT).show()
                            refresh()
                        }
                        Constants.ErrorStatusCodes.BAD_REQUEST -> {
                            showError(resources.getString(R.string.error), resources.getString(R.string.invalid_account_data))
                        }
                        Constants.ErrorStatusCodes.CONFLICT -> {
                            showError(resources.getString(R.string.error), resources.getString(R.string.invalid_user_email))
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

                    viewModel.createAccountState.removeObservers(requireActivity())
                }
            }
        }
    }

    /**
     * Update account
     */
    private fun updateAccount() {
        if ((viewModel.getAccountState.value?.account?.userId ?: 0) == viewModel.getUserState.value?.user?.userId) {
            clearFocusOfUpdateAccountFields()

            if (!viewModel.checkName(dialogAccountDetailsBinding.accountDetailsInputNameEditText.text.toString())) {
                dialogAccountDetailsBinding.accountDetailsInputName.error = resources.getString(R.string.invalid_account_name)
            } else if (!viewModel.checkEmail(dialogAccountDetailsBinding.accountDetailsEditTextInputEmail1.text.toString())) {
                dialogAccountDetailsBinding.accountDetailsTextInputEmail1.error = resources.getString(R.string.invalid_email)
            } else if (!viewModel.checkEmail(dialogAccountDetailsBinding.accountDetailsEditTextInputEmail2.text.toString())) {
                dialogAccountDetailsBinding.accountDetailsTextInputEmail2.error = resources.getString(R.string.invalid_email)
            } else if (!viewModel.checkEmail(dialogAccountDetailsBinding.accountDetailsEditTextInputEmail3.text.toString())) {
                dialogAccountDetailsBinding.accountDetailsTextInputEmail3.error = resources.getString(R.string.invalid_email)
            } else {
                progressStart()
                dialogAccountDetails.hide()

                val changeAccountDto = ChangeAccountDto(
                    dialogAccountDetailsBinding.accountDetailsInputNameEditText.text.toString(),
                    dialogAccountDetailsBinding.accountDetailsEditTextInputEmail1.text.toString(),
                    dialogAccountDetailsBinding.accountDetailsEditTextInputEmail2.text.toString(),
                    dialogAccountDetailsBinding.accountDetailsEditTextInputEmail3.text.toString()
                )
                viewModel.updateAccount(viewModel.getAccountState.value?.account?.accountId.toString(), changeAccountDto)
                viewModel.updateAccountState.observe(requireActivity()) { updateAccount ->
                    if (!updateAccount.isLoading) {
                        progressEnd()

                        when (updateAccount.error) {
                            null -> {
                                Toast.makeText(requireContext(), resources.getString(R.string.account_was_updated), Toast.LENGTH_SHORT).show()
                                refresh()
                            }
                            Constants.ErrorStatusCodes.BAD_REQUEST -> {
                                showError(resources.getString(R.string.error), resources.getString(R.string.invalid_account_data))
                            }
                            Constants.ErrorStatusCodes.CONFLICT -> {
                                showError(resources.getString(R.string.error), resources.getString(R.string.invalid_user_email))
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

                        viewModel.updateAccountState.removeObservers(requireActivity())
                    }
                }
            }
        } else {
            dialogAccountDetails.hide()
        }
    }

    /**
     * Delete account
     */
    private fun deleteAccount() {
        if (viewModel.getAccountState.value?.account?.accountId == null) {
            binding.accountsError.root.visibility = View.VISIBLE
            binding.accountsError.errorTitle.text = resources.getString(R.string.error)
            binding.accountsError.errorText.text = resources.getString(R.string.invalid_account_id)
        } else {
            progressStart()

            viewModel.deleteAccount(viewModel.getAccountState.value?.account!!.accountId.toString())
            viewModel.deleteAccountState.observe(requireActivity()) { deleteAccount ->
                if (!deleteAccount.isLoading) {
                    progressEnd()

                    when (deleteAccount.error) {
                        null -> {
                            Toast.makeText(requireContext(), resources.getString(R.string.account_was_deleted), Toast.LENGTH_SHORT).show()
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

                    viewModel.deleteAccountState.removeObservers(requireActivity())
                }
            }
        }
    }

    override fun onAccountClick(account: Account) {
        getAccount(account.accountId.toString())
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
     * Clear focus of create account name and emails fields
     */
    private fun clearFocusOfCreateAccountFields() {
        dialogCreateAccountBinding.createAccountInputNameEditText.clearFocus()
        dialogCreateAccountBinding.createAccountEditTextInputEmail1.clearFocus()
        dialogCreateAccountBinding.createAccountEditTextInputEmail2.clearFocus()
        dialogCreateAccountBinding.createAccountEditTextInputEmail3.clearFocus()
    }

    /**
     * Clear focus of update account name and emails fields
     */
    private fun clearFocusOfUpdateAccountFields() {
        dialogAccountDetailsBinding.accountDetailsInputNameEditText.clearFocus()
        dialogAccountDetailsBinding.accountDetailsEditTextInputEmail1.clearFocus()
        dialogAccountDetailsBinding.accountDetailsEditTextInputEmail2.clearFocus()
        dialogAccountDetailsBinding.accountDetailsEditTextInputEmail3.clearFocus()
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
        _binding = null
    }
}