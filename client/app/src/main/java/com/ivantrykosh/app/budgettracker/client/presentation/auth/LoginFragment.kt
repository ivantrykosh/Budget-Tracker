package com.ivantrykosh.app.budgettracker.client.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.text.toUpperCase
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentLoginBinding
import java.util.Locale

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButtonSignup.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
        binding.loginButtonLogin.setOnClickListener {
//            findNavController().navigate(R.id.action_loginFragment_to_confirmEmail)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.confirm_email_title))
                .setMessage(resources.getString(R.string.confirm_email_message))
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ -> }
                .show()
        }
        binding.loginTextForgotPassword.setOnClickListener {
            // todo
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}