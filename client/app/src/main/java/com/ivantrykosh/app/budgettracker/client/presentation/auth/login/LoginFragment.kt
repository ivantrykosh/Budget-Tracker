package com.ivantrykosh.app.budgettracker.client.presentation.auth.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentLoginBinding
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Login fragment
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val sharedAuthViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginEditTextInputEmail.setText(sharedAuthViewModel.getAuthDto()?.email)
        binding.loginEditTextInputPassword.setText(sharedAuthViewModel.getAuthDto()?.passwordHash)

        binding.loginButtonSignup.setOnClickListener {
            binding.loginNetworkError.root.visibility = View.GONE
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.loginButtonLogin.setOnClickListener {
            onLogin()
        }

        binding.loginTextForgotPassword.setOnClickListener {
            onForgotPassword()
        }

        binding.loginEditTextInputEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!sharedAuthViewModel.checkEmail(binding.loginEditTextInputEmail.text.toString())) {
                    binding.loginTextInputEmail.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.loginTextInputEmail.error = null
                }
                hideKeyboard(binding.loginTextInputEmail.windowToken)
            }
        }
        binding.loginEditTextInputPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!sharedAuthViewModel.checkPassword(binding.loginEditTextInputPassword.text.toString())) {
                    binding.loginTextInputPassword.error = resources.getString(R.string.invalid_password)
                } else {
                    binding.loginTextInputPassword.error = null
                }
                hideKeyboard(binding.loginTextInputPassword.windowToken)
            }
        }

        binding.loginNetworkError.errorOk.setOnClickListener {
            binding.loginNetworkError.root.visibility = View.GONE
        }

        binding.loginAnyProblems.setOnClickListener {
            showDefaultDialog(
                resources.getString(R.string.any_problems_question),
                resources.getString(R.string.any_problems_message),
                resources.getString(R.string.ok)
            )
        }
    }

    /**
     * On login click
     */
    private fun onLogin() {
        binding.loginNetworkError.root.visibility = View.GONE
        clearFocusOfFields()

        val email = binding.loginEditTextInputEmail.text?.toString() ?: ""
        val password = binding.loginEditTextInputPassword.text?.toString() ?: ""

        if (!sharedAuthViewModel.checkEmail(email)) {
            binding.loginTextInputEmail.error = resources.getString(R.string.invalid_email)
        } else if (!sharedAuthViewModel.checkPassword(password)) {
            binding.loginTextInputPassword.error = resources.getString(R.string.invalid_password)
        } else {
            progressStart()

            sharedAuthViewModel.setAuthDto(AuthDto(email, password))
            sharedAuthViewModel.login()
            sharedAuthViewModel.loginState.observe(requireActivity()) { loginState ->
                if (!loginState.isLoading) {
                    progressEnd()

                    if (loginState.token.isNotBlank()) {
                        requireActivity().startActivity(
                            Intent(requireActivity(), MainActivity::class.java)
                        )
                    } else if (loginState.error == Constants.ErrorStatusCodes.FORBIDDEN) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(resources.getString(R.string.confirm_email_error_title))
                            .setMessage(resources.getString(R.string.confirm_email_error_message_with_question))
                            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                                sendConfirmationEmail()
                            }
                            .setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
                            .show()
                    } else if (loginState.error == Constants.ErrorStatusCodes.UNAUTHORIZED) {
                        binding.loginTextInputEmail.error =
                            resources.getString(R.string.incorrect_email_password)
                        binding.loginTextInputPassword.error =
                            resources.getString(R.string.incorrect_email_password)
                    } else if (loginState.error == Constants.ErrorStatusCodes.NETWORK_ERROR){
                        showError(resources.getString(R.string.network_error), resources.getString(R.string.connection_failed_message))
                    } else {
                        showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
                    }

                    sharedAuthViewModel.loginState.removeObservers(requireActivity())
                }
            }
        }
    }

    /**
     * On forgot password click
     */
    private fun onForgotPassword() {
        binding.loginNetworkError.root.visibility = View.GONE
        clearFocusOfFields()
        binding.loginEditTextInputPassword.text = null
        binding.loginTextInputPassword.error = null

        val email = binding.loginEditTextInputEmail.text?.toString() ?: ""
        if (!sharedAuthViewModel.checkEmail(email)) {
            binding.loginTextInputEmail.error = resources.getString(R.string.invalid_email)
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.reset_password_question_title))
                .setMessage(resources.getString(R.string.reset_password_question_message))
                .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                    resetPassword()
                }
                .setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
                .show()
        }
    }

    /**
     * Reset password
     */
    private fun resetPassword() {
        progressStart()

        sharedAuthViewModel.setAuthDto(AuthDto(binding.loginEditTextInputEmail.text?.toString() ?: "",""))
        sharedAuthViewModel.resetPassword()
        sharedAuthViewModel.resetPasswordState.observe(requireActivity()) { resetPassword ->
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
                    Constants.ErrorStatusCodes.FORBIDDEN -> {
                        showDefaultDialog(
                            resources.getString(R.string.confirm_email_error_title),
                            resources.getString(R.string.confirm_email_error_message),
                            resources.getString(R.string.ok)
                        )
                    }
                    Constants.ErrorStatusCodes.UNAUTHORIZED -> {
                        binding.loginTextInputEmail.error = resources.getString(R.string.invalid_email)
                    }
                    Constants.ErrorStatusCodes.NETWORK_ERROR -> {
                        showError(resources.getString(R.string.network_error), resources.getString(R.string.connection_failed_message))
                    }
                    else -> {
                        showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
                    }
                }

                sharedAuthViewModel.resetPasswordState.removeObservers(requireActivity())
            }
        }
    }

    /**
     * Send confirmation email
     */
    private fun sendConfirmationEmail() {
        clearFocusOfFields()

        val email = binding.loginEditTextInputEmail.text?.toString() ?: ""
        val password = binding.loginEditTextInputPassword.text?.toString() ?: ""

        if (!sharedAuthViewModel.checkEmail(email)) {
            binding.loginTextInputEmail.error = resources.getString(R.string.invalid_email)
        } else if (!sharedAuthViewModel.checkPassword(password)) {
            binding.loginTextInputPassword.error = resources.getString(R.string.invalid_password)
        } else {
            progressStart()

            sharedAuthViewModel.setAuthDto(AuthDto(email, password))
            sharedAuthViewModel.sendConfirmationEmail()
            sharedAuthViewModel.confirmationEmailState.observe(requireActivity()) { confirmationEmail ->
                if (!confirmationEmail.isLoading) {
                    progressEnd()

                    when (confirmationEmail.error) {
                        null -> {
                            showDefaultDialog(
                                resources.getString(R.string.confirm_email_title),
                                resources.getString(R.string.confirm_email_message),
                                resources.getString(R.string.ok)
                            )
                        }
                        Constants.ErrorStatusCodes.UNAUTHORIZED -> {
                            binding.loginTextInputEmail.error = resources.getString(R.string.incorrect_email_password)
                            binding.loginButtonLogin.error = resources.getString(R.string.incorrect_email_password)
                        }
                        Constants.ErrorStatusCodes.BAD_REQUEST -> {
                            showDefaultDialog(
                                resources.getString(R.string.email_confirmed_title),
                                resources.getString(R.string.email_confirmed_message),
                                resources.getString(R.string.ok)
                            )
                        }
                        Constants.ErrorStatusCodes.NETWORK_ERROR -> {
                            showError(resources.getString(R.string.network_error), resources.getString(R.string.connection_failed_message))
                        }
                        else -> {
                            showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
                        }
                    }

                    sharedAuthViewModel.confirmationEmailState.removeObservers(requireActivity())
                }
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
     * Hide keyboard
     *
     * @param windowToken token of window
     */
    private fun hideKeyboard(windowToken: IBinder) {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    /**
     * Clear focus of login and password fields
     */
    private fun clearFocusOfFields() {
        binding.loginTextInputEmail.clearFocus()
        binding.loginTextInputPassword.clearFocus()
    }

    /**
     * Show progress indicator and make screen not touchable
     */
    private fun progressStart() {
        binding.loginCircularProgressIndicator.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    /**
     * Hide progress indicator and make screen touchable
     */
    private fun progressEnd() {
        binding.loginCircularProgressIndicator.visibility = View.GONE
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    /**
     * Show error message
     *
     * @param title title of message
     * @param text text of message
     */
    private fun showError(title: String, text: String) {
        binding.loginNetworkError.root.visibility = View.VISIBLE
        binding.loginNetworkError.errorTitle.text = title
        binding.loginNetworkError.errorText.text = text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}