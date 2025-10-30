package com.example.acefx_app.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.R
import com.example.acefx_app.data.ProjectRequest
import com.example.acefx_app.data.ProjectResponse
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ClientAddProjectFragment : Fragment() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etAttachLink: EditText
    private lateinit var etDataLink: EditText
    private lateinit var etDeadline: EditText
    private lateinit var etExpectedAmount: EditText
    private lateinit var btnSubmit: Button
    private lateinit var backBtn: ImageView
    private lateinit var loadingOverlay: FrameLayout

    private var token: String? = null
    private lateinit var apiService: ApiService
    private lateinit var sharedPrefs: SharedPreferences

    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_client_add_project, container, false)

        // Initialize components
        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        sharedPrefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        token = sharedPrefs.getString("authToken", null)

        etTitle = view.findViewById(R.id.etTitle)
        etDescription = view.findViewById(R.id.etDescription)
        etDataLink = view.findViewById(R.id.etDataLink)
        etAttachLink = view.findViewById(R.id.etAttachLink)
        etDeadline = view.findViewById(R.id.etDeadline)
        etExpectedAmount = view.findViewById(R.id.etExpectedAmount)
        btnSubmit = view.findViewById(R.id.btnSubmitProject)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
        backBtn = view.findViewById(R.id.backBtn)

        setupDatePicker()
        setupSubmitButton()
        backBtn.setOnClickListener { findNavController().popBackStack() }


        return view
    }

    /** Date Picker Setup **/
    private fun setupDatePicker() {
        etDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate =
                    String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                etDeadline.setText(formattedDate)
            }, year, month, day).show()
        }
    }

    /** Submit Button Setup **/
    private fun setupSubmitButton() {
        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val dataLink = etDataLink.text.toString().trim()
            val attachLink = etAttachLink.text.toString().trim()
            val deadline = etDeadline.text.toString().trim()
            val expectedAmountStr = etExpectedAmount.text.toString().trim()

            // Validation
            if (title.isEmpty() || description.isEmpty() || deadline.isEmpty() || expectedAmountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expectedAmount = try {
                expectedAmountStr.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (token.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Confirm before submission
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Submission")
                .setMessage(
                    """
                    Are you sure you want to add this project?
                    
                    • Title: $title
                    • Deadline: $deadline
                    • Amount: ₹$expectedAmountStr
                    """.trimIndent()
                )
                .setPositiveButton("Yes") { _, _ ->
                    addProject(title, description, dataLink, attachLink, deadline, expectedAmount)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    /** API Call **/
    private fun addProject(
        title: String,
        description: String,
        dataLink: String,
        attachLink: String,
        deadline: String,
        expectedAmount: Double
    ) {
        loadingOverlay.visibility = View.VISIBLE

        val projectRequest = ProjectRequest(
            title = title,
            description = description,
            dataLink = dataLink,
            attachLink = attachLink,
            deadline = deadline,
            expectedAmount = expectedAmount
        )

        try {
            apiService.createProject("Bearer $token", projectRequest)
                .enqueue(object : Callback<ProjectResponse> {
                    override fun onResponse(call: Call<ProjectResponse>, response: Response<ProjectResponse>) {
                        loadingOverlay.visibility = View.GONE

                        if (response.isSuccessful && response.body() != null) {
                            Toast.makeText(requireContext(), "Project submitted successfully!", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack() // back auto
                        } else {
                            val code = response.code()
                            val msg = response.message()
                            Log.e("ADD_PROJECT", "Error $code: $msg")
                            Toast.makeText(requireContext(), "Failed: $code $msg", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ProjectResponse>, t: Throwable) {
                        loadingOverlay.visibility = View.GONE
                        Log.e("ADD_PROJECT", "Failure: ${t.localizedMessage}")
                        Toast.makeText(requireContext(), "Network error!", Toast.LENGTH_LONG).show()
                    }
                })
        } catch (e: Exception) {
            loadingOverlay.visibility = View.GONE
            Log.e("ADD_PROJECT_EXCEPTION", "Exception: ${e.message}", e)
            Toast.makeText(requireContext(), "Unexpected error occurred", Toast.LENGTH_SHORT).show()
        }
    }
}
