package com.example.acefx_app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.R
import com.example.acefx_app.data.ProjectData
import com.example.acefx_app.databinding.FragmentClientProjectDetailsBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientProjectDetailsFragment : Fragment() {

    private var _binding: FragmentClientProjectDetailsBinding? = null
    private val binding get() = _binding

    private var projectId: String? = null
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectId = arguments?.getString("projectId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientProjectDetailsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        projectId?.let { fetchProjectDetails(it) }

        // Navigate to Add Project screen
        binding?.addProjectButton?.setOnClickListener {
            val action = ClientProjectDetailsFragmentDirections
                .actionClientProjectDetailsFragmentToClientAddProjectFragment()
            findNavController().navigate(action)
        }

        // Pay Now button click → navigate to Payment screen
        binding?.payUpfrontButton?.setOnClickListener {
            val projectTitle = binding?.projectTitleText?.text.toString()
            val amount = binding?.projectAmountText?.text.toString().replace("₹", "").trim()
            val action = ClientProjectDetailsFragmentDirections
                .actionClientProjectDetailsFragmentToPaymentFragment(
                    projectId = projectId ?: "",
                    amount = amount.toFloat(),
                    projectName = projectTitle
                )
            findNavController().navigate(action)
        }
    }

    /** Fetch project details from API */
    private fun fetchProjectDetails(id: String) {
        showLoading(true)

        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val token = sharedPref.getString("authToken", "") ?: ""

        if (token.isEmpty()) {
            showLoading(false)
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        apiService.getProjectById("Bearer $token", id).enqueue(object : Callback<ProjectData> {
            override fun onResponse(call: Call<ProjectData>, response: Response<ProjectData>) {
                showLoading(false)
                if (!isAdded) return

                if (response.isSuccessful && response.body() != null) {
                    displayProjectDetails(response.body()!!)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Project not found or failed to fetch!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ProjectData>, t: Throwable) {
                showLoading(false)
                if (!isAdded) return
                Log.e("PROJECT_DETAILS", "Error fetching project: ${t.localizedMessage}")
                Toast.makeText(requireContext(), "Network error!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** Display project data in UI */
    private fun displayProjectDetails(project: ProjectData) {
        with(binding) {
            this?.projectTitleText?.text = project.title
            this?.projectDescriptionText?.text = project.description
            this?.projectDeadlineText?.text = "Deadline: ${project.deadline ?: "N/A"}"
            this?.projectAmountText?.text = "₹${project.expectedAmount ?: 0}"

            // Status color badge
            this?.projectStatusText?.text = project.status
            val statusBg = this?.projectStatusText?.background?.mutate()
            val color = when (project.status?.lowercase()) {
                "approved" -> R.color.green_400
                "on hold" -> R.color.orange_200
                else -> R.color.gray
            }
            statusBg?.setTint(ContextCompat.getColor(requireContext(), color))
            this?.projectStatusText?.background = statusBg

            // Links
            this?.projectDataLink?.setOnClickListener { openUrl(project.dataLink) }
            this?.projectAttachLink?.setOnClickListener { openUrl(project.attachLink) }

            // Pay button only if unpaid invoice
            this?.payUpfrontButton?.visibility =
                if (project.invoiceId?.paid == false) View.VISIBLE else View.GONE
        }
    }

    /** Open URL safely */
    private fun openUrl(url: String?) {
        if (url.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Link not available", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Cannot open link", Toast.LENGTH_SHORT).show()
        }
    }

    /** Fade-in/out loading overlay */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding?.loadingOverlay?.fadeIn()
            binding?.progressBar?.fadeIn()
        } else {
            binding?.progressBar?.fadeOut()
            binding?.loadingOverlay?.fadeOut()
        }
    }

    private fun View.fadeIn(duration: Long = 300) {
        alpha = 0f
        visibility = View.VISIBLE
        animate().alpha(1f).setDuration(duration).start()
    }

    private fun View.fadeOut(duration: Long = 300, endVisibility: Int = View.GONE) {
        animate().alpha(0f).setDuration(duration).withEndAction {
            visibility = endVisibility
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
