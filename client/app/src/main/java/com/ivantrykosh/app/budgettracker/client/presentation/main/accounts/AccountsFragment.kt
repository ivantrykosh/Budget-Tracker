package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts

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

    private val viewModel: AccountsViewModel by viewModels()

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
        binding.accountDialog.root.visibility = View.GONE
        binding.createAccountDialog.root.visibility = View.GONE

        binding.accountsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.accountsRecyclerView.adapter = AccountItemAdapter(requireContext(), emptyList())

        refresh()

        binding.accountsTopAppBar.setNavigationOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        binding.accountsTopAppBar.setOnMenuItemClickListener { menuItem ->
            binding.accountDialog.root.visibility = View.GONE
            when (menuItem.itemId) {
                R.id.accounts_refresh -> {
                    refresh()
                    true
                }
                else -> false
            }
        }

        binding.accountsButtonAddAccount.setOnClickListener {
            binding.accountDialog.root.visibility = View.GONE
            binding.createAccountDialog.root.visibility = View.VISIBLE
            binding.createAccountDialog.createAccountInputNameEditText.text = null
            binding.createAccountDialog.createAccountEditTextInputEmail1.text = null
            binding.createAccountDialog.createAccountEditTextInputEmail2.text = null
            binding.createAccountDialog.createAccountEditTextInputEmail3.text = null
        }

        binding.accountDialog.accountDetailsTextCancel.setOnClickListener {
            binding.accountDialog.root.visibility = View.GONE
            refresh()
        }

        binding.createAccountDialog.createAccountTextCancel.setOnClickListener {
            binding.createAccountDialog.root.visibility = View.GONE
            refresh()
        }

        binding.createAccountDialog.createAccountTextOk.setOnClickListener {
            createAccount()
        }

        binding.accountDialog.accountDetailsDeleteAccount.setOnClickListener {
            onDeleteAccount()
        }

        binding.accountDialog.accountDetailsTextOk.setOnClickListener {
            updateAccount()
        }

        binding.accountDialog.accountDetailsInputNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkName(binding.accountDialog.accountDetailsInputNameEditText.text.toString())) {
                    binding.accountDialog.accountDetailsInputName.error = resources.getString(R.string.invalid_account_name)
                } else {
                    binding.accountDialog.accountDetailsInputName.error = null
                }
                hideKeyboard(binding.accountDialog.accountDetailsInputNameEditText.windowToken)
            }
        }

        binding.accountDialog.accountDetailsEditTextInputEmail1.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(binding.accountDialog.accountDetailsEditTextInputEmail1.text.toString())) {
                    binding.accountDialog.accountDetailsTextInputEmail1.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.accountDialog.accountDetailsTextInputEmail1.error = null
                }
                hideKeyboard(binding.accountDialog.accountDetailsEditTextInputEmail1.windowToken)
            }
        }
        binding.accountDialog.accountDetailsEditTextInputEmail2.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(binding.accountDialog.accountDetailsEditTextInputEmail2.text.toString())) {
                    binding.accountDialog.accountDetailsTextInputEmail2.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.accountDialog.accountDetailsTextInputEmail2.error = null
                }
                hideKeyboard(binding.accountDialog.accountDetailsEditTextInputEmail2.windowToken)
            }
        }
        binding.accountDialog.accountDetailsEditTextInputEmail3.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(binding.accountDialog.accountDetailsEditTextInputEmail3.text.toString())) {
                    binding.accountDialog.accountDetailsTextInputEmail3.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.accountDialog.accountDetailsTextInputEmail3.error = null
                }
                hideKeyboard(binding.accountDialog.accountDetailsEditTextInputEmail3.windowToken)
            }
        }

        binding.createAccountDialog.createAccountInputNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkName(binding.createAccountDialog.createAccountInputNameEditText.text.toString())) {
                    binding.createAccountDialog.createAccountInputName.error = resources.getString(R.string.invalid_account_name)
                } else {
                    binding.createAccountDialog.createAccountInputName.error = null
                }
                hideKeyboard(binding.createAccountDialog.createAccountInputNameEditText.windowToken)
            }
        }

        binding.createAccountDialog.createAccountEditTextInputEmail1.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(binding.createAccountDialog.createAccountEditTextInputEmail1.text.toString())) {
                    binding.createAccountDialog.createAccountTextInputEmail1.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.createAccountDialog.createAccountTextInputEmail1.error = null
                }
                hideKeyboard(binding.createAccountDialog.createAccountEditTextInputEmail1.windowToken)
            }
        }
        binding.createAccountDialog.createAccountEditTextInputEmail2.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(binding.createAccountDialog.createAccountEditTextInputEmail2.text.toString())) {
                    binding.createAccountDialog.createAccountTextInputEmail2.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.createAccountDialog.createAccountTextInputEmail2.error = null
                }
                hideKeyboard(binding.createAccountDialog.createAccountEditTextInputEmail2.windowToken)
            }
        }
        binding.createAccountDialog.createAccountEditTextInputEmail3.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(binding.createAccountDialog.createAccountEditTextInputEmail3.text.toString())) {
                    binding.createAccountDialog.createAccountTextInputEmail3.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.createAccountDialog.createAccountTextInputEmail3.error = null
                }
                hideKeyboard(binding.createAccountDialog.createAccountEditTextInputEmail3.windowToken)
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
        hideDialogs()

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
                        showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
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
        hideDialogs()

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
                        showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
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
        progressStart()
        hideDialogs()

        viewModel.getAccount(id)
        viewModel.getAccountState.observe(requireActivity()) { getAccount ->
            if (!getAccount.isLoading) {
                progressEnd()

                if (getAccount.error == null) {
                    if (getAccount.account == null) {
                        showError(resources.getString(R.string.error), resources.getString(R.string.invalid_account_id))
                    } else {
                        binding.accountDialog.root.visibility = View.VISIBLE
                        binding.accountDialog.accountDetailsIncomesValue.text = getFormat().format(getAccount.account.incomesSum)
                        binding.accountDialog.accountDetailsExpensesValue.text = getFormat().format(getAccount.account.expensesSum)
                        binding.accountDialog.accountDetailsTotalValue.text = getFormat().format(getAccount.account.incomesSum.plus(getAccount.account.expensesSum))
                        binding.accountDialog.accountDetailsInputNameEditText.setText(getAccount.account.name)
                        binding.accountDialog.accountDetailsEmailsLayout.visibility = View.GONE
                        binding.accountDialog.accountDetailsDeleteAccount.visibility = View.GONE
                        if (getAccount.account.userId == (viewModel.getUserState.value?.user?.userId ?: -1)) {
                            binding.accountDialog.accountDetailsEmailsLayout.visibility = View.VISIBLE
                            binding.accountDialog.accountDetailsDeleteAccount.visibility = View.VISIBLE
                            binding.accountDialog.accountDetailsInputNameEditText.isFocusableInTouchMode = true
                            binding.accountDialog.accountDetailsInputNameEditText.isFocusable = true
                            binding.accountDialog.accountDetailsTextInputEmail1.error = null
                            binding.accountDialog.accountDetailsEditTextInputEmail1.setText(
                                getAccount.account.email2
                            )
                            binding.accountDialog.accountDetailsTextInputEmail2.error = null
                            binding.accountDialog.accountDetailsEditTextInputEmail2.setText(
                                getAccount.account.email3
                            )
                            binding.accountDialog.accountDetailsTextInputEmail3.error = null
                            binding.accountDialog.accountDetailsEditTextInputEmail3.setText(
                                getAccount.account.email4
                            )
                        } else {
                            binding.accountDialog.accountDetailsInputNameEditText.isFocusable = false
                        }
                        binding.accountDialog.root.requestLayout()
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
                            showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
                        }
                    }
                }

                viewModel.getAccountState.removeObservers(requireActivity())
            }
        }
    }

    private fun createAccount() {
        clearFocusOfCreateAccountFields()

        if (!viewModel.checkName(binding.createAccountDialog.createAccountInputNameEditText.text.toString())) {
            binding.createAccountDialog.createAccountInputName.error = resources.getString(R.string.invalid_account_name)
        } else if (!viewModel.checkEmail(binding.createAccountDialog.createAccountEditTextInputEmail1.text.toString())) {
            binding.createAccountDialog.createAccountTextInputEmail1.error = resources.getString(R.string.invalid_email)
        } else if (!viewModel.checkEmail(binding.createAccountDialog.createAccountEditTextInputEmail2.text.toString())) {
            binding.createAccountDialog.createAccountTextInputEmail2.error = resources.getString(R.string.invalid_email)
        } else if (!viewModel.checkEmail(binding.createAccountDialog.createAccountEditTextInputEmail3.text.toString())) {
            binding.createAccountDialog.createAccountTextInputEmail3.error = resources.getString(R.string.invalid_email)
        } else {
            progressStart()
            binding.createAccountDialog.root.visibility = View.GONE

            val changeAccountDto = ChangeAccountDto(
                binding.createAccountDialog.createAccountInputNameEditText.text.toString(),
                binding.createAccountDialog.createAccountEditTextInputEmail1.text.toString(),
                binding.createAccountDialog.createAccountEditTextInputEmail2.text.toString(),
                binding.createAccountDialog.createAccountEditTextInputEmail3.text.toString()
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
                            showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
                        }
                    }

                    viewModel.createAccountState.removeObservers(requireActivity())
                }
            }
        }
    }

    private fun updateAccount() {
        if ((viewModel.getAccountState.value?.account?.userId ?: 0) == viewModel.getUserState.value?.user?.userId) {
            clearFocusOfUpdateAccountFields()

            if (!viewModel.checkName(binding.accountDialog.accountDetailsInputNameEditText.text.toString())) {
                binding.accountDialog.accountDetailsInputName.error = resources.getString(R.string.invalid_account_name)
            } else if (!viewModel.checkEmail(binding.accountDialog.accountDetailsEditTextInputEmail1.text.toString())) {
                binding.accountDialog.accountDetailsTextInputEmail1.error = resources.getString(R.string.invalid_email)
            } else if (!viewModel.checkEmail(binding.accountDialog.accountDetailsEditTextInputEmail2.text.toString())) {
                binding.accountDialog.accountDetailsTextInputEmail2.error = resources.getString(R.string.invalid_email)
            } else if (!viewModel.checkEmail(binding.accountDialog.accountDetailsEditTextInputEmail3.text.toString())) {
                binding.accountDialog.accountDetailsTextInputEmail3.error = resources.getString(R.string.invalid_email)
            } else {
                progressStart()
                binding.accountDialog.root.visibility = View.GONE

                val changeAccountDto = ChangeAccountDto(
                    binding.accountDialog.accountDetailsInputNameEditText.text.toString(),
                    binding.accountDialog.accountDetailsEditTextInputEmail1.text.toString(),
                    binding.accountDialog.accountDetailsEditTextInputEmail2.text.toString(),
                    binding.accountDialog.accountDetailsEditTextInputEmail3.text.toString()
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
                                showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
                            }
                        }

                        viewModel.updateAccountState.removeObservers(requireActivity())
                    }
                }
            }
        } else {
            binding.accountDialog.root.visibility = View.GONE
        }
    }

    private fun deleteAccount() {
        hideDialogs()

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
                            showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
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
        binding.createAccountDialog.createAccountInputNameEditText.clearFocus()
        binding.createAccountDialog.createAccountEditTextInputEmail1.clearFocus()
        binding.createAccountDialog.createAccountEditTextInputEmail2.clearFocus()
        binding.createAccountDialog.createAccountEditTextInputEmail3.clearFocus()
    }

    /**
     * Clear focus of update account name and emails fields
     */
    private fun clearFocusOfUpdateAccountFields() {
        binding.accountDialog.accountDetailsInputNameEditText.clearFocus()
        binding.accountDialog.accountDetailsEditTextInputEmail1.clearFocus()
        binding.accountDialog.accountDetailsEditTextInputEmail2.clearFocus()
        binding.accountDialog.accountDetailsEditTextInputEmail3.clearFocus()
    }

    /**
     * Show progress indicator and make screen not touchable
     */
    private fun progressStart() {
        binding.root.isRefreshing = true
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    /**
     * Hide dialogs
     */
    private fun hideDialogs() {
        binding.accountsError.root.visibility = View.GONE
        binding.accountDialog.root.visibility = View.GONE
        binding.createAccountDialog.root.visibility = View.GONE
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