package com.ivantrykosh.app.budgettracker.client.presentation.auth

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
import com.ivantrykosh.app.budgettracker.client.MainActivity
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentLoginBinding
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
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.loginButtonLogin.setOnClickListener {
            onLogin()
        }

        binding.loginTextForgotPassword.setOnClickListener {
            // todo add error checks and shows
            sharedAuthViewModel.setAuthDto(
                AuthDto(
                    (binding.loginEditTextInputEmail.text ?: "").toString(),
                    ""
                )
            )
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.reset_password_question_title))
                .setMessage(resources.getString(R.string.reset_password_question_message))
                .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
//                                sharedAuthViewModel.resetPassword()
                    showDefaultDialog(resources.getString(R.string.reset_password_title), resources.getString(R.string.reset_password_message), resources.getString(R.string.ok))
                }
                .setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
                .show()
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
                        sharedAuthViewModel.sendConfirmationEmail()
                        showDefaultDialog(
                            resources.getString(R.string.confirm_email_title),
                            resources.getString(R.string.confirm_email_message),
                            resources.getString(R.string.ok)
                        )
                    } else if (sharedAuthViewModel.loginState.value.error.startsWith("401")) {
                        binding.loginTextInputEmail.error = resources.getString(R.string.incorrect_email_password)
                        binding.loginButtonLogin.error = resources.getString(R.string.incorrect_email_password)
                    } else {
                        showDefaultDialog(
                            resources.getString(R.string.connection_failed),
                            resources.getString(R.string.connection_failed_message),
                            resources.getString(R.string.ok)
                        )
                    }
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