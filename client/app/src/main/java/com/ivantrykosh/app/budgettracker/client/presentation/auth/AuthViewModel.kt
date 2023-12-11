package com.ivantrykosh.app.budgettracker.client.presentation.auth

import androidx.lifecycle.ViewModel
import com.ivantrykosh.app.budgettracker.client.domain.use_case.auth.sign_up.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
   private val signUpUseCase: SignUpUseCase
) : ViewModel() {

}