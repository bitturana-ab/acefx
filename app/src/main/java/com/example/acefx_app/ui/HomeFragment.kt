package com.example.acefx_app.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.R
import com.example.acefx_app.data.ProjectData
import com.example.acefx_app.data.ProjectsResponse
import com.example.acefx_app.data.UserDetailsResponse
import com.example.acefx_app.databinding.FragmentHomeBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var api: ApiService
    private lateinit var sharedPref: android.content.SharedPreferences

    private var companyName: String? = null
    private var name: String? = null
    private var phoneNumber: String? = null
    private var pinCode: String? = null
    private var token: String? = null

    private var unpaidTotal = 0.0
    private val completedProjects = mutableListOf<ProjectData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        api = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        loadLocalUserData()

        if (!name.isNullOrEmpty()) binding.tvUserName.text = name

        binding.btnChatNow.setOnClickListener { handleHomeNavigation() }
        binding.addProjectBtn.setOnClickListener {
            if (isProfileComplete()) navigateToAddProject() else navigateToProfile()
        }
        binding.btnPayBill.setOnClickListener { navigateToProjects() }

        token?.let { fetchClientProjects(it) }
    }

    /** Fetch all projects of this client **/
    private fun fetchClientProjects(token: String) {
        showLoading(true)
        api.getClientProjects("Bearer $token").enqueue(object : Callback<ProjectsResponse> {
            override fun onResponse(
                call: Call<ProjectsResponse>, response: Response<ProjectsResponse>
            ) {
                showLoading(false)
                if (!isAdded) return

                if (response.isSuccessful && response.body() != null) {
                    val projects = response.body()!!.data
                    unpaidTotal = 0.0
                    completedProjects.clear()

                    for (project in projects) {
                        val payment = project.paymentId
                        val paymentStatus = payment?.status ?: "unpaid"
                        val paidType = payment?.paidType ?: "none"

                        // Safely get the main amount
                        val totalAmount =
                            project.actualAmount?.takeIf { it > 0 } ?: project.expectedAmount ?: 0.0

                        // Calculate unpaid amount
                        unpaidTotal += when {
                            paymentStatus == "success" -> 0.0                // fully paid
                            paidType.equals(
                                "half",
                                true
                            ) -> totalAmount.div(2) // half-paid -> remaining half
                            else -> totalAmount                              // unpaid -> full amount due
                        }

                        // Completed projects list
                        if (project.status.equals(
                                "success",
                                true
                            ) || paymentStatus.equals("success", true)
                        ) {
                            completedProjects.add(project)
                        }
                    }


                    updateBillView()
                    displayCompletedProjects()
                } else {
                    Toast.makeText(requireContext(), "No projects found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProjectsResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** Update total unpaid amount **/
    private fun updateBillView() {
        binding.totalBillView.text = "Total Due: ₹${String.format("%.2f", unpaidTotal)}"
    }

    /** Display last two completed projects **/
    private fun displayCompletedProjects() {
        val container = listOf(
            binding.projectCard1 to completedProjects.getOrNull(0),
            binding.projectCard2 to completedProjects.getOrNull(1)
        )

        for ((card, project) in container) {
            if (project == null) {
                card.visibility = View.GONE
                continue
            }

            card.visibility = View.VISIBLE
            val layout = card.getChildAt(0) as ViewGroup
            val nameText = layout.findViewById<android.widget.TextView>(R.id.projectName)
            val dateText = layout.findViewById<android.widget.TextView>(R.id.projectDate)
            val downloadBtn = layout.findViewById<android.widget.Button>(R.id.downloadBtn)

            nameText.text = project.title
            dateText.text = formatDateTime(project.deadline ?: project.completedTime)

            val isPaid = project.paymentId?.status == "success"
            downloadBtn.isEnabled = isPaid
            downloadBtn.alpha = if (isPaid) 1f else 0.6f

            // Click listener for card to open project details
            card.setOnClickListener {
                navigateToProjectDetails(project._id)
            }

            // Click listener for download button
            downloadBtn.setOnClickListener {
                if (isPaid) {
                    openUrl(project.deliverableUrl)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Pay to download deliverables",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }
    private fun navigateToProjectDetails(projectId: String?) {
        if (projectId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invalid project", Toast.LENGTH_SHORT).show()
            return
        }
        val bundle = Bundle().apply {
            putString("projectId", projectId)
        }
        findNavController().navigate(R.id.clientProjectDetailsFragment, bundle)
    }


    private fun openUrl(url: String?) {
        if (url.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No file available", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(url)
            )
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Cannot open file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatDateTime(iso: String?): String {
        if (iso.isNullOrEmpty()) return "N/A"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(iso)
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            formatter.format(date!!)
        } catch (e: Exception) {
            iso
        }
    }

    private fun loadLocalUserData() {
        name = sharedPref.getString("name", null)
        companyName = sharedPref.getString("companyName", null)
        phoneNumber = sharedPref.getString("phoneNumber", null)
        pinCode = sharedPref.getString("pinCode", null)
        token = sharedPref.getString("authToken", null)
    }

    private fun isProfileComplete(): Boolean {
        return !companyName.isNullOrEmpty() && !phoneNumber.isNullOrEmpty() && !pinCode.isNullOrEmpty()
    }

    private fun handleHomeNavigation() {
        if (isProfileComplete()) {
            Toast.makeText(requireContext(), "Welcome back, $companyName!", Toast.LENGTH_SHORT)
                .show()
            navigateToChat()
        } else if (!token.isNullOrEmpty()) fetchUpdatedProfile() else navigateToProfile()
    }

    private fun fetchUpdatedProfile() {
        showLoading(true)
        api.getUserProfile("Bearer $token").enqueue(object : Callback<UserDetailsResponse> {
            override fun onResponse(
                call: Call<UserDetailsResponse>, response: Response<UserDetailsResponse>
            ) {
                showLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        saveUserDataLocally(it)
                        if (isProfileComplete()) navigateToProjects() else navigateToProfile()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch profile", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<UserDetailsResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserDataLocally(data: UserDetailsResponse) {
        companyName = data.companyName
        phoneNumber = data.phoneNumber
        pinCode = data.pinCode
        sharedPref.edit().apply {
            putString("companyName", companyName)
            putString("phoneNumber", phoneNumber)
            putString("pinCode", pinCode)
            apply()
        }
    }

    private fun navigateToProjects() = findNavController().navigate(R.id.clientProjectsFragment)
    private fun navigateToAddProject() = findNavController().navigate(R.id.clientAddProjectFragment)
    private fun navigateToChat() = findNavController().navigate(R.id.chatFragment)
    private fun navigateToProfile() = findNavController().navigate(R.id.clientProfileFragment)

    private fun showLoading(isLoading: Boolean) {
        binding.btnChatNow.isEnabled = !isLoading
        binding.btnPayBill.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
