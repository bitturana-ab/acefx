package com.example.acefx_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val token = sharedPrefs.getString("authToken", null)

        if (token != null) {
            // User already logged in -> Dashboard
            startActivity(Intent(this, DashboardActivity::class.java))
        } else {
            // Not logged in -> MainActivity (login screen)
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish() // Prevent going back to SplashActivity
    }
}
