package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
        binding.accountsDialog.root.visibility = View.GONE
        binding.createAccountDialog.root.visibility = View.GONE

        binding.accountsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.accountsRecyclerView.adapter = AccountItemAdapter(requireContext(), emptyList())

        refresh()

        binding.accountsTopAppBar.setNavigationOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        binding.accountsTopAppBar.setOnMenuItemClickListener { menuItem ->
            binding.accountsDialog.root.visibility = View.GONE
            when (menuItem.itemId) {
                R.id.accounts_refresh -> {
                    refresh()
                    true
                }
                else -> false
            }
        }

        binding.accountsButtonAddAccount.setOnClickListener {
            binding.accountsDialog.root.visibility = View.GONE
            binding.createAccountDialog.root.visibility = View.VISIBLE
            binding.createAccountDialog.createAccountInputNameEditText.text = null
            binding.createAccountDialog.createAccountEditTextInputEmail1.text = null
            binding.createAccountDialog.createAccountEditTextInputEmail2.text = null
            binding.createAccountDialog.createAccountEditTextInputEmail3.text = null
        }

        binding.accountsDialog.accountDetailsTextCancel.setOnClickListener {
            binding.accountsDialog.root.visibility = View.GONE
            refresh()
        }

        binding.createAccountDialog.createAccountTextCancel.setOnClickListener {
            binding.createAccountDialog.root.visibility = View.GONE
            refresh()
        }

        binding.createAccountDialog.createAccountTextOk.setOnClickListener {
            createAccount()
        }

        binding.accountsDialog.accountDetailsDeleteAccount.setOnClickListener {
            onDeleteAccount()
        }

        binding.accountsDialog.accountDetailsTextOk.setOnClickListener {
            updateAccount()
        }

        binding.accountsDialog.accountDetailsInputNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkName(binding.accountsDialog.accountDetailsInputNameEditText.text.toString())) {
                    binding.accountsDialog.accountDetailsInputName.error = resources.getString(R.string.invalid_account_name)
                } else {
                    binding.accountsDialog.accountDetailsInputName.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.accountsDialog.accountDetailsInputNameEditText.windowToken, 0)
            }
        }

        binding.accountsDialog.accountDetailsEditTextInputEmail1.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(binding.accountsDialog.accountDetailsEditTextInputEmail1.text.toString())) {
                    binding.accountsDialog.accountDetailsTextInputEmail1.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.accountsDialog.accountDetailsTextInputEmail1.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.accountsDialog.accountDetailsEditTextInputEmail1.windowToken, 0)
            }
        }
        binding.accountsDialog.accountDetailsEditTextInputEmail2.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(binding.accountsDialog.accountDetailsEditTextInputEmail2.text.toString())) {
                    binding.accountsDialog.accountDetailsTextInputEmail2.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.accountsDialog.accountDetailsTextInputEmail2.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.accountsDialog.accountDetailsEditTextInputEmail2.windowToken, 0)
            }
        }
        binding.accountsDialog.accountDetailsEditTextInputEmail3.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(binding.accountsDialog.accountDetailsEditTextInputEmail3.text.toString())) {
                    binding.accountsDialog.accountDetailsTextInputEmail3.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.accountsDialog.accountDetailsTextInputEmail3.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.accountsDialog.accountDetailsEditTextInputEmail3.windowToken, 0)
            }
        }

        binding.createAccountDialog.createAccountInputNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkName(binding.createAccountDialog.createAccountInputNameEditText.text.toString())) {
                    binding.createAccountDialog.createAccountInputName.error = resources.getString(R.string.invalid_account_name)
                } else {
                    binding.createAccountDialog.createAccountInputName.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.createAccountDialog.createAccountInputNameEditText.windowToken, 0)
            }
        }

        binding.createAccountDialog.createAccountEditTextInputEmail1.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(binding.createAccountDialog.createAccountEditTextInputEmail1.text.toString())) {
                    binding.createAccountDialog.createAccountTextInputEmail1.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.createAccountDialog.createAccountTextInputEmail1.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.createAccountDialog.createAccountEditTextInputEmail1.windowToken, 0)
            }
        }
        binding.createAccountDialog.createAccountEditTextInputEmail2.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(binding.createAccountDialog.createAccountEditTextInputEmail2.text.toString())) {
                    binding.createAccountDialog.createAccountTextInputEmail2.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.createAccountDialog.createAccountTextInputEmail2.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.createAccountDialog.createAccountEditTextInputEmail2.windowToken, 0)
            }
        }
        binding.createAccountDialog.createAccountEditTextInputEmail3.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkEmail(binding.createAccountDialog.createAccountEditTextInputEmail3.text.toString())) {
                    binding.createAccountDialog.createAccountTextInputEmail3.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.createAccountDialog.createAccountTextInputEmail3.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.createAccountDialog.createAccountEditTextInputEmail3.windowToken, 0)
            }
        }

        binding.accountsError.errorOk.setOnClickListener {
            binding.accountsError.root.visibility = View.GONE
        }
    }

    private fun refresh() {
        getUser()
    }

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

    private fun getUser() {
        binding.root.isRefreshing = true
        binding.accountsError.root.visibility = View.GONE
        binding.accountsDialog.root.visibility = View.GONE
        binding.createAccountDialog.root.visibility = View.GONE

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.getUser()
        viewModel.isLoadingGetUser.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.root.isRefreshing = false
                if (viewModel.getUserState.value.user != null) {
                    refreshAccounts()
                }
                else if (viewModel.getUserState.value.error.isNotBlank()) {
                    if (viewModel.getUserState.value.error.contains("Email is not verified", ignoreCase = true) || viewModel.getUserState.value.error.startsWith("401") || viewModel.getUserState.value.error.contains("JWT", ignoreCase = true)) {
                        startAuthActivity()
                    } else if (viewModel.getUserState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.accountsError.root.visibility = View.VISIBLE
                        binding.accountsError.errorTitle.text = resources.getString(R.string.error)
                        binding.accountsError.errorText.text = viewModel.getUserState.value.error
                    } else {
                        binding.accountsError.root.visibility = View.VISIBLE
                        binding.accountsError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.accountsError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                }
                viewModel.isLoadingGetUser.removeObservers(requireActivity())
            }
        }
    }

    private fun refreshAccounts() {
        binding.accountsError.root.visibility = View.GONE
        binding.accountsDialog.root.visibility = View.GONE
        binding.createAccountDialog.root.visibility = View.GONE
        binding.root.isRefreshing = true

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.getAccounts()
        viewModel.isLoadingGetAccounts.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.root.isRefreshing = false
                if (viewModel.getAccountsState.value.error.isBlank()) {
                    val adapter = AccountItemAdapter(
                        requireContext(),
                        viewModel.getAccountsState.value.accounts
                    )
                    adapter.setOnAccountClickListener(this)
                    binding.accountsRecyclerView.adapter = adapter
                    binding.accountsRecyclerView.setHasFixedSize(true)

                    if (viewModel.getAccountsState.value.accounts.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.you_do_not_have_any_accounts, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (viewModel.getAccountsState.value.error.startsWith("403") || viewModel.getAccountsState.value.error.startsWith("401") || viewModel.getAccountsState.value.error.contains("JWT", ignoreCase = true)) {
                        startAuthActivity()
                    } else if (viewModel.getAccountsState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.accountsError.root.visibility = View.VISIBLE
                        binding.accountsError.errorTitle.text = resources.getString(R.string.error)
                        binding.accountsError.errorText.text = viewModel.getAccountsState.value.error
                    } else {
                        binding.accountsError.root.visibility = View.VISIBLE
                        binding.accountsError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.accountsError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                }
                viewModel.isLoadingGetAccounts.removeObservers(requireActivity())
            }
        }
    }

    private fun getAccount(id: String) {
        binding.accountsError.root.visibility = View.GONE
        binding.accountsDialog.root.visibility = View.GONE
        binding.createAccountDialog.root.visibility = View.GONE
        binding.root.isRefreshing = true

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.getAccount(id)
        viewModel.isLoadingGetAccount.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.root.isRefreshing = false
                if (viewModel.getAccountState.value.error.isBlank()) {
                    if (viewModel.getAccountState.value.account == null) {
                        binding.accountsError.root.visibility = View.VISIBLE
                        binding.accountsError.errorTitle.text = resources.getString(R.string.error)
                        binding.accountsError.errorText.text = resources.getString(R.string.invalid_account_id)
                    } else {
                        binding.accountsDialog.root.visibility = View.VISIBLE
                        binding.accountsDialog.accountDetailsIncomesValue.text = getFormat().format(viewModel.getAccountState.value.account!!.incomesSum)
                        binding.accountsDialog.accountDetailsExpensesValue.text = getFormat().format(viewModel.getAccountState.value.account!!.expensesSum)
                        binding.accountsDialog.accountDetailsTotalValue.text = getFormat().format(viewModel.getAccountState.value.account!!.incomesSum.plus(viewModel.getAccountState.value.account!!.expensesSum))
                        binding.accountsDialog.accountDetailsInputNameEditText.setText(viewModel.getAccountState.value.account!!.name)
                        binding.accountsDialog.accountDetailsEmailsLayout.visibility = View.GONE
                        binding.accountsDialog.accountDetailsDeleteAccount.visibility = View.GONE
                        if (viewModel.getAccountState.value.account!!.userId == (viewModel.getUserState.value.user?.userId ?: -1)) {
                            binding.accountsDialog.accountDetailsEmailsLayout.visibility = View.VISIBLE
                            binding.accountsDialog.accountDetailsDeleteAccount.visibility = View.VISIBLE
                            binding.accountsDialog.accountDetailsInputNameEditText.isFocusableInTouchMode = true
                            binding.accountsDialog.accountDetailsInputNameEditText.isFocusable = true
                            binding.accountsDialog.accountDetailsTextInputEmail1.error = null
                            binding.accountsDialog.accountDetailsEditTextInputEmail1.setText(
                                viewModel.getAccountState.value.account!!.email2
                            )
                            binding.accountsDialog.accountDetailsTextInputEmail2.error = null
                            binding.accountsDialog.accountDetailsEditTextInputEmail2.setText(
                                viewModel.getAccountState.value.account!!.email3
                            )
                            binding.accountsDialog.accountDetailsTextInputEmail3.error = null
                            binding.accountsDialog.accountDetailsEditTextInputEmail3.setText(
                                viewModel.getAccountState.value.account!!.email4
                            )
                        } else {
                            binding.accountsDialog.accountDetailsInputNameEditText.isFocusable = false
                        }
                        binding.accountsDialog.root.requestLayout()
                    }
                } else {
                    if (viewModel.getAccountState.value.error.startsWith("403") || viewModel.getAccountState.value.error.startsWith("401") || viewModel.getAccountState.value.error.contains("JWT", ignoreCase = true)) {
                        startAuthActivity()
                    } else if (viewModel.getAccountState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.accountsError.root.visibility = View.VISIBLE
                        binding.accountsError.errorTitle.text = resources.getString(R.string.error)
                        binding.accountsError.errorText.text = viewModel.getAccountState.value.error
                    } else {
                        binding.accountsError.root.visibility = View.VISIBLE
                        binding.accountsError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.accountsError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                }
                viewModel.isLoadingGetAccount.removeObservers(requireActivity())
            }
        }
    }

    private fun createAccount() {
        binding.createAccountDialog.createAccountInputNameEditText.clearFocus()
        binding.createAccountDialog.createAccountEditTextInputEmail1.clearFocus()
        binding.createAccountDialog.createAccountEditTextInputEmail2.clearFocus()
        binding.createAccountDialog.createAccountEditTextInputEmail3.clearFocus()

        if (!viewModel.checkName(binding.createAccountDialog.createAccountInputNameEditText.text.toString())) {
            binding.createAccountDialog.createAccountInputName.error = resources.getString(R.string.invalid_account_name)
        } else if (!viewModel.checkEmail(binding.createAccountDialog.createAccountEditTextInputEmail1.text.toString())) {
            binding.createAccountDialog.createAccountTextInputEmail1.error = resources.getString(R.string.invalid_email)
        } else if (!viewModel.checkEmail(binding.createAccountDialog.createAccountEditTextInputEmail2.text.toString())) {
            binding.createAccountDialog.createAccountTextInputEmail2.error = resources.getString(R.string.invalid_email)
        } else if (!viewModel.checkEmail(binding.createAccountDialog.createAccountEditTextInputEmail3.text.toString())) {
            binding.createAccountDialog.createAccountTextInputEmail3.error = resources.getString(R.string.invalid_email)
        } else {
            binding.root.isRefreshing = true
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            binding.createAccountDialog.root.visibility = View.GONE

            val changeAccountDto = ChangeAccountDto(
                binding.createAccountDialog.createAccountInputNameEditText.text.toString(),
                binding.createAccountDialog.createAccountEditTextInputEmail1.text.toString(),
                binding.createAccountDialog.createAccountEditTextInputEmail2.text.toString(),
                binding.createAccountDialog.createAccountEditTextInputEmail3.text.toString()
            )
            viewModel.createAccount(changeAccountDto)
            viewModel.isLoadingCreateAccount.observe(requireActivity()) { isLoading ->
                if (!isLoading) {
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    binding.root.isRefreshing = false

                    if (viewModel.createAccountState.value.error.isBlank()) {
                        Toast.makeText(requireContext(), resources.getString(R.string.account_was_created), Toast.LENGTH_SHORT).show()
                        refresh()
                    } else {
                        if (viewModel.createAccountState.value.error.contains("Email is not verified", ignoreCase = true) || viewModel.createAccountState.value.error.startsWith("401") || viewModel.createAccountState.value.error.contains("JWT", ignoreCase = true)) {
                            startAuthActivity()
                        } else if (viewModel.createAccountState.value.error.startsWith("400")) {
                            binding.accountsError.root.visibility = View.VISIBLE
                            binding.accountsError.errorTitle.text = resources.getString(R.string.error)
                            binding.accountsError.errorText.text = resources.getString(R.string.invalid_account_data)
                        } else if (viewModel.createAccountState.value.error.startsWith("409")) {
                            binding.accountsError.root.visibility = View.VISIBLE
                            binding.accountsError.errorTitle.text = resources.getString(R.string.error)
                            binding.accountsError.errorText.text = resources.getString(R.string.invalid_user_email)
                        } else if (viewModel.createAccountState.value.error.contains("HTTP", ignoreCase = true)) {
                            binding.accountsError.root.visibility = View.VISIBLE
                            binding.accountsError.errorTitle.text = resources.getString(R.string.error)
                            binding.accountsError.errorText.text = viewModel.createAccountState.value.error
                        } else {
                            binding.accountsError.root.visibility = View.VISIBLE
                            binding.accountsError.errorTitle.text = resources.getString(R.string.network_error)
                            binding.accountsError.errorText.text = resources.getString(R.string.connection_failed_message)
                        }
                    }

                    viewModel.isLoadingCreateAccount.removeObservers(requireActivity())
                }
            }
        }
    }

    private fun updateAccount() {
        if ((viewModel.getAccountState.value.account?.userId ?: 0) == viewModel.getUserState.value.user?.userId) {
            binding.accountsDialog.accountDetailsInputNameEditText.clearFocus()
            binding.accountsDialog.accountDetailsEditTextInputEmail1.clearFocus()
            binding.accountsDialog.accountDetailsEditTextInputEmail2.clearFocus()
            binding.accountsDialog.accountDetailsEditTextInputEmail3.clearFocus()

            if (!viewModel.checkName(binding.accountsDialog.accountDetailsInputNameEditText.text.toString())) {
                binding.accountsDialog.accountDetailsInputName.error = resources.getString(R.string.invalid_account_name)
            } else if (!viewModel.checkEmail(binding.accountsDialog.accountDetailsEditTextInputEmail1.text.toString())) {
                binding.accountsDialog.accountDetailsTextInputEmail1.error = resources.getString(R.string.invalid_email)
            } else if (!viewModel.checkEmail(binding.accountsDialog.accountDetailsEditTextInputEmail2.text.toString())) {
                binding.accountsDialog.accountDetailsTextInputEmail2.error = resources.getString(R.string.invalid_email)
            } else if (!viewModel.checkEmail(binding.accountsDialog.accountDetailsEditTextInputEmail3.text.toString())) {
                binding.accountsDialog.accountDetailsTextInputEmail3.error = resources.getString(R.string.invalid_email)
            } else {
                binding.root.isRefreshing = true
                requireActivity().window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )

                binding.accountsDialog.root.visibility = View.GONE

                val changeAccountDto = ChangeAccountDto(
                    binding.accountsDialog.accountDetailsInputNameEditText.text.toString(),
                    binding.accountsDialog.accountDetailsEditTextInputEmail1.text.toString(),
                    binding.accountsDialog.accountDetailsEditTextInputEmail2.text.toString(),
                    binding.accountsDialog.accountDetailsEditTextInputEmail3.text.toString()
                )
                viewModel.updateAccount(viewModel.getAccountState.value.account?.accountId.toString(), changeAccountDto)
                viewModel.isLoadingUpdateAccount.observe(requireActivity()) { isLoading ->
                    if (!isLoading) {
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        binding.root.isRefreshing = false

                        if (viewModel.updateAccountState.value.error.isBlank()) {
                            Toast.makeText(
                                requireContext(),
                                resources.getString(R.string.account_was_updated),
                                Toast.LENGTH_SHORT
                            ).show()
                            refresh()
                        } else {
                            if (viewModel.updateAccountState.value.error.contains("Email is not verified", ignoreCase = true) || viewModel.updateAccountState.value.error.startsWith("401") || viewModel.updateAccountState.value.error.contains("JWT", ignoreCase = true)) {
                                startAuthActivity()
                            } else if (viewModel.updateAccountState.value.error.startsWith("400")) {
                                binding.accountsError.root.visibility = View.VISIBLE
                                binding.accountsError.errorTitle.text = resources.getString(R.string.error)
                                binding.accountsError.errorText.text = resources.getString(R.string.invalid_account_data)
                            } else if (viewModel.updateAccountState.value.error.startsWith("409")) {
                                binding.accountsError.root.visibility = View.VISIBLE
                                binding.accountsError.errorTitle.text = resources.getString(R.string.error)
                                binding.accountsError.errorText.text = resources.getString(R.string.invalid_user_email)
                            } else if (viewModel.updateAccountState.value.error.contains("HTTP", ignoreCase = true)) {
                                binding.accountsError.root.visibility = View.VISIBLE
                                binding.accountsError.errorTitle.text =
                                    resources.getString(R.string.error)
                                binding.accountsError.errorText.text =
                                    viewModel.updateAccountState.value.error
                            } else {
                                binding.accountsError.root.visibility = View.VISIBLE
                                binding.accountsError.errorTitle.text =
                                    resources.getString(R.string.network_error)
                                binding.accountsError.errorText.text =
                                    resources.getString(R.string.connection_failed_message)
                            }
                        }

                        viewModel.isLoadingUpdateAccount.removeObservers(requireActivity())
                    }
                }
            }
        } else {
            binding.accountsDialog.root.visibility = View.GONE
        }
    }

    private fun deleteAccount() {
        binding.accountsError.root.visibility = View.GONE
        binding.accountsDialog.root.visibility = View.GONE

        if (viewModel.getAccountState.value.account?.accountId == null) {
            binding.accountsError.root.visibility = View.VISIBLE
            binding.accountsError.errorTitle.text = resources.getString(R.string.error)
            binding.accountsError.errorText.text = resources.getString(R.string.invalid_account_id)
        } else {
            binding.root.isRefreshing = true
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            viewModel.deleteAccount(viewModel.getAccountState.value.account!!.accountId.toString())
            viewModel.isLoadingDeleteAccount.observe(requireActivity()) { isLoading ->
                if (!isLoading) {
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    binding.root.isRefreshing = false
                    if (viewModel.deleteAccountState.value.error.isBlank()) {
                        Toast.makeText(requireContext(), R.string.account_was_deleted, Toast.LENGTH_SHORT).show()
                        refresh()
                    } else {
                        if (viewModel.deleteAccountState.value.error.startsWith("403") || viewModel.deleteAccountState.value.error.startsWith("401") || viewModel.deleteAccountState.value.error.contains("JWT", ignoreCase = true)) {
                            startAuthActivity()
                        } else if (viewModel.deleteAccountState.value.error.contains("HTTP", ignoreCase = true)) {
                            binding.accountsError.root.visibility = View.VISIBLE
                            binding.accountsError.errorTitle.text =
                                resources.getString(R.string.error)
                            binding.accountsError.errorText.text =
                                viewModel.deleteAccountState.value.error
                        } else {
                            binding.accountsError.root.visibility = View.VISIBLE
                            binding.accountsError.errorTitle.text =
                                resources.getString(R.string.network_error)
                            binding.accountsError.errorText.text =
                                resources.getString(R.string.connection_failed_message)
                        }
                    }
                    viewModel.isLoadingDeleteAccount.removeObservers(requireActivity())
                }
            }
        }
    }

    override fun onAccountClick(account: Account) {
        getAccount(account.accountId.toString())
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