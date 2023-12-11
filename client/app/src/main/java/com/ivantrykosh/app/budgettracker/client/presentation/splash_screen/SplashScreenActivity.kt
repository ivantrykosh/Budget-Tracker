package com.ivantrykosh.app.budgettracker.client.presentation.splash_screen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthActivity
import com.ivantrykosh.app.budgettracker.client.MainActivity
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.databinding.ActivitySplashScreenBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {

    private val viewModel by viewModels<SplashScreenViewModel>()
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPreferences.setup(applicationContext)

//        AppPreferences.jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ2YW55dHJ5a29jaDc4OUBnbWFpbC5jb20iLCJpYXQiOjE3MDE3MTQwNDcsImV4cCI6MTcwMjMxODg0N30.w6SWeKYH2L5lrJU1BZCK0kLwY3FYDINSthc3a92EeDw"
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
    }

    private fun checkToken(): Intent? {
        if (viewModel.state.value.token.isNotBlank()) {
            return Intent(this@SplashScreenActivity, MainActivity::class.java)
        }
        if (viewModel.state.value.error.startsWith("403") || viewModel.state.value.error == "Token is not found") {
            return Intent(this@SplashScreenActivity, AuthActivity::class.java)
        }
        return null
    }

    private fun setContent() {
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setOnRefreshListener {
            viewModel.refreshToken()

            viewModel.isLoading.observe(this@SplashScreenActivity) { isLoading ->
                if (!isLoading) {
                    checkToken()?.let {
                        it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        this@SplashScreenActivity.startActivity(it)
                    }
                    binding.root.isRefreshing = false
                }
            }
        }
    }
}