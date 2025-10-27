package com.example.acefx_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.acefx_app.databinding.ActivityDashboardBinding
import com.example.acefx_app.ui.HomeFragment

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private var isBottomNavVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup Bottom Navigation
        binding.bottomNavigation.setupWithNavController(navController)
        binding.bottomNavigation.selectedItemId = R.id.homeFragment

        // Handle visibility based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment,
                R.id.accountFragment,
                R.id.projectsFragment -> showBottomNav()

                else -> hideBottomNav()
            }
        }
    }

    private fun showBottomNav() {
        if (!isBottomNavVisible) {
            binding.bottomNavigation.animate()
                .translationY(0f)
                .setDuration(250)
                .withStartAction { binding.bottomNavigation.visibility = View.VISIBLE }
                .start()
            isBottomNavVisible = true
        }
    }

    private fun hideBottomNav() {
        if (isBottomNavVisible) {
            binding.bottomNavigation.animate()
                .translationY(binding.bottomNavigation.height.toFloat())
                .setDuration(250)
                .withEndAction { binding.bottomNavigation.visibility = View.GONE }
                .start()
            isBottomNavVisible = false
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()

        if (currentFragment is HomeFragment) {
            finish()
        } else {
            super.onBackPressed()
        }
    }
}
