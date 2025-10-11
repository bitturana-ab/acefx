package com.example.acefx_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    private lateinit var nextBtn : Button
    private lateinit var submitBtn : Button

    private lateinit var backToHomeBtn: Button

    private lateinit var textEmail: EditText
    private lateinit var optTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var optText: EditText



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onCreate()")
        setContentView(R.layout.activity_main)


        // access email for pass signup activity
        textEmail = findViewById<EditText>(R.id.emailInput)

//        submit btn handle
        submitBtn = findViewById<Button>(R.id.submitBtn)
        submitBtn.setOnClickListener {
            if(textEmail.text.isEmpty()){
                Toast.makeText(this,"Please enter email first", Toast.LENGTH_LONG).show()
            }else {
                Toast.makeText(
                    this@MainActivity,
                    "OTP sent to your ${textEmail.text.toString()} email!",
                    Toast.LENGTH_SHORT
                ).show()
                emailTextView = findViewById<TextView>(R.id.emailTextView)
                emailTextView.text = "Enter OTP: "
                textEmail.visibility = View.INVISIBLE
                optText = findViewById<EditText>(R.id.otpInput)
                optText.visibility = View.VISIBLE
                submitBtn.visibility = View.INVISIBLE
                nextBtn = findViewById<Button>(R.id.nextBtn)
                nextBtn.visibility = View.VISIBLE
//                back to home button make visible
                backToHomeBtn = findViewById<Button>(R.id.backToHomeBtn)
                backToHomeBtn.visibility = View.VISIBLE


            }
        }
// back to home after just enter email

        backToHomeBtn = findViewById<Button>(R.id.backToHomeBtn)
        backToHomeBtn.setOnClickListener {
            Toast.makeText(this,"Home screen", Toast.LENGTH_SHORT).show()
            Intent(this@MainActivity, MainActivity::class.java).also {
                startActivity(it)
            }
        }


//        navigate next signup page also will back to main
        nextBtn = findViewById<Button>(R.id.nextBtn)
        nextBtn.setOnClickListener{
            optText = findViewById<EditText>(R.id.otpInput)
            if(optText.text.isEmpty()){
                Toast.makeText(this,"Please enter otp here!", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Navigating to sign page now", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@MainActivity, SignUpActivity::class.java)
//              send email or any data from this to sign up screen
                intent.putExtra(Constants.INTENT_EMAIL_KEY, textEmail.text.toString())
                startActivity(intent)
            }

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