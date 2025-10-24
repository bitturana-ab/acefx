package com.example.acefx_app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ClientProfileActivity : AppCompatActivity() {
    private lateinit var companyNameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var pinCodeInput: EditText
    private lateinit var nextBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_profile)

        companyNameInput = findViewById(R.id.companyNameInput)
        phoneInput = findViewById(R.id.phoneInput)
        pinCodeInput = findViewById(R.id.pinCodeInput)
        nextBtn = findViewById(R.id.nextBtn)

        nextBtn.setOnClickListener {
            val name = companyNameInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val pin = pinCodeInput.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Next Step → Profile Saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
