package com.example.acefx_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignUpActivity : AppCompatActivity() {
    private lateinit var emailSetText : TextView
    private lateinit var backToMain: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        emailSetText = findViewById<TextView>(R.id.signupAppName)
//        access intent extra data for singup
        val data = intent.extras

        data?.let {
            val email = data.getString(Constants.INTENT_EMAIL_KEY)
            emailSetText.text = email
        }



//        again back to main screen
        backToMain = findViewById<Button>(R.id.backToHomeBtn)
        backToMain.setOnClickListener {
            Toast.makeText(this,"Home screen", Toast.LENGTH_SHORT).show()
            Intent(this@SignUpActivity, MainActivity::class.java).also {
                startActivity(it)
            }
        }

    }
}