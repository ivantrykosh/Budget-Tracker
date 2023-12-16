package com.ivantrykosh.app.budgettracker.client.presentation.auth.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentLoginBinding
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

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
            binding.loginNetworkError.root.visibility = View.GONE
            onLogin()
        }

        binding.loginTextForgotPassword.setOnClickListener {
            binding.loginNetworkError.root.visibility = View.GONE
            onForgotPassword()
        }

        binding.loginEditTextInputEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!sharedAuthViewModel.checkEmail(binding.loginEditTextInputEmail.text.toString())) {
                    binding.loginTextInputEmail.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.loginTextInputEmail.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.loginTextInputEmail.windowToken, 0)
            }
        }
        binding.loginEditTextInputPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!sharedAuthViewModel.checkPassword(binding.loginEditTextInputPassword.text.toString())) {
                    binding.loginTextInputPassword.error = resources.getString(R.string.invalid_password)
                } else {
                    binding.loginTextInputPassword.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.loginTextInputPassword.windowToken, 0)
            }
        }

        binding.loginNetworkError.errorOk.setOnClickListener {
            binding.loginNetworkError.root.visibility = View.GONE
        }
    }

    private fun onLogin() {
        binding.loginTextInputEmail.clearFocus()
        binding.loginTextInputPassword.clearFocus()

        // todo add error checks and shows
        val email = binding.loginEditTextInputEmail.text?.toString() ?: ""
        val password = binding.loginEditTextInputPassword.text?.toString() ?: ""

        if (!sharedAuthViewModel.checkEmail(email)) {
            binding.loginTextInputEmail.error = resources.getString(R.string.invalid_email)
        } else if (!sharedAuthViewModel.checkPassword(password)) {
            binding.loginTextInputPassword.error = resources.getString(R.string.invalid_password)
        } else {
            binding.loginTextInputEmail.error = null
            binding.loginButtonLogin.error = null

            binding.loginCircularProgressIndicator.visibility = View.VISIBLE

            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            sharedAuthViewModel.setAuthDto(AuthDto(email, password))
            sharedAuthViewModel.login()
            sharedAuthViewModel.isLoginLoading.observe(requireActivity()) { isLoading ->
                if (!isLoading) {
                    binding.loginCircularProgressIndicator.visibility = View.GONE

                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    if (sharedAuthViewModel.loginState.value.token.isNotBlank()) {
                        requireActivity().startActivity(
                            Intent(requireActivity(), MainActivity::class.java)
                        )
                    } else if (sharedAuthViewModel.loginState.value.error.startsWith("403")) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(resources.getString(R.string.confirm_email_error_title))
                            .setMessage(resources.getString(R.string.confirm_email_error_message_with_question))
                            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                                sendConfirmationEmail()
                            }
                            .setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
                            .show()

                    } else if (sharedAuthViewModel.loginState.value.error.startsWith("401")) {
                        binding.loginTextInputEmail.error =
                            resources.getString(R.string.incorrect_email_password)
                        binding.loginTextInputPassword.error =
                            resources.getString(R.string.incorrect_email_password)
                    } else if (sharedAuthViewModel.loginState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.loginNetworkError.root.visibility = View.VISIBLE
                        binding.loginNetworkError.errorTitle.text = resources.getString(R.string.error)
                        binding.loginNetworkError.errorText.text = sharedAuthViewModel.loginState.value.error
                    } else {
                        binding.loginNetworkError.root.visibility = View.VISIBLE
                        binding.loginNetworkError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.loginNetworkError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }

                    sharedAuthViewModel.isLoginLoading.removeObservers(requireActivity())
                }
            }

        }
    }

    private fun onForgotPassword() {
        binding.loginTextInputEmail.clearFocus()
        binding.loginEditTextInputPassword.text = null
        binding.loginTextInputPassword.error = null

        // todo add error checks and shows
        val email = binding.loginEditTextInputEmail.text?.toString() ?: ""
        if (!sharedAuthViewModel.checkEmail(email)) {
            binding.loginTextInputEmail.error = resources.getString(R.string.invalid_email)
        } else {
            binding.loginTextInputEmail.error = null
            binding.loginButtonLogin.error = null

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

    private fun resetPassword() {
        // todo add error checks and shows
        binding.loginCircularProgressIndicator.visibility = View.VISIBLE

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        sharedAuthViewModel.setAuthDto(AuthDto(binding.loginEditTextInputEmail.text?.toString() ?: "",""))
        sharedAuthViewModel.resetPassword()
        sharedAuthViewModel.isResetPasswordLoading.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                binding.loginCircularProgressIndicator.visibility = View.GONE

                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                if (sharedAuthViewModel.resetPasswordState.value.error.isBlank()) {
                    showDefaultDialog(
                        resources.getString(R.string.reset_password_title),
                        resources.getString(R.string.reset_password_message),
                        resources.getString(R.string.ok)
                    )
                } else if (sharedAuthViewModel.resetPasswordState.value.error.startsWith("403")) {
                    showDefaultDialog(
                        resources.getString(R.string.confirm_email_error_message_with_question),
                        resources.getString(R.string.confirm_email_message),
                        resources.getString(R.string.ok)
                    )
                } else if (sharedAuthViewModel.resetPasswordState.value.error.startsWith("401")) {
                    binding.loginTextInputEmail.error = resources.getString(R.string.invalid_email)
                } else if (sharedAuthViewModel.resetPasswordState.value.error.contains("HTTP", ignoreCase = true)) {
                    binding.loginNetworkError.root.visibility = View.VISIBLE
                    binding.loginNetworkError.errorTitle.text = resources.getString(R.string.error)
                    binding.loginNetworkError.errorText.text = sharedAuthViewModel.resetPasswordState.value.error
                } else {
                    binding.loginNetworkError.root.visibility = View.VISIBLE
                    binding.loginNetworkError.errorTitle.text = resources.getString(R.string.network_error)
                    binding.loginNetworkError.errorText.text = resources.getString(R.string.connection_failed_message)
                }

                sharedAuthViewModel.isResetPasswordLoading.removeObservers(requireActivity())
            }
        }
    }

    private fun sendConfirmationEmail() {
        binding.loginTextInputEmail.clearFocus()

        // todo add error checks and shows
        val email = binding.loginEditTextInputEmail.text?.toString() ?: ""
        val password = binding.loginEditTextInputPassword.text?.toString() ?: ""

        if (!sharedAuthViewModel.checkEmail(email)) {
            binding.loginTextInputEmail.error = resources.getString(R.string.invalid_email)
        } else if (!sharedAuthViewModel.checkPassword(password)) {
            binding.loginTextInputPassword.error = resources.getString(R.string.invalid_password)
        } else {
            binding.loginTextInputEmail.error = null
            binding.loginButtonLogin.error = null

            binding.loginCircularProgressIndicator.visibility = View.VISIBLE

            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            sharedAuthViewModel.setAuthDto(AuthDto(email, password))
            sharedAuthViewModel.sendConfirmationEmail()
            sharedAuthViewModel.isConfirmationEmailLoading.observe(requireActivity()) { isLoading ->
                if (!isLoading) {
                    binding.loginCircularProgressIndicator.visibility = View.GONE

                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    if (sharedAuthViewModel.confirmationEmailState.value.error.isBlank()) {
                        showDefaultDialog(
                            resources.getString(R.string.confirm_email_title),
                            resources.getString(R.string.confirm_email_message),
                            resources.getString(R.string.ok)
                        )
                    } else if (sharedAuthViewModel.confirmationEmailState.value.error.startsWith("401")) {
                        binding.loginTextInputEmail.error = resources.getString(R.string.incorrect_email_password)
                        binding.loginButtonLogin.error = resources.getString(R.string.incorrect_email_password)
                    } else if (sharedAuthViewModel.confirmationEmailState.value.error.startsWith("400")) {
                        showDefaultDialog(
                            resources.getString(R.string.email_confirmed_title),
                            resources.getString(R.string.email_confirmed_message),
                            resources.getString(R.string.ok)
                        )
                    } else if (sharedAuthViewModel.confirmationEmailState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.loginNetworkError.root.visibility = View.VISIBLE
                        binding.loginNetworkError.errorTitle.text = resources.getString(R.string.error)
                        binding.loginNetworkError.errorText.text = sharedAuthViewModel.confirmationEmailState.value.error
                    } else {
                        binding.loginNetworkError.root.visibility = View.VISIBLE
                        binding.loginNetworkError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.loginNetworkError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }

                    sharedAuthViewModel.isConfirmationEmailLoading.removeObservers(requireActivity())
                }
            }
        }
    }

    private fun showDefaultDialog(title: String, message: String, posButton: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(posButton) { _, _ -> }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}