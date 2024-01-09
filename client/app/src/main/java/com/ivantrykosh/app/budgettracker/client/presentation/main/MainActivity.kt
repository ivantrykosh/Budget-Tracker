package com.ivantrykosh.app.budgettracker.client.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationView
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.databinding.ActivityMainBinding
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.overview.OverviewFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_main_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.mainNavView.setNavigationItemSelectedListener(this)

        onBackPressedDispatcher.addCallback(this) {
            exitOnBackPressed()
        }

        binding.mainNavView.menu.findItem(R.id.nav_overview).isChecked = true

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.overviewFragment -> setNavItemChecked(R.id.nav_overview)
                R.id.myProfileFragment -> setNavItemChecked(R.id.nav_my_profile)
                R.id.accountsFragment -> setNavItemChecked(R.id.nav_accounts)
                R.id.transactionsFragment -> setNavItemChecked(R.id.nav_transactions)
                R.id.categoryReportFragment -> setNavItemChecked(R.id.nav_category_report)
                R.id.timeReportFragment -> setNavItemChecked(R.id.nav_time_report)
                R.id.pdfReportFragment -> setNavItemChecked(R.id.nav_pdf_report)
                R.id.settingsFragment -> setNavItemChecked(R.id.nav_settings)
                else -> setNavItemChecked(R.id.nav_logout)
            }
        }
    }

    /**
     * Set navigation item checked
     */
    private fun setNavItemChecked(itemId: Int) {
        val navItem = binding.mainNavView.menu.findItem(itemId)
        if (!navItem.isChecked) {
            binding.mainNavView.menu.findItem(R.id.nav_overview).isChecked = false
            binding.mainNavView.menu.findItem(R.id.nav_my_profile).isChecked = false
            binding.mainNavView.menu.findItem(R.id.nav_accounts).isChecked = false
            binding.mainNavView.menu.findItem(R.id.nav_transactions).isChecked = false
            binding.mainNavView.menu.findItem(R.id.nav_category_report).isChecked = false
            binding.mainNavView.menu.findItem(R.id.nav_time_report).isChecked = false
            binding.mainNavView.menu.findItem(R.id.nav_pdf_report).isChecked = false
            binding.mainNavView.menu.findItem(R.id.nav_settings).isChecked = false
            binding.mainNavView.menu.findItem(R.id.nav_logout).isChecked = false
            navItem.isChecked = true
        }
    }

    /**
     * On exit back pressed. Check if it should navigate up or close app
     */
    private fun exitOnBackPressed() {
        val navHostFragment: NavHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_main_fragment) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments[0]
        if (currentFragment is OverviewFragment) {
            finishAffinity()
        } else {
            navController.popBackStack()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
                || super.onSupportNavigateUp()
    }

    /**
     * Open navigation drawer
     */
    fun openDrawer() {
        binding.mainDrawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                navController.navigateUp()
                navController.navigate(R.id.action_overviewFragment_to_myProfileFragment)
            }
            R.id.nav_overview -> {
                navController.navigateUp()
            }
            R.id.nav_accounts -> {
                navController.navigateUp()
                navController.navigate(R.id.action_overviewFragment_to_accountsFragment)
            }
            R.id.nav_transactions -> {
                navController.navigateUp()
                navController.navigate(R.id.action_overviewFragment_to_transactionsFragment)
            }
            R.id.nav_category_report -> {
                navController.navigateUp()
                navController.navigate(R.id.action_overviewFragment_to_categoryReportFragment)
            }
            R.id.nav_time_report -> {
                navController.navigateUp()
                navController.navigate(R.id.action_overviewFragment_to_timeReportFragment)
            }
            R.id.nav_pdf_report -> {
                navController.navigateUp()
                navController.navigate(R.id.action_overviewFragment_to_pdfReportFragment)
            }
            R.id.nav_settings -> {
                navController.navigateUp()
                navController.navigate(R.id.action_overviewFragment_to_settingsFragment)
            }
            R.id.nav_logout -> {
                logout()
            }
        }
        binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Log out
     */
    fun logout() {
        AppPreferences.jwtToken = null
        val intent = Intent(this, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}