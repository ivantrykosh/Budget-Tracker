package com.ivantrykosh.app.budgettracker.client.presentation.auth

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.databinding.ActivityAuthBinding
import com.ivantrykosh.app.budgettracker.client.presentation.auth.login.LoginFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Auth activity
 */
@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var navController: NavController

    private val viewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setAuthDto(null)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_auth_fragment) as NavHostFragment
        navController = navHostFragment.navController

        onBackPressedDispatcher.addCallback(this) {
            exitOnBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.setAuthDto(null)
    }

    private fun exitOnBackPressed() {
        val navHostFragment: NavHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_auth_fragment) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments[0]
        if (currentFragment is LoginFragment) {
            finishAffinity()
        } else {
            navController.popBackStack()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
                || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setAuthDto(null)
    }
}