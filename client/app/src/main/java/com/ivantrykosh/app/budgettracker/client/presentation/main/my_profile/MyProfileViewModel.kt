package com.ivantrykosh.app.budgettracker.client.presentation.main.my_profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.ChangePasswordDto
import com.ivantrykosh.app.budgettracker.client.domain.use_case.user.change_password.ChangePasswordUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.user.delete_user.DeleteUserUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.user.get_user.GetUserUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.user.reset_password.ResetPasswordUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.auth.reset_password.ResetPasswordState
import com.ivantrykosh.app.budgettracker.client.presentation.main.my_profile.state.ChangePasswordState
import com.ivantrykosh.app.budgettracker.client.presentation.main.my_profile.state.DeleteUserState
import com.ivantrykosh.app.budgettracker.client.presentation.main.my_profile.state.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject

/**
 * My profile view model
 */
@HiltViewModel
class MyProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
) : ViewModel() {

    companion object {
        private const val PASSWORD_REGEX =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,32}$"
    }

    private val _getUserState = mutableStateOf(UserState())
    val getUserState: State<UserState> = _getUserState
    private val _isLoadingGetUser = MutableLiveData(false)
    val isLoadingGetUser: LiveData<Boolean> = _isLoadingGetUser

    private val _changePasswordState = mutableStateOf(ChangePasswordState())
    val changePasswordState: State<ChangePasswordState> = _changePasswordState
    private val _isLoadingChangePassword = MutableLiveData(false)
    val isLoadingChangePassword: LiveData<Boolean> = _isLoadingChangePassword

    private val _resetPasswordState = mutableStateOf(ResetPasswordState())
    val resetPasswordState: State<ResetPasswordState> = _resetPasswordState
    private val _isLoadingResetPassword = MutableLiveData(false)
    val isLoadingResetPassword: LiveData<Boolean> = _isLoadingResetPassword

    private val _deleteUserState = mutableStateOf(DeleteUserState())
    val deleteUserState: State<DeleteUserState> = _deleteUserState
    private val _isLoadingDeleteUser = MutableLiveData(false)
    val isLoadingDeleteUser: LiveData<Boolean> = _isLoadingDeleteUser

    fun checkPassword(password: String): Boolean {
        return password.matches(Regex(PASSWORD_REGEX))
    }

    fun getUser() {
        AppPreferences.jwtToken?.let { token ->
            getUser(token)
        } ?: run {
            _getUserState.value = UserState(error = "No JWT token found")
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        AppPreferences.jwtToken?.let { token ->
            changePassword(token, oldPassword, newPassword)
        } ?: run {
            _changePasswordState.value = ChangePasswordState(error = "No JWT token found")
        }
    }

    fun deleteUser(password: String) {
        AppPreferences.jwtToken?.let { token ->
            deleteUser(token, password)
        } ?: run {
            _deleteUserState.value = DeleteUserState(error = "No JWT token found")
        }
    }

    private fun getUser(token: String) {
        _isLoadingGetUser.value = true
        getUserUseCase(token).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _getUserState.value = UserState(user = result.data)
                    _isLoadingGetUser.value = false
                }
                is Resource.Error -> {
                    _getUserState.value = UserState(error = result.message ?: "An unexpected error occurred")
                    _isLoadingGetUser.value = false
                }
                is Resource.Loading -> {
                    _getUserState.value = UserState(isLoading = true)
                    _isLoadingGetUser.value = true
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun changePassword(token: String, oldPassword: String?, newPassword: String?) {
        _isLoadingChangePassword.value = true
        if (oldPassword == null || !checkPassword(oldPassword)) {
            _changePasswordState.value = ChangePasswordState(error = "Invalid password")
            _isLoadingChangePassword.value = false
        } else if (newPassword == null || !checkPassword(newPassword)) {
            _changePasswordState.value = ChangePasswordState(error = "Invalid new password")
            _isLoadingChangePassword.value = false
        } else if (_getUserState.value.user == null) {
            _changePasswordState.value = ChangePasswordState(error = "No user")
                _isLoadingChangePassword.value = false
        } else {
            val changePasswordDto = ChangePasswordDto(
                oldPassword = hashPassword(oldPassword, _getUserState.value.user?.email ?: ""),
                newPassword = hashPassword(newPassword, _getUserState.value.user?.email ?: ""),
            )

            changePasswordUseCase(token, changePasswordDto).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _changePasswordState.value = ChangePasswordState()
                        _isLoadingChangePassword.value = false
                    }

                    is Resource.Error -> {
                        _changePasswordState.value = ChangePasswordState(
                            error = result.message ?: "An unexpected error occurred"
                        )
                        _isLoadingChangePassword.value = false
                    }

                    is Resource.Loading -> {
                        _changePasswordState.value = ChangePasswordState(isLoading = true)
                        _isLoadingChangePassword.value = true
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun resetPassword() {
        _isLoadingResetPassword.value = true
        if (_getUserState.value.user == null) {
            _resetPasswordState.value = ResetPasswordState(error = "HTTP Invalid email")
            _isLoadingResetPassword.value = false
        } else {
            val authDto = AuthDto(
                email = _getUserState.value.user!!.email,
                passwordHash = ""
            )
            resetPasswordUseCase(authDto).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _resetPasswordState.value = ResetPasswordState()
                        _isLoadingResetPassword.value = false
                    }

                    is Resource.Error -> {
                        _resetPasswordState.value = ResetPasswordState(error = result.message ?: "An unexpected error occurred")
                        _isLoadingResetPassword.value = false
                    }

                    is Resource.Loading -> {
                        _resetPasswordState.value = ResetPasswordState(isLoading = true)
                        _isLoadingResetPassword.value = true
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun deleteUser(token: String, password: String) {
        _isLoadingDeleteUser.value = true
        val authDto = AuthDto(
            _getUserState.value.user?.email ?: "",
            hashPassword(password, _getUserState.value.user?.email ?: ""),
        )
        deleteUserUseCase(token, authDto).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _deleteUserState.value = DeleteUserState()
                    _isLoadingDeleteUser.value = false
                    AppPreferences.jwtToken = null
                }
                is Resource.Error -> {
                    _deleteUserState.value = DeleteUserState(error = result.message ?: "An unexpected error occurred")
                    _isLoadingDeleteUser.value = false
                }
                is Resource.Loading -> {
                    _deleteUserState.value = DeleteUserState(isLoading = true)
                    _isLoadingDeleteUser.value = true
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val passwordWithSalt = (password + salt).toByteArray()
        return Base64.getEncoder().encodeToString(md.digest(passwordWithSalt))
    }
}