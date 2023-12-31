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
                    return@setKeepOnScreenCondition viewModel.state.value.isLoading
                }

                setOnExitAnimationListener {
                    val intent = checkToken()
                    if (intent == null) {
                        setContent()
                    } else {
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        this@SplashScreenActivity.startActivity(intent)
                    }
                    it.remove()
                }
            }
        } else {
            viewModel.isLoading.observe(this@SplashScreenActivity) { isLoading ->
                if (!isLoading) {
                    val intent = checkToken()
                    if (intent == null) {
                        setContent()
                    } else {
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        this@SplashScreenActivity.startActivity(intent)
                    }
                }
            }
        }
    }

    private fun checkToken(): Intent? {
        if (viewModel.state.value.token.isNotBlank()) {
            return Intent(this@SplashScreenActivity, MainActivity::class.java)
        }
        if (viewModel.state.value.error.startsWith("403") || viewModel.state.value.error.startsWith("401") || viewModel.state.value.error == "Token is not found") {
            return Intent(this@SplashScreenActivity, AuthActivity::class.java)
        }
        return null
    }

    private fun setContent() {
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.splashscreenError.errorOk.setOnClickListener {
            binding.splashscreenError.root.visibility = View.GONE
        }

        binding.splashscreenError.root.visibility = View.VISIBLE
        binding.splashscreenError.errorTitle.text = resources.getString(R.string.network_error)
        binding.splashscreenError.errorText.text = resources.getString(R.string.connection_failed_message)

        binding.root.setOnRefreshListener {
            binding.splashscreenError.root.visibility = View.GONE
            this.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            viewModel.refreshToken()

            viewModel.isLoading.observe(this@SplashScreenActivity) { isLoading ->
                if (!isLoading) {
                    this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    val intent = checkToken()
                    if (intent == null) {
                        binding.splashscreenError.root.visibility = View.VISIBLE
                        binding.splashscreenError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.splashscreenError.errorText.text = resources.getString(R.string.connection_failed_message)
                    } else {
                        binding.splashscreenError.root.visibility = View.GONE
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        this@SplashScreenActivity.startActivity(intent)
                    }
                    binding.root.isRefreshing = false
                }
            }
        }
    }
}