package com.example.acefx_app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.R
import com.example.acefx_app.data.ProjectData
import com.example.acefx_app.data.ProjectsResponse
import com.example.acefx_app.data.UserDetailsResponse
import com.example.acefx_app.databinding.FragmentHomeBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import retrofit2.*
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding

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
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        api = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        loadLocalUserData()

        binding?.let { bind ->
            if (!name.isNullOrEmpty()) bind.tvUserName.text = name

            bind.btnChatNow.setOnClickListener { handleHomeNavigation() }
            bind.addProjectBtn.setOnClickListener {
                if (isProfileComplete()) navigateToAddProject() else navigateToProfile()
            }
            bind.btnPayBill.setOnClickListener { navigateToProjects() }

            token?.let { fetchClientProjects(it) }

            // Swipe refresh
            bind.swipeRefreshLayout.setOnRefreshListener {
                token?.let {
                    fetchClientProjects(it)
                } ?: run {
                    Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show()
                    bind.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    /** Fetch client projects **/
    private fun fetchClientProjects(token: String) {
        if (!isAdded) return
        showLoading(true)
        binding?.swipeRefreshLayout?.isRefreshing = true

        api.getClientProjects("Bearer $token").enqueue(object : Callback<ProjectsResponse> {
            override fun onResponse(
                call: Call<ProjectsResponse>, response: Response<ProjectsResponse>
            ) {
                if (!isAdded) return
                showLoading(false)
                binding?.swipeRefreshLayout?.isRefreshing = false

                val body = response.body()
                if (response.isSuccessful && body != null) {
                    unpaidTotal = 0.0
                    completedProjects.clear()

                    for (project in body.data.orEmpty()) {
                        val payment = project.paymentId
                        val paymentStatus = payment?.status ?: "unpaid"
                        val paidType = payment?.paidType ?: "none"
                        val totalAmount = project.actualAmount?.takeIf { it > 0 }
                            ?: project.expectedAmount ?: 0.0

                        unpaidTotal += when {
                            paymentStatus == "success" -> 0.0
                            paidType.equals("half", true) -> totalAmount / 2
                            else -> totalAmount
                        }

                        if (project.status.equals("success", true)
                            || paymentStatus.equals("success", true)
                        ) completedProjects.add(project)
                    }

                    updateBillView()
                    displayCompletedProjects()
                } else {
                    Toast.makeText(requireContext(), "No projects found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProjectsResponse>, t: Throwable) {
                if (!isAdded) return
                showLoading(false)
                binding?.swipeRefreshLayout?.isRefreshing = false
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** Update unpaid total **/
    private fun updateBillView() {
        binding?.totalBillView?.text = "Total Due: ₹${String.format("%.2f", unpaidTotal)}"
    }

    /** Show completed projects **/
    private fun displayCompletedProjects() {
        val container = binding?.projectListContainer ?: return
        container.removeAllViews()

        if (completedProjects.isEmpty()) {
            Toast.makeText(requireContext(), "No completed projects found", Toast.LENGTH_SHORT).show()
            return
        }

        val inflater = LayoutInflater.from(requireContext())
        for (project in completedProjects) {
            val card = inflater.inflate(R.layout.item_project_card, container, false)
            val nameText = card.findViewById<TextView>(R.id.projectName)
            val dateText = card.findViewById<TextView>(R.id.projectDate)
            val downloadBtn = card.findViewById<Button>(R.id.downloadBtn)
            val daysLeftText = card.findViewById<TextView>(R.id.daysLeft)

            nameText.text = project.title ?: "Untitled Project"
            dateText.text = formatDateTime(project.deadline ?: project.completedTime)
            daysLeftText.text = calculateDaysLeft(project.deadline)

            val isPaid = project.paymentId?.status.equals("success", true)
            downloadBtn.isEnabled = isPaid
            downloadBtn.alpha = if (isPaid) 1f else 0.6f

            card.setOnClickListener {
                project._id?.let { id -> navigateToProjectDetails(id) }
                    ?: Toast.makeText(requireContext(), "Invalid project", Toast.LENGTH_SHORT).show()
            }

            downloadBtn.setOnClickListener {
                if (isPaid) openUrl(project.deliverableUrl)
                else Toast.makeText(requireContext(), "Pay to download deliverables", Toast.LENGTH_SHORT).show()
            }

            container.addView(card)
        }
    }

    private fun calculateDaysLeft(deadline: String?): String {
        if (deadline.isNullOrEmpty()) return "-"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val targetDate = sdf.parse(deadline)
            val today = Date()
            val diff = ((targetDate?.time ?: 0) - today.time) / (1000 * 60 * 60 * 24)
            if (diff >= 0) diff.toString() else "0"
        } catch (e: Exception) {
            "-"
        }
    }

    private fun navigateToProjectDetails(projectId: String?) {
        if (projectId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invalid project", Toast.LENGTH_SHORT).show()
            return
        }
        val bundle = Bundle().apply { putString("projectId", projectId) }
        if (isAdded) findNavController().navigate(R.id.clientProjectDetailsFragment, bundle)
    }

    private fun openUrl(url: String?) {
        if (url.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No file available", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
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
        sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        name = sharedPref.getString("name", null)
        companyName = sharedPref.getString("companyName", null)
        phoneNumber = sharedPref.getString("phoneNumber", null)
        pinCode = sharedPref.getString("pinCode", null)
        token = sharedPref.getString("authToken", null)
    }

    private fun isProfileComplete(): Boolean {
        return !companyName.isNullOrEmpty() &&
                !phoneNumber.isNullOrEmpty() &&
                !pinCode.isNullOrEmpty()
    }

    private fun handleHomeNavigation() {
        if (!isAdded) return
        when {
            isProfileComplete() -> {
                Toast.makeText(requireContext(), "Welcome back, $companyName!", Toast.LENGTH_SHORT).show()
                navigateToChat()
            }
            !token.isNullOrEmpty() -> fetchUpdatedProfile()
            else -> navigateToProfile()
        }
    }

    private fun fetchUpdatedProfile() {
        if (!isAdded) return
        showLoading(true)
        api.getUserProfile("Bearer $token").enqueue(object : Callback<UserDetailsResponse> {
            override fun onResponse(
                call: Call<UserDetailsResponse>, response: Response<UserDetailsResponse>
            ) {
                if (!isAdded) return
                showLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        saveUserDataLocally(it)
                        if (isProfileComplete()) navigateToProjects() else navigateToProfile()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserDetailsResponse>, t: Throwable) {
                if (!isAdded) return
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

    private fun navigateToProjects() {
        if (isAdded) findNavController().navigate(R.id.clientProjectsFragment)
    }

    private fun navigateToAddProject() {
        if (isAdded) findNavController().navigate(R.id.clientAddProjectFragment)
    }

    private fun navigateToChat() {
        if (isAdded) findNavController().navigate(R.id.chatFragment)
    }

    private fun navigateToProfile() {
        if (isAdded) findNavController().navigate(R.id.clientProfileFragment)
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.btnChatNow?.isEnabled = !isLoading
        binding?.btnPayBill?.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
