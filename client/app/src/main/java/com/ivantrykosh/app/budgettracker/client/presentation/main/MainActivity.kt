package com.ivantrykosh.app.budgettracker.client.presentation.main

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.databinding.ActivityMainBinding
import com.ivantrykosh.app.budgettracker.client.presentation.main.overview.OverviewFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_main_fragment) as NavHostFragment
        navController = navHostFragment.navController

        onBackPressedDispatcher.addCallback(this) {
            exitOnBackPressed()
        }
    }

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

    fun openDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.main_drawer_layout)
        drawerLayout.openDrawer(GravityCompat.START)
    }
}