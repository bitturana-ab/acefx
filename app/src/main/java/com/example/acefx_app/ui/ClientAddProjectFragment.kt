package com.example.acefx_app.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.R
import com.example.acefx_app.data.ProjectRequest
import com.example.acefx_app.data.ProjectResponse
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class ClientAddProjectFragment : Fragment() {

    private lateinit var apiService: ApiService
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_client_add_project, container, false)

        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        sharedPrefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("authToken", null)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etDataLink = view.findViewById<EditText>(R.id.etDataLink)
        val etAttachLink = view.findViewById<EditText>(R.id.etAttachLink)
        val etDeadline = view.findViewById<EditText>(R.id.etDeadline)
        val etExpectedAmount = view.findViewById<EditText>(R.id.etExpectedAmount)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmitProject)

        // Date Picker
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

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val dataLink = etDataLink.text.toString().trim()
            val attachLink = etAttachLink.text.toString().trim()
            val deadline = etDeadline.text.toString().trim()
            val expectedAmountStr = etExpectedAmount.text.toString().trim()

            if (title.isEmpty() || description.isEmpty() || deadline.isEmpty() || expectedAmountStr.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "All required fields must be filled",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val expectedAmount = try {
                expectedAmountStr.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Enter valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val projectRequest = ProjectRequest(
                title = title,
                description = description,
                dataLink = dataLink,
                attachLink = attachLink,
                deadline = deadline,
                expectedAmount = expectedAmount
            )

            apiService.createProject("Bearer $token", projectRequest)
                .enqueue(object : Callback<ProjectResponse> {
                    override fun onResponse(
                        call: Call<ProjectResponse>,
                        response: Response<ProjectResponse>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Project submitted for approval",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        } else {
                            Log.d("ADD_PRODUCT",response.toString())
                            Toast.makeText(
                                requireContext(),
                                "Submission failed: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ProjectResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                })

        }

        return view
    }
}
