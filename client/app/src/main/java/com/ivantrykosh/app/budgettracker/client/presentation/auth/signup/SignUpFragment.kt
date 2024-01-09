package com.ivantrykosh.app.budgettracker.client.presentation.auth.signup

import android.content.Context
import android.os.Bundle
import android.os.IBinder
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
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentSignupBinding
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Sign up fragment
 */
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
            onSignUp()
        }

        binding.signupEditTextInputEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!sharedAuthViewModel.checkEmail(binding.signupEditTextInputEmail.text.toString())) {
                    binding.signupTextInputEmail.error = resources.getString(R.string.invalid_email)
                } else {
                    binding.signupTextInputEmail.error = null
                }
                hideKeyboard(binding.signupTextInputEmail.windowToken)
            }
        }
        binding.signupEditTextInputPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!sharedAuthViewModel.checkPassword(binding.signupEditTextInputPassword.text.toString())) {
                    binding.signupTextInputPassword.error = resources.getString(R.string.invalid_password)
                } else {
                    binding.signupTextInputPassword.error = null
                }
                hideKeyboard(binding.signupTextInputPassword.windowToken)
            }
        }

        binding.signupNetworkError.errorOk.setOnClickListener {
            binding.signupNetworkError.root.visibility = View.GONE
        }

        binding.signupAnyProblems.setOnClickListener {
            showDefaultDialog(
                resources.getString(R.string.any_problems_question),
                resources.getString(R.string.any_problems_message),
                resources.getString(R.string.ok)
            )
        }
    }

    /**
     * On sign up clicked
     */
    private fun onSignUp() {
        binding.signupNetworkError.root.visibility = View.GONE
        clearFocusOfFields()

        val email = binding.signupEditTextInputEmail.text?.toString() ?: ""
        val password = binding.signupEditTextInputPassword.text?.toString() ?: ""

        if (!sharedAuthViewModel.checkEmail(email)) {
            binding.signupTextInputEmail.error = resources.getString(R.string.invalid_email)
        } else if (!sharedAuthViewModel.checkPassword(password)) {
            binding.signupTextInputPassword.error = resources.getString(R.string.invalid_password)
        } else {
            progressStart()

            sharedAuthViewModel.setAuthDto(AuthDto(email, password))
            sharedAuthViewModel.signUp()
            sharedAuthViewModel.signUpState.observe(requireActivity()) { signUp ->
                if (!signUp.isLoading) {
                    progressEnd()

                    when (signUp.error) {
                        null -> {
                            showDefaultDialog(
                                resources.getString(R.string.confirm_email_title),
                                resources.getString(R.string.confirm_email_message),
                                resources.getString(R.string.ok)
                            )
                            this@SignUpFragment.findNavController()
                                .navigate(R.id.action_signUpFragment_to_loginFragment)
                        }
                        Constants.ErrorStatusCodes.CONFLICT -> {
                            binding.signupTextInputEmail.error = resources.getString(R.string.email_is_used)
                        }
                        Constants.ErrorStatusCodes.NETWORK_ERROR -> {
                            showError(resources.getString(R.string.network_error), resources.getString(R.string.connection_failed_message))
                        }
                        else -> {
                            showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occured))
                        }
                    }

                    sharedAuthViewModel.signUpState.removeObservers(requireActivity())
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
        binding.signupTextInputEmail.clearFocus()
        binding.signupTextInputPassword.clearFocus()
    }

    /**
     * Show progress indicator and make screen not touchable
     */
    private fun progressStart() {
        binding.signupCircularProgressIndicator.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    /**
     * Hide progress indicator and make screen touchable
     */
    private fun progressEnd() {
        binding.signupCircularProgressIndicator.visibility = View.GONE
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    /**
     * Show error message
     *
     * @param title title of message
     * @param text text of message
     */
    private fun showError(title: String, text: String) {
        binding.signupNetworkError.root.visibility = View.VISIBLE
        binding.signupNetworkError.errorTitle.text = title
        binding.signupNetworkError.errorText.text = text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}