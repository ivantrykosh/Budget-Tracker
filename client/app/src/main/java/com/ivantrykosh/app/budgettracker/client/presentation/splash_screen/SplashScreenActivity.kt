package com.ivantrykosh.app.budgettracker.client.presentation.splash_screen

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import com.ivantrykosh.app.budgettracker.client.databinding.ActivitySplashScreenBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Splash screen activity
 */
@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {

    private val viewModel by viewModels<SplashScreenViewModel>()
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            installSplashScreen().apply {
                setKeepOnScreenCondition {
                    return@setKeepOnScreenCondition viewModel.refreshTokenState.value!!.isLoading
                }

                setOnExitAnimationListener {
                    exitSplashScreen()
                    it.remove()
                }
            }
        } else {
            viewModel.refreshTokenState.observe(this) { refreshToken ->
                if (!refreshToken.isLoading) {
                    viewModel.refreshTokenState.removeObservers(this)
                    exitSplashScreen()
                }
            }
        }
    }

    /**
     * Exit splash screen: check refreshing token and set content or start activity
     */
    private fun exitSplashScreen() {
        val intent = checkToken()
        if (intent == null) {
            setContent()
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }
    }

    /**
     * Check refreshing token and return intent.
     */
    private fun checkToken(): Intent? {
        return if (viewModel.refreshTokenState.value!!.token.isNotBlank()) {
            Intent(this@SplashScreenActivity, MainActivity::class.java)
        } else {
            when (viewModel.refreshTokenState.value!!.error) {
                Constants.ErrorStatusCodes.TOKEN_NOT_FOUND,
                Constants.ErrorStatusCodes.UNAUTHORIZED,
                Constants.ErrorStatusCodes.FORBIDDEN ->
                    Intent(this@SplashScreenActivity, AuthActivity::class.java)
                else -> null
            }
        }
    }

    /**
     * Set content of activity
     */
    private fun setContent() {
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.splashscreenError.errorOk.setOnClickListener {
            binding.splashscreenError.root.visibility = View.GONE
        }

        showErrorMessage()

        binding.root.setOnRefreshListener {
            refresh()
        }
    }

    /**
     * Refresh token method
     */
    private fun refresh() {
        binding.splashscreenError.root.visibility = View.GONE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.refreshToken()

        viewModel.refreshTokenState.observe(this) { refreshToken ->
            if (!refreshToken.isLoading) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                viewModel.refreshTokenState.removeObservers(this)

                val intent = checkToken()
                if (intent == null) {
                    showErrorMessage()
                } else {
                    binding.splashscreenError.root.visibility = View.GONE
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent)
                }
                binding.root.isRefreshing = false
            }
        }
    }

    /**
     * Show error message
     */
    private fun showErrorMessage() {
        binding.splashscreenError.root.visibility = View.VISIBLE
        binding.splashscreenError.errorTitle.text = resources.getString(R.string.network_error)
        binding.splashscreenError.errorText.text = resources.getString(R.string.connection_failed_message)
    }
}