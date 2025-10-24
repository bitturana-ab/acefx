package com.example.acefx_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
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

    private val apiService = ApiClient.getClient().create(ApiService::class.java)
    private val prefs by lazy { getSharedPreferences("AceFXPrefs", MODE_PRIVATE) }
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

    /** Initialize all view components **/
    private fun initializeViews() {
        emailInput = findViewById(R.id.emailInput)
        otpInput = findViewById(R.id.otpInput)
        sendOtpBtn = findViewById(R.id.sendOtpBtn)
        verifyOtpBtn = findViewById(R.id.verifyOtpBtn)
        goToHomeBtn = findViewById(R.id.goToHomeBtn)
        resendTimer = findViewById(R.id.resendTimer)
    }

    /** Setup button click listeners **/
    private fun setupButtons() {

        sendOtpBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendOtp(email)
        }

        verifyOtpBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val otp = otpInput.text.toString().trim()
            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyOtp(email, otp)
        }

        goToHomeBtn.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }

    /** Send OTP to backend **/
    private fun sendOtp(email: String) {
        sendOtpBtn.isEnabled = false
        sendOtpBtn.text = "Sending..."

        val request = mapOf("email" to email)
        apiService.sendOtp(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                Log.d("OTP_DEBUG", "Response code: ${response.code()}")
                Log.d("OTP_DEBUG", "Response body: ${response.body()}")
                Log.d("OTP_DEBUG", "Error body: ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "OTP sent to email", Toast.LENGTH_LONG).show()
                    emailInput.isEnabled = false
                    otpInput.visibility = View.VISIBLE
                    verifyOtpBtn.visibility = View.VISIBLE

                    sendOtpBtn.text = "Resend OTP"
                    startResendTimer()
                } else {
                    sendOtpBtn.isEnabled = true
                    sendOtpBtn.text = "Send OTP"
                    Toast.makeText(this@MainActivity, "Failed to send OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                sendOtpBtn.isEnabled = true
                sendOtpBtn.text = "Send OTP"
                Toast.makeText(this@MainActivity, "Network error: Check Internet connection", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** Verify OTP with backend **/
    private fun verifyOtp(email: String, otp: String) {
        verifyOtpBtn.isEnabled = false
        verifyOtpBtn.text = "Verifying..."

        val request = mapOf("email" to email, "otp" to otp)
        apiService.verifyOtp(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                verifyOtpBtn.isEnabled = true
                verifyOtpBtn.text = "Verify OTP"

                if (response.isSuccessful) {
                    val data = response.body()
                    val token = data?.get("token") ?: ""
                    prefs.edit { putString("authToken", token.toString()) }
                    saveUserSession()

                    Toast.makeText(this@MainActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Invalid OTP!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                verifyOtpBtn.isEnabled = true
                verifyOtpBtn.text = "Verify OTP"
                Toast.makeText(this@MainActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** Save user session to SharedPreferences **/
    private fun saveUserSession() {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putBoolean("isLoggedIn", true)
            putString("userId", prefs.getString("userId", ""))
            apply()
        }
    }

    /** Start countdown timer for OTP resend **/
    private fun startResendTimer() {
        sendOtpBtn.isEnabled = false
        resendTimer.visibility = View.VISIBLE

        resendCountDownTimer?.cancel()
        resendCountDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendTimer.text = "Resend in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                resendTimer.visibility = View.GONE
                sendOtpBtn.isEnabled = true
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendCountDownTimer?.cancel()
    }
}
