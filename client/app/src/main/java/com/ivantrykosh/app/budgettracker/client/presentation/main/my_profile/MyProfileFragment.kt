package com.ivantrykosh.app.budgettracker.client.presentation.main.my_profile

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentMyProfileBinding
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

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

        binding.myProfileChangePasswordDialog.root.visibility = View.GONE
        getUser()

        binding.root.setOnRefreshListener {
            binding.myProfileChangePasswordDialog.root.visibility = View.GONE
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
    }

    private fun getUser() {
        binding.root.isRefreshing = true
        binding.myProfileError.root.visibility = View.GONE

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.getUser()
        viewModel.isLoadingGetUser.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.root.isRefreshing = false

                if (viewModel.getUserState.value.error.isBlank()) {
                    binding.myProfileEmailEmail.text = viewModel.getUserState.value.user?.email ?: resources.getString(R.string.no_email)
                } else {
                    if (viewModel.getUserState.value.error.contains("Email is not verified", ignoreCase = true) || viewModel.getUserState.value.error.startsWith("401") || viewModel.getUserState.value.error.contains("JWT", ignoreCase = true)) {
                        startAuthActivity()
                    } else if (viewModel.getUserState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.myProfileError.root.visibility = View.VISIBLE
                        binding.myProfileError.errorTitle.text = resources.getString(R.string.error)
                        binding.myProfileError.errorText.text = viewModel.getUserState.value.error
                    } else {
                        binding.myProfileError.root.visibility = View.VISIBLE
                        binding.myProfileError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.myProfileError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                }
                viewModel.isLoadingGetUser.removeObservers(requireActivity())
            }
        }
    }

    private fun onChangePassword() {
        binding.myProfileError.root.visibility = View.GONE
        binding.myProfileChangePasswordDialog.root.visibility = View.VISIBLE

        binding.myProfileChangePasswordDialog.changePasswordTextInputPassword.error = null
        binding.myProfileChangePasswordDialog.changePasswordTextInputNewPassword.error = null
        binding.myProfileChangePasswordDialog.changePasswordEditTextInputPassword.text = null
        binding.myProfileChangePasswordDialog.changePasswordEditTextInputNewPassword.text = null

        binding.myProfileChangePasswordDialog.changePasswordEditTextInputPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkPassword(binding.myProfileChangePasswordDialog.changePasswordEditTextInputPassword.text.toString())) {
                    binding.myProfileChangePasswordDialog.changePasswordTextInputPassword.error = resources.getString(R.string.invalid_password)
                } else {
                    binding.myProfileChangePasswordDialog.changePasswordTextInputPassword.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.myProfileChangePasswordDialog.changePasswordTextInputPassword.windowToken, 0)
            }
        }

        binding.myProfileChangePasswordDialog.changePasswordEditTextInputNewPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!viewModel.checkPassword(binding.myProfileChangePasswordDialog.changePasswordEditTextInputNewPassword.text.toString())) {
                    binding.myProfileChangePasswordDialog.changePasswordTextInputNewPassword.error = resources.getString(R.string.invalid_password)
                } else {
                    binding.myProfileChangePasswordDialog.changePasswordTextInputNewPassword.error = null
                }
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.myProfileChangePasswordDialog.changePasswordTextInputNewPassword.windowToken, 0)
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

    private fun changePassword(password: String, newPassword: String) {
        if (!viewModel.checkPassword(password)) {
            binding.myProfileError.root.visibility = View.VISIBLE
            binding.myProfileError.errorTitle.text = resources.getString(R.string.error)
            binding.myProfileError.errorText.text = resources.getString(R.string.invalid_password)
        } else if (!viewModel.checkPassword(newPassword)) {
            binding.myProfileError.root.visibility = View.VISIBLE
            binding.myProfileError.errorTitle.text = resources.getString(R.string.error)
            binding.myProfileError.errorText.text = resources.getString(R.string.invalid_new_password)
        } else {
            binding.root.isRefreshing = true
            binding.myProfileError.root.visibility = View.GONE

            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            viewModel.changePassword(password, newPassword)
            viewModel.isLoadingChangePassword.observe(requireActivity()) { isLoading ->
                if (!isLoading) {
                    binding.root.isRefreshing = false
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    if (viewModel.changePasswordState.value.error.isBlank()) {
                        showDefaultDialog(
                            resources.getString(R.string.password_was_changed),
                            resources.getString(R.string.password_was_changed),
                            resources.getString(R.string.ok)
                        )
                    } else if (viewModel.changePasswordState.value.error.startsWith("400")) {
                        binding.myProfileError.root.visibility = View.VISIBLE
                        binding.myProfileError.errorTitle.text = resources.getString(R.string.error)
                        binding.myProfileError.errorText.text = resources.getString(R.string.incorrect_password)
                    } else if (viewModel.changePasswordState.value.error.contains("Email is not verified", ignoreCase = true) || viewModel.changePasswordState.value.error.startsWith("401") || viewModel.changePasswordState.value.error.contains("JWT", ignoreCase = true)) {
                        startAuthActivity()
                    } else if (viewModel.changePasswordState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.myProfileError.root.visibility = View.VISIBLE
                        binding.myProfileError.errorTitle.text = resources.getString(R.string.error)
                        binding.myProfileError.errorText.text = viewModel.changePasswordState.value.error
                    } else {
                        binding.myProfileError.root.visibility = View.VISIBLE
                        binding.myProfileError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.myProfileError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                    viewModel.isLoadingChangePassword.removeObservers(requireActivity())
                }
            }
        }
    }

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

    private fun resetPassword() {
        binding.root.isRefreshing = true
        binding.myProfileError.root.visibility = View.GONE
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

        viewModel.resetPassword()
        viewModel.isLoadingResetPassword.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                binding.root.isRefreshing = false
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                if (viewModel.resetPasswordState.value.error.isBlank()) {
                    showDefaultDialog(
                        resources.getString(R.string.reset_password_title),
                        resources.getString(R.string.reset_password_message),
                        resources.getString(R.string.ok)
                    )
                } else if (viewModel.resetPasswordState.value.error.startsWith("403") || viewModel.resetPasswordState.value.error.contains("JWT", ignoreCase = true)) {
                    startAuthActivity()
                } else if (viewModel.resetPasswordState.value.error.startsWith("401")) {
                    binding.myProfileError.root.visibility = View.VISIBLE
                    binding.myProfileError.errorTitle.text = resources.getString(R.string.error)
                    binding.myProfileError.errorText.text = resources.getString(R.string.invalid_email)
                } else if (viewModel.resetPasswordState.value.error.contains(
                        "HTTP",
                        ignoreCase = true
                    )
                ) {
                    binding.myProfileError.root.visibility = View.VISIBLE
                    binding.myProfileError.errorTitle.text = resources.getString(R.string.error)
                    binding.myProfileError.errorText.text =
                        viewModel.resetPasswordState.value.error
                } else {
                    binding.myProfileError.root.visibility = View.VISIBLE
                    binding.myProfileError.errorTitle.text =
                        resources.getString(R.string.network_error)
                    binding.myProfileError.errorText.text =
                        resources.getString(R.string.connection_failed_message)
                }

                viewModel.isLoadingResetPassword.removeObservers(requireActivity())
            }
        }
    }

    private fun onDeleteAll() {
        binding.myProfileError.root.visibility = View.GONE

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.delete_all_data_question_title))
            .setMessage(resources.getString(R.string.delete_all_data_question_message))
            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                deleteAll()
            }
            .setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
            .show()
    }

    private fun deleteAll() {
        binding.root.isRefreshing = true
        binding.myProfileError.root.visibility = View.GONE

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.deleteUser()
        viewModel.isLoadingDeleteUser.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.root.isRefreshing = false

                if (viewModel.deleteUserState.value.error.isBlank()) {
                    Toast.makeText(requireContext(), resources.getString(R.string.all_data_was_deleted), Toast.LENGTH_LONG).show()
                    startAuthActivity()
                } else {
                    if (viewModel.deleteUserState.value.error.contains("Email is not verified", ignoreCase = true) || viewModel.deleteUserState.value.error.startsWith("401") || viewModel.deleteUserState.value.error.contains("JWT", ignoreCase = true)) {
                        startAuthActivity()
                    } else if (viewModel.deleteUserState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.myProfileError.root.visibility = View.VISIBLE
                        binding.myProfileError.errorTitle.text = resources.getString(R.string.error)
                        binding.myProfileError.errorText.text = viewModel.deleteUserState.value.error
                    } else {
                        binding.myProfileError.root.visibility = View.VISIBLE
                        binding.myProfileError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.myProfileError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                }
                viewModel.isLoadingGetUser.removeObservers(requireActivity())
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