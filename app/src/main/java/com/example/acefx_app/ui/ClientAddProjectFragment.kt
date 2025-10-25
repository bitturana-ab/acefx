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
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etAttachLink: EditText
    private lateinit var etDataLink: EditText
    private lateinit var etDeadline: EditText
    private lateinit var etExpectedAmount: EditText
    private lateinit var btnSubmit: Button
    private var token: String? = ""
    private var title: String = ""
    private var description: String = ""
    private var attachLink: String = ""
    private var dataLink: String = ""
    private var expectedAmountStr: String = ""
    private var deadline: String = ""
    private var expectedAmount: Double = 0.0

    private lateinit var apiService: ApiService
    private lateinit var sharedPrefs: SharedPreferences

    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_client_add_project, container, false)

        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        sharedPrefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        token = sharedPrefs.getString("authToken", null)

        etTitle = view.findViewById<EditText>(R.id.etTitle)
        etDescription = view.findViewById<EditText>(R.id.etDescription)
        etDataLink = view.findViewById<EditText>(R.id.etDataLink)
        etAttachLink = view.findViewById<EditText>(R.id.etAttachLink)
        etDeadline = view.findViewById<EditText>(R.id.etDeadline)
        etExpectedAmount = view.findViewById<EditText>(R.id.etExpectedAmount)
        btnSubmit = view.findViewById<Button>(R.id.btnSubmitProject)

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

title
        btnSubmit.setOnClickListener {
            title = etTitle.text.toString().trim()
            description = etDescription.text.toString().trim()
            dataLink = etDataLink.text.toString().trim()
            attachLink = etAttachLink.text.toString().trim()
            deadline = etDeadline.text.toString().trim()
            expectedAmountStr = etExpectedAmount.text.toString().trim()

            if (title.isEmpty() || description.isEmpty() || deadline.isEmpty() || expectedAmountStr.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "All required fields must be filled",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            expectedAmount = try {
                expectedAmountStr.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Enter valid amount", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Submission")
                .setMessage("Are you sure you want to add this project?\n\nTitle: $title\nDeadline: $deadline\nAmount: ₹$expectedAmountStr")
                .setPositiveButton("Yes") { _, _ ->
                    addProject()
                }
                .setNegativeButton("No", null)
                .show()
        }



        return view
    }

    fun addProject() {

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
                        Log.d("ADD_PRODUCT", response.toString())
                        Toast.makeText(
                            requireContext(),
                            "Submission failed: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ProjectResponse>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })


    }
}
