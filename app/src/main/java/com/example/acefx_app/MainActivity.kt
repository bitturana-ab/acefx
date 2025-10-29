package com.example.acefx_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.annotation.ContentView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var otpInput: EditText
    private lateinit var sendOtpBtn: Button
    private lateinit var verifyOtpBtn: Button
    private lateinit var goToHomeBtn: Button
    private lateinit var resendTimer: TextView
    private lateinit var progressOverlay: FrameLayout
    private lateinit var progressBar: ProgressBar

    private val apiService: ApiService by lazy { ApiClient.getClient(this).create(ApiService::class.java) }
    private val prefs by lazy { getSharedPreferences("UserSession", MODE_PRIVATE) }
    private var resendCountDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto-login check
        val token = prefs.getString("authToken", null)
        if (!token.isNullOrEmpty()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        initializeViews()
        setupButtons()
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.emailInput)
        otpInput = findViewById(R.id.otpInput)
        sendOtpBtn = findViewById(R.id.sendOtpBtn)
        verifyOtpBtn = findViewById(R.id.verifyOtpBtn)
        goToHomeBtn = findViewById(R.id.goToHomeBtn)
        resendTimer = findViewById(R.id.resendTimer)
        progressOverlay = findViewById(R.id.progressOverlay)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupButtons() {

        sendOtpBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) {
                val snackbar = Snackbar.make(
                    findViewById(android.R.id.content), // ✅ root view of your Activity
                    "Please enter your email",
                    Snackbar.LENGTH_LONG
                )

                snackbar.view.backgroundTintList =
                    ContextCompat.getColorStateList(this@MainActivity, R.color.black)

                snackbar.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.white))
                snackbar.setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.teal_200))

                snackbar.setAction("OK") {
                    snackbar.dismiss()
                }

                snackbar.show()
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            animateFadeIn(progressOverlay)
            sendOtp(email)
        }

        verifyOtpBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val otp = otpInput.text.toString().trim()
            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            animateFadeIn(progressOverlay)
            verifyOtp(email, otp)
        }

        goToHomeBtn.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }

    private fun sendOtp(email: String) {
        sendOtpBtn.isEnabled = false
        sendOtpBtn.text = "Sending..."

        val request = mapOf("email" to email)
        apiService.sendOtp(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                animateFadeOut(progressOverlay)
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "OTP sent to email", Toast.LENGTH_LONG).show()
                    emailInput.isEnabled = false
                    fadeInView(otpInput)
                    fadeInView(verifyOtpBtn)

                    sendOtpBtn.text = "Resend OTP"
                    startResendTimer()
                } else {
                    sendOtpBtn.isEnabled = true
                    sendOtpBtn.text = "Send OTP"
                    Toast.makeText(this@MainActivity, "Failed to send OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                animateFadeOut(progressOverlay)
                sendOtpBtn.isEnabled = true
                sendOtpBtn.text = "Send OTP"
                Toast.makeText(this@MainActivity, "Network error: Check Internet connection", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun verifyOtp(email: String, otp: String) {
        verifyOtpBtn.isEnabled = false
        verifyOtpBtn.text = "Verifying..."

        val request = mapOf("email" to email, "otp" to otp)
        apiService.verifyOtp(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                animateFadeOut(progressOverlay)
                verifyOtpBtn.isEnabled = true
                verifyOtpBtn.text = "Verify OTP"

                if (response.isSuccessful) {
                    val data = response.body()
                    val token = data?.get("token").toString()

                    val userMap = data?.get("user") as? Map<*, *>
                    val userId = userMap?.get("_id")?.toString() ?: ""

                    prefs.edit {
                        putString("authToken", token)
                        putString("userId", userId)
                        apply()
                    }
                    saveUserSession(userId)

                    Toast.makeText(this@MainActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Invalid OTP!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                animateFadeOut(progressOverlay)
                verifyOtpBtn.isEnabled = true
                verifyOtpBtn.text = "Verify OTP"
                Toast.makeText(this@MainActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserSession(userId: String) {
        prefs.edit {
            putBoolean("isLoggedIn", true)
            putString("userId", userId)
            apply()
        }
    }

    private fun startResendTimer() {
        sendOtpBtn.isEnabled = false
        resendTimer.visibility = View.VISIBLE

        resendCountDownTimer?.cancel()
        resendCountDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                resendTimer.text = "Resend in ${seconds}s"

                // Fade in/out animation for the timer text
                val fade = AlphaAnimation(0.3f, 1f)
                fade.duration = 500
                fade.repeatMode = AlphaAnimation.REVERSE
                fade.repeatCount = 1
                resendTimer.startAnimation(fade)
            }

            override fun onFinish() {
                resendTimer.visibility = View.GONE
                sendOtpBtn.isEnabled = true
            }
        }.start()
    }


    /** View fade-in animation **/
    private fun fadeInView(view: View, duration: Long = 300) {
        view.visibility = View.VISIBLE
        val anim = AlphaAnimation(0f, 1f)
        anim.duration = duration
        view.startAnimation(anim)
    }

    /** Fade-in overlay **/
    private fun animateFadeIn(view: View, duration: Long = 300) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        view.animate().alpha(1f).setDuration(duration).start()
    }

    /** Fade-out overlay **/
    private fun animateFadeOut(view: View, duration: Long = 300) {
        view.animate().alpha(0f).setDuration(duration).withEndAction {
            view.visibility = View.GONE
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendCountDownTimer?.cancel()
    }
}
