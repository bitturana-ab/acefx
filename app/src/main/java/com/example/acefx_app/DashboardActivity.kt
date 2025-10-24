package com.example.acefx_app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.acefx_app.databinding.ActivityDashboardBinding
import com.example.acefx_app.ui.ClientProfileFragment
import com.example.acefx_app.ui.HomeFragment
import com.example.acefx_app.ui.StaffProfileFragment

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        // Optional: set start destination selected
        binding.bottomNavigation.selectedItemId = R.id.homeFragment
    }

    // Disable default back stack behavior for BottomNavigationView
    @Deprecated("Deprecated in Java")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments.first()
        if (currentFragment is HomeFragment) {
            finish() // exit app from home
        } else {
            super.onBackPressed()
        }
    }
}

//if ((currentFragment is ClientProfileFragment) ||(currentFragment is StaffProfileFragment)) {
//    finish() // exit app from home
//} else {
//    super.onBackPressed()
//}