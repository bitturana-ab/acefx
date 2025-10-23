package com.example.acefx_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.acefx_app.databinding.ActivityDashboardBinding
import com.example.acefx_app.ui.AccountFragment
import com.example.acefx_app.ui.HomeFragment
import com.example.acefx_app.ui.ProjectsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.MaterialColors

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dynamic status bar color
        window.statusBarColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary, 0)

        // Load default fragment
        loadFragment(HomeFragment())

        // Set item selection listener
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_projects -> loadFragment(ProjectsFragment())
                R.id.nav_account -> loadFragment(AccountFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
