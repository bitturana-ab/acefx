package com.example.acefx_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    private lateinit var nextBtn : Button
    private lateinit var submitBtn : Button

    private lateinit var backToHome: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onCreate()")
        setContentView(R.layout.activity_main)

//        submit btn handle
        submitBtn = findViewById<Button>(R.id.submitBtn)
        submitBtn.setOnClickListener {
            // access email for db call later
            var textEmail = findViewById<EditText>(R.id.emailInput).text.toString()

            Toast.makeText(this,"OTP sent to your $textEmail email!", Toast.LENGTH_SHORT).show()

        }

//        navigate next signup page also will back to main
        nextBtn = findViewById<Button>(R.id.nextBtn)
        nextBtn.setOnClickListener{
            Toast.makeText(this,"Navigating to sign page now", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, SignUpActivity::class.java)
            startActivity(intent)
        }


    }

    override fun onResume() {
        super.onResume()
        println("onResume()")
    }

    override fun onStart() {
        super.onStart()
        println("onStart()")
    }

    override fun onPause() {
        super.onPause()
        println("onPause()")
    }

    override fun onStop() {
        super.onStop()
        println("onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy()")
    }


}