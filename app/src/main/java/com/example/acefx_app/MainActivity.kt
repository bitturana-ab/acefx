package com.example.acefx_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.acefx_app.retrofitServices.ApiService
import com.example.acefx_app.retrofitServices.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var otpInput: EditText
    private lateinit var sendOtpBtn: Button
    private lateinit var verifyOtpBtn: Button
    private lateinit var goToHomeBtn: Button
    private lateinit var roleSwitch: SwitchCompat
    private lateinit var roleLabelLeft: TextView
    private lateinit var roleLabelRight: TextView

    private var selectedRole = "Employee"
    private val apiService = ApiClient.getClient().create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        emailInput = findViewById(R.id.emailInput)
        otpInput = findViewById(R.id.otpInput)
        sendOtpBtn = findViewById(R.id.sendOtpBtn)
        verifyOtpBtn = findViewById(R.id.verifyOtpBtn)
        goToHomeBtn = findViewById(R.id.goToHomeBtn)
        roleSwitch = findViewById(R.id.roleSwitch)
        roleLabelLeft = findViewById(R.id.roleLabelLeft)
        roleLabelRight = findViewById(R.id.roleLabelRight)

        // Set role switch listener
        roleSwitch.setOnCheckedChangeListener { _, isChecked ->
            selectedRole = if (isChecked) "Client" else "Employee"
            Toast.makeText(this, "Selected: $selectedRole", Toast.LENGTH_SHORT).show()
        }

        // Send OTP button
        sendOtpBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendOtp(email, selectedRole)
        }

        // Verify OTP button
        verifyOtpBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val otp = otpInput.text.toString().trim()
            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyOtp(email, otp)
        }

        // Go to Home button
        goToHomeBtn.setOnClickListener {
            Toast.makeText(this, "Navigating to Dashboard", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
        }
    }

    // Function to send OTP
    private fun sendOtp(email: String, role: String) {
        sendOtpBtn.isEnabled = false
        sendOtpBtn.text = "Sending..."

        val request = mapOf("email" to email, "role" to role)
        apiService.sendOtp(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                sendOtpBtn.isEnabled = true
                sendOtpBtn.text = "Resend OTP"

                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "OTP sent to $email", Toast.LENGTH_LONG).show()
                    emailInput.isEnabled = false
                    otpInput.visibility = View.VISIBLE
                    verifyOtpBtn.visibility = View.VISIBLE
                    roleSwitch.isEnabled = false
                } else {
                    Toast.makeText(this@MainActivity, "Failed to send OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                sendOtpBtn.isEnabled = true
                sendOtpBtn.text = "Send OTP"
                Toast.makeText(this@MainActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to verify OTP
    private fun verifyOtp(email: String, otp: String) {
        verifyOtpBtn.isEnabled = false
        verifyOtpBtn.text = "Verifying..."

        val request = mapOf("email" to email, "otp" to otp)
        apiService.verifyOtp(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                verifyOtpBtn.isEnabled = true
                verifyOtpBtn.text = "Verify OTP"

                if (response.isSuccessful) {
                    val data = response.body()
                    val token = data?.get("token") ?: ""
                    Toast.makeText(this@MainActivity, "Login Successful!", Toast.LENGTH_SHORT).show()

                    // Save token to SharedPreferences
                    getSharedPreferences("AceFXPrefs", MODE_PRIVATE)
                        .edit {
                            putString("authToken", token.toString())
                        }
                    try {
                        startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                    }catch (exce: Exception){}

                    goToHomeBtn.visibility = View.VISIBLE
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

}
