package com.ivantrykosh.app.budgettracker.client.presentation.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentConfirmEmailBinding

class ConfirmEmail : Fragment() {
    private var _binding: FragmentConfirmEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfirmEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // todo
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}