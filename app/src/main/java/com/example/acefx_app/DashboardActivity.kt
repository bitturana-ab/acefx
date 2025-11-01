package com.example.acefx_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.acefx_app.databinding.ActivityDashboardBinding
import com.example.acefx_app.ui.HomeFragment
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class DashboardActivity : AppCompatActivity(), PaymentResultWithDataListener {

    private lateinit var binding: ActivityDashboardBinding
    private var isBottomNavVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Navigation
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
        binding.bottomNavigation.selectedItemId = R.id.homeFragment

        // Manage Bottom Navigation visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment,
                R.id.accountFragment,
                R.id.clientInvoiceFragment,
                R.id.projectsFragment -> showBottomNav()
                else -> hideBottomNav()
            }
        }
    }

    /** Show Bottom Navigation with smooth animation */
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

    /** Hide Bottom Navigation smoothly */
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

    /** Forward Razorpay payment success callback to current Fragment */
    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()

        if (currentFragment is PaymentResultWithDataListener) {
            currentFragment.onPaymentSuccess(razorpayPaymentId, paymentData)
        }
    }

    /** Forward Razorpay payment failure callback to current Fragment */
    override fun onPaymentError(code: Int, description: String?, paymentData: PaymentData?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()

        if (currentFragment is PaymentResultWithDataListener) {
            currentFragment.onPaymentError(code, description, paymentData)
        }
    }

    /** Handle back navigation logic properly */
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
