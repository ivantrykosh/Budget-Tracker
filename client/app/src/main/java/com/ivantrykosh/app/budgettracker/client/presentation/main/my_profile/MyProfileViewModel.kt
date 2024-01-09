package com.ivantrykosh.app.budgettracker.client.presentation.main.my_profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
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
import com.ivantrykosh.app.budgettracker.client.presentation.main.my_profile.state.GetUserState
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

    private val _getUserState = MutableLiveData(GetUserState())
    val getUserState: LiveData<GetUserState> = _getUserState

    private val _changePasswordState = MutableLiveData(ChangePasswordState())
    val changePasswordState: LiveData<ChangePasswordState> = _changePasswordState

    private val _resetPasswordState = MutableLiveData(ResetPasswordState())
    val resetPasswordState: LiveData<ResetPasswordState> = _resetPasswordState

    private val _deleteUserState = MutableLiveData(DeleteUserState())
    val deleteUserState: LiveData<DeleteUserState> = _deleteUserState

    /**
     * Check password
     */
    fun checkPassword(password: String): Boolean {
        return password.matches(Regex(PASSWORD_REGEX))
    }

    /**
     * Get user
     */
    fun getUser() {
        AppPreferences.jwtToken?.let { token ->
            getUser(token)
        } ?: run {
            _getUserState.value = GetUserState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Change password with old and new one
     *
     * @param oldPassword user old password
     * @param newPassword user new password
     */
    fun changePassword(oldPassword: String, newPassword: String) {
        AppPreferences.jwtToken?.let { token ->
            changePassword(token, oldPassword, newPassword)
        } ?: run {
            _changePasswordState.value = ChangePasswordState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Delete user with user password
     *
     * @param password user current password
     */
    fun deleteUser(password: String) {
        AppPreferences.jwtToken?.let { token ->
            deleteUser(token, password)
        } ?: run {
            _deleteUserState.value = DeleteUserState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Get user with their JWT
     *
     * @param token user JWT
     */
    private fun getUser(token: String) {
        _getUserState.value = GetUserState(isLoading = true)
        getUserUseCase(token).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _getUserState.value = GetUserState(user = result.data)
                }
                is Resource.Error -> {
                    _getUserState.value = GetUserState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                }
                is Resource.Loading -> {
                    _getUserState.value = GetUserState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Change password with user JWT, old and new password
     *
     * @param token user JWT
     * @param oldPassword user old password
     * @param newPassword user new password
     */
    private fun changePassword(token: String, oldPassword: String?, newPassword: String?) {
        _changePasswordState.value = ChangePasswordState(isLoading = true)
        if (oldPassword == null || !checkPassword(oldPassword)) {
            _changePasswordState.value = ChangePasswordState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (newPassword == null || !checkPassword(newPassword)) {
            _changePasswordState.value = ChangePasswordState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (_getUserState.value?.user == null) {
            _changePasswordState.value = ChangePasswordState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else {
            val changePasswordDto = ChangePasswordDto(
                oldPassword = hashPassword(oldPassword, _getUserState.value?.user?.email ?: ""),
                newPassword = hashPassword(newPassword, _getUserState.value?.user?.email ?: ""),
            )

            changePasswordUseCase(token, changePasswordDto).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _changePasswordState.value = ChangePasswordState()
                    }

                    is Resource.Error -> {
                        _changePasswordState.value = ChangePasswordState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                    }

                    is Resource.Loading -> {
                        _changePasswordState.value = ChangePasswordState(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Reset user password
     */
    fun resetPassword() {
        _resetPasswordState.value = ResetPasswordState(isLoading = true)
        if (_getUserState.value?.user == null) {
            _resetPasswordState.value = ResetPasswordState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else {
            val authDto = AuthDto(email = _getUserState.value!!.user!!.email, passwordHash = "")

            resetPasswordUseCase(authDto).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _resetPasswordState.value = ResetPasswordState()
                    }

                    is Resource.Error -> {
                        _resetPasswordState.value = ResetPasswordState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                    }

                    is Resource.Loading -> {
                        _resetPasswordState.value = ResetPasswordState(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Delete user using JWT and current password
     *
     * @param token user JWT
     * @param password user current password
     */
    private fun deleteUser(token: String, password: String) {
        _deleteUserState.value = DeleteUserState(isLoading = true)
        val authDto = AuthDto(
            _getUserState.value?.user?.email ?: "",
            hashPassword(password, _getUserState.value?.user?.email ?: ""),
        )

        deleteUserUseCase(token, authDto).onEach {result ->
            when (result) {
                is Resource.Success -> {
                    _deleteUserState.value = DeleteUserState()
                    AppPreferences.jwtToken = null
                }
                is Resource.Error -> {
                    _deleteUserState.value = DeleteUserState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                }
                is Resource.Loading -> {
                    _deleteUserState.value = DeleteUserState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Hash password with salt using SHA-256
     *
     * @param password password
     * @param salt salt
     */
    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val passwordWithSalt = (password + salt).toByteArray()
        return Base64.getEncoder().encodeToString(md.digest(passwordWithSalt))
    }
}