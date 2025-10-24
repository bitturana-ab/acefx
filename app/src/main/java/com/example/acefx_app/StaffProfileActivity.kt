package com.example.acefx_app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class StaffProfileActivity : AppCompatActivity() {
    private lateinit var fullNameInput: EditText
    private lateinit var staffPhoneInput: EditText
    private lateinit var idInput: EditText
    private lateinit var createProfileBtn: Button
    private lateinit var nearbyCheck: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_profile)

        fullNameInput = findViewById(R.id.fullNameInput)
        staffPhoneInput = findViewById(R.id.staffPhoneInput)
        idInput = findViewById(R.id.idInput)
        createProfileBtn = findViewById(R.id.createProfileBtn)
        nearbyCheck = findViewById(R.id.nearbyCheck)

        createProfileBtn.setOnClickListener {
            val name = fullNameInput.text.toString().trim()
            val phone = staffPhoneInput.text.toString().trim()
            val id = idInput.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || id.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                val nearby = nearbyCheck.isChecked
                Toast.makeText(
                    this,
                    "Profile Created! Nearby: $nearby",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
