package com.example.acefx_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var roleText: TextView
    private lateinit var projectList: RecyclerView
    private lateinit var logoutBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    private val apiService = ApiClient.getClient().create(ApiService::class.java)
    private val projects = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize views
        welcomeText = findViewById(R.id.welcomeText)
        roleText = findViewById(R.id.roleText)
        projectList = findViewById(R.id.projectRecycler)
        logoutBtn = findViewById(R.id.logoutBtn)
        progressBar = findViewById(R.id.progressBar)
        emptyText = findViewById(R.id.emptyText)

        // Setup recycler
        projectList.layoutManager = LinearLayoutManager(this)
        projectList.adapter = ProjectAdapter(projects)

        // Get token
        val sharedPrefs = getSharedPreferences("AceFXPrefs", MODE_PRIVATE)
        val token = sharedPrefs.getString("authToken", null)

        if (token == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            loadUserDashboard(token)
        }

        logoutBtn.setOnClickListener {
            sharedPrefs.edit().remove("authToken").apply()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun loadUserDashboard(token: String) {
        progressBar.visibility = View.VISIBLE

        apiService.getDashboard("Bearer $token").enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val data = response.body()
                    val user = data?.get("user") as? Map<*, *>
                    val projectData = data?.get("projects") as? List<Map<String, Any>>

                    welcomeText.text = "Welcome, ${user?.get("name") ?: "User"}"
                    roleText.text = "Role: ${user?.get("role") ?: "Unknown"}"

                    if (projectData.isNullOrEmpty()) {
                        emptyText.visibility = View.VISIBLE
                        projectList.visibility = View.GONE
                    } else {
                        projects.clear()
                        projects.addAll(projectData)
                        projectList.adapter?.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(this@DashboardActivity, "Failed to load dashboard", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@DashboardActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
