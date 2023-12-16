package com.ivantrykosh.app.budgettracker.client.presentation.auth.signup

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentSignupBinding
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private val sharedAuthViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupButtonSignup.setOnClickListener {
            binding.signupNetworkError.root.visibility = View.GONE
            onSignUp()
        }

        binding.signupEditTextInputEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!sharedAuthViewModel.checkEmail(binding.signupEditTextInputEmail.text.toString())) {
                    binding.signupTextInputEmail.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.signupTextInputEmail.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.signupTextInputEmail.windowToken, 0)
            }
        }
        binding.signupEditTextInputPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!sharedAuthViewModel.checkPassword(binding.signupEditTextInputPassword.text.toString())) {
                    binding.signupTextInputPassword.error = resources.getString(R.string.invalid_password)
                } else {
                    binding.signupTextInputPassword.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.signupTextInputPassword.windowToken, 0)
            }
        }

        binding.signupNetworkError.errorOk.setOnClickListener {
            binding.signupNetworkError.root.visibility = View.GONE
        }
    }

    private fun onSignUp() {
        binding.signupTextInputEmail.clearFocus()
        binding.signupTextInputPassword.clearFocus()

        // todo add error checks and shows
        val email = binding.signupEditTextInputEmail.text?.toString() ?: ""
        val password = binding.signupEditTextInputPassword.text?.toString() ?: ""

        if (!sharedAuthViewModel.checkEmail(email)) {
            binding.signupTextInputEmail.error = resources.getString(R.string.invalid_email)
        } else if (!sharedAuthViewModel.checkPassword(password)) {
            binding.signupTextInputPassword.error = resources.getString(R.string.invalid_password)
        } else {
            binding.signupTextInputEmail.error = null
            binding.signupTextInputPassword.error = null

            binding.signupCircularProgressIndicator.visibility = View.VISIBLE

            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            sharedAuthViewModel.setAuthDto(AuthDto(email, password))
            sharedAuthViewModel.signUp()
            sharedAuthViewModel.isSingUpLoading.observe(requireActivity()) { isLoading ->
                if (!isLoading) {
                    binding.signupCircularProgressIndicator.visibility = View.GONE

                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    if (sharedAuthViewModel.signUpState.value.error == "") {
                        showDialog(
                            resources.getString(R.string.confirm_email_title),
                            resources.getString(R.string.confirm_email_message),
                            resources.getString(R.string.ok)
                        )
                        this@SignUpFragment.findNavController()
                            .navigate(R.id.action_signUpFragment_to_loginFragment)
                    } else if (sharedAuthViewModel.signUpState.value.error.startsWith("409")) {
                        binding.signupTextInputEmail.error = resources.getString(R.string.email_is_used)
                    } else if (sharedAuthViewModel.signUpState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.signupNetworkError.root.visibility = View.VISIBLE
                        binding.signupNetworkError.errorTitle.text = resources.getString(R.string.error)
                        binding.signupNetworkError.errorText.text = sharedAuthViewModel.signUpState.value.error
                    } else {
                        binding.signupNetworkError.root.visibility = View.VISIBLE
                        binding.signupNetworkError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.signupNetworkError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }

                    sharedAuthViewModel.isSingUpLoading.removeObservers(requireActivity())
                }
            }
        }
    }

    private fun showDialog(title: String, message: String, posButton: String) {
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