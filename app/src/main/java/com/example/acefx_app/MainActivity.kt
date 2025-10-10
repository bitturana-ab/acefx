package com.example.acefx_app

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var nextBtn : Button
    private lateinit var backToHome: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onCreate()")
        setContentView(R.layout.activity_main)

//        navigate next signup page
        nextBtn = findViewById<Button>(R.id.nextBtn)
        nextBtn.setOnClickListener(){
            Toast.makeText(this,"Navigating to sign page now", Toast.LENGTH_SHORT).show()
            setContentView(R.layout.home_activity)

            backToHome = findViewById<Button>(R.id.backToHomeBtn)
            backToHome.setOnClickListener() {
                Toast.makeText(this,"Navigating to home page now", Toast.LENGTH_SHORT).show()
                setContentView(R.layout.activity_main)
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