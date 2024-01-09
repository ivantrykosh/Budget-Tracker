package com.ivantrykosh.app.budgettracker.client.presentation.main.my_profile

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentMyProfileBinding
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * My profile fragment
 */
@AndroidEntryPoint
class MyProfileFragment : Fragment() {

    private var _binding: FragmentMyProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUser()

        binding.root.setOnRefreshListener {
            getUser()
        }

        binding.myProfileTopAppBar.setNavigationOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        // Change password
        binding.myProfileChangePasswordMain.setOnClickListener {
            onChangePassword()
        }
        binding.myProfileChangePasswordIcon.setOnClickListener {
            onChangePassword()
        }
        binding.myProfileChangePasswordText.setOnClickListener {
            onChangePassword()
        }

        // Reset password
        binding.myProfileResetPasswordMain.setOnClickListener {
            onResetPassword()
        }
        binding.myProfileResetPasswordIcon.setOnClickListener {
            onResetPassword()
        }
        binding.myProfileResetPasswordText.setOnClickListener {
            onResetPassword()
        }

        // Delete all data
        binding.myProfileDeleteAllDataMain.setOnClickListener {
            onDeleteAll()
        }
        binding.myProfileDeleteAllDataIcon.setOnClickListener {
            onDeleteAll()
        }
        binding.myProfileDeleteAllDataText.setOnClickListener {
            onDeleteAll()
        }

        // Logout
        binding.myProfileLogOutMain.setOnClickListener {
            (activity as MainActivity).logout()
        }
        binding.myProfileLogOutIcon.setOnClickListener {
            (activity as MainActivity).logout()
        }
        binding.myProfileLogOutText.setOnClickListener {
            (activity as MainActivity).logout()
        }

        binding.myProfileError.errorOk.setOnClickListener {
            binding.myProfileError.root.visibility = View.GONE
        }

        // Listeners for password dialog
        binding.myProfilePasswordDialog.passwordEditTextInputPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkPassword(binding.myProfilePasswordDialog.passwordEditTextInputPassword.text.toString())) {
                    binding.myProfilePasswordDialog.passwordTextInputPassword.error = resources.getString(R.string.invalid_password)
                } else {
                    binding.myProfilePasswordDialog.passwordTextInputPassword.error = null
                }
                hideKeyboard(binding.myProfilePasswordDialog.passwordTextInputPassword.windowToken)
            }
        }

        binding.myProfilePasswordDialog.passwordTextOk.setOnClickListener {
            binding.myProfilePasswordDialog.passwordEditTextInputPassword.clearFocus()

            if (!viewModel.checkPassword(binding.myProfilePasswordDialog.passwordEditTextInputPassword.text.toString())) {
                binding.myProfilePasswordDialog.passwordTextInputPassword.error = resources.getString(R.string.invalid_password)
            } else {
                deleteAll(binding.myProfilePasswordDialog.passwordEditTextInputPassword.text.toString())
                binding.myProfilePasswordDialog.root.visibility = View.GONE
            }
        }

        binding.myProfilePasswordDialog.changePasswordTextCancel.setOnClickListener {
            binding.myProfilePasswordDialog.root.visibility = View.GONE
        }

        // Listeners for change password dialog
        binding.myProfileChangePasswordDialog.changePasswordEditTextInputPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkPassword(binding.myProfileChangePasswordDialog.changePasswordEditTextInputPassword.text.toString())) {
                    binding.myProfileChangePasswordDialog.changePasswordTextInputPassword.error = resources.getString(R.string.invalid_password)
                } else {
                    binding.myProfileChangePasswordDialog.changePasswordTextInputPassword.error = null
                }
                hideKeyboard(binding.myProfileChangePasswordDialog.changePasswordTextInputPassword.windowToken)
            }
        }

        binding.myProfileChangePasswordDialog.changePasswordEditTextInputNewPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkPassword(binding.myProfileChangePasswordDialog.changePasswordEditTextInputNewPassword.text.toString())) {
                    binding.myProfileChangePasswordDialog.changePasswordTextInputNewPassword.error = resources.getString(R.string.invalid_password)
                } else {
                    binding.myProfileChangePasswordDialog.changePasswordTextInputNewPassword.error = null
                }
                hideKeyboard(binding.myProfileChangePasswordDialog.changePasswordTextInputNewPassword.windowToken)
            }
        }

        binding.myProfileChangePasswordDialog.changePasswordTextOk.setOnClickListener {
            binding.myProfileChangePasswordDialog.changePasswordEditTextInputPassword.clearFocus()
            binding.myProfileChangePasswordDialog.changePasswordEditTextInputNewPassword.clearFocus()

            if (!viewModel.checkPassword(binding.myProfileChangePasswordDialog.changePasswordEditTextInputPassword.text.toString())) {
                binding.myProfileChangePasswordDialog.changePasswordTextInputPassword.error = resources.getString(R.string.invalid_password)
            } else if (!viewModel.checkPassword(binding.myProfileChangePasswordDialog.changePasswordEditTextInputNewPassword.text.toString())) {
                binding.myProfileChangePasswordDialog.changePasswordTextInputNewPassword.error = resources.getString(R.string.invalid_password)
            } else {
                changePassword(binding.myProfileChangePasswordDialog.changePasswordEditTextInputPassword.text.toString(), binding.myProfileChangePasswordDialog.changePasswordEditTextInputNewPassword.text.toString())
                binding.myProfileChangePasswordDialog.root.visibility = View.GONE
            }
        }

        binding.myProfileChangePasswordDialog.changePasswordTextCancel.setOnClickListener {
            binding.myProfileChangePasswordDialog.root.visibility = View.GONE
        }
    }

    /**
     * Get user
     */
    private fun getUser() {
        binding.myProfileChangePasswordDialog.root.visibility = View.GONE
        binding.myProfileError.root.visibility = View.GONE
        progressStart()

        viewModel.getUser()
        viewModel.getUserState.observe(requireActivity()) { getUser ->
            if (!getUser.isLoading) {
                progressEnd()

                when (getUser.error) {
                    null -> {
                        binding.myProfileEmailEmail.text = getUser.user?.email ?: resources.getString(R.string.no_email)
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

                viewModel.getUserState.removeObservers(requireActivity())
            }
        }
    }

    /**
     * On change password click
     */
    private fun onChangePassword() {
        binding.myProfileError.root.visibility = View.GONE
        binding.myProfileChangePasswordDialog.root.visibility = View.VISIBLE

        binding.myProfileChangePasswordDialog.changePasswordTextInputPassword.error = null
        binding.myProfileChangePasswordDialog.changePasswordTextInputNewPassword.error = null
        binding.myProfileChangePasswordDialog.changePasswordEditTextInputPassword.text = null
        binding.myProfileChangePasswordDialog.changePasswordEditTextInputNewPassword.text = null
    }

    /**
     * Change password with current and new password
     *
     * @param password current password
     * @param newPassword new password
     */
    private fun changePassword(password: String, newPassword: String) {
        if (!viewModel.checkPassword(password)) {
            showError(resources.getString(R.string.error), resources.getString(R.string.invalid_password))
        } else if (!viewModel.checkPassword(newPassword)) {
            showError(resources.getString(R.string.error), resources.getString(R.string.invalid_new_password))
        } else {
            binding.myProfileError.root.visibility = View.GONE
            progressStart()

            viewModel.changePassword(password, newPassword)
            viewModel.changePasswordState.observe(requireActivity()) { changePassword ->
                if (!changePassword.isLoading) {
                    progressEnd()

                    when (changePassword.error) {
                        null -> {
                            showDefaultDialog(
                                resources.getString(R.string.password_was_changed),
                                resources.getString(R.string.password_was_changed),
                                resources.getString(R.string.ok)
                            )
                        }
                        Constants.ErrorStatusCodes.BAD_REQUEST -> {
                            showError(resources.getString(R.string.error), resources.getString(R.string.incorrect_password))
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

                    viewModel.changePasswordState.removeObservers(requireActivity())
                }
            }
        }
    }

    /**
     * On reset password click
     */
    private fun onResetPassword() {
        binding.myProfileError.root.visibility = View.GONE

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.reset_password_question_title))
            .setMessage(resources.getString(R.string.reset_password_question_message))
            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                resetPassword()
            }
            .setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
            .show()
    }

    /**
     * Reset password
     */
    private fun resetPassword() {
        binding.myProfileError.root.visibility = View.GONE
        progressStart()

        viewModel.resetPassword()
        viewModel.resetPasswordState.observe(requireActivity()) { resetPassword ->
            if (!resetPassword.isLoading) {
                progressEnd()

                when (resetPassword.error) {
                    null -> {
                        showDefaultDialog(
                            resources.getString(R.string.reset_password_title),
                            resources.getString(R.string.reset_password_message),
                            resources.getString(R.string.ok)
                        )
                    }
                    Constants.ErrorStatusCodes.UNAUTHORIZED -> {
                        showError(resources.getString(R.string.error), resources.getString(R.string.invalid_email))
                    }
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

                viewModel.resetPasswordState.removeObservers(requireActivity())
            }
        }
    }

    /**
     * On delete all click
     */
    private fun onDeleteAll() {
        binding.myProfileError.root.visibility = View.GONE

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.delete_all_data_question_title))
            .setMessage(resources.getString(R.string.delete_all_data_question_message))
            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->

                binding.myProfileError.root.visibility = View.GONE
                binding.myProfilePasswordDialog.root.visibility = View.VISIBLE

                binding.myProfilePasswordDialog.passwordTextInputPassword.error = null
                binding.myProfilePasswordDialog.passwordEditTextInputPassword.text = null
            }
            .setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
            .show()
    }

    /**
     * Delete all data with password
     *
     * @param password user current password
     */
    private fun deleteAll(password: String) {
        binding.myProfileError.root.visibility = View.GONE
        progressStart()

        viewModel.deleteUser(password)
        viewModel.deleteUserState.observe(requireActivity()) { deleteUser ->
            if (!deleteUser.isLoading) {
                progressEnd()

                when (deleteUser.error) {
                    null -> {
                        Toast.makeText(requireContext(), resources.getString(R.string.all_data_was_deleted), Toast.LENGTH_LONG).show()
                        startAuthActivity()
                    }
                    Constants.ErrorStatusCodes.UNAUTHORIZED -> {
                        showError(resources.getString(R.string.error), resources.getString(R.string.incorrect_password))
                    }
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

                viewModel.deleteUserState.removeObservers(requireActivity())
            }
        }
    }


    /**
     * Show default dialog
     *
     * @param title title of dialog
     * @param message message of dialog
     * @param posButton name of positive button
     */
    private fun showDefaultDialog(title: String, message: String, posButton: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(posButton) { _, _ -> }
            .show()
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
        binding.myProfileError.root.visibility = View.VISIBLE
        binding.myProfileError.errorTitle.text = title
        binding.myProfileError.errorText.text = text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}