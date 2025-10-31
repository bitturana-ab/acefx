package com.example.acefx_app.ui

import android.annotation.SuppressLint
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.R
import com.example.acefx_app.data.ProjectDataByProject
import com.example.acefx_app.data.ProjectDetailResponse
import com.example.acefx_app.databinding.FragmentClientProjectDetailsBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
        // back button function
        binding?.backBtn?.setOnClickListener { findNavController().popBackStack() }

        // Pay Now button click → navigate to Payment screen
        binding?.payUpfrontButton?.setOnClickListener {
            val projectTitle = binding?.projectTitleText?.text.toString()
            val amount = binding?.projectAmountText?.text.toString().replace("₹", "").trim()
            val bundle = Bundle().apply {
                putString("projectId", projectId)
                putDouble("amount", amount.toDouble())
                putString("projectName", projectTitle)
            }

            findNavController().navigate(R.id.action_clientProjectDetailsFragment_to_paymentFragment, bundle)

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

        apiService.getProjectById("Bearer $token", id)
            .enqueue(object : Callback<ProjectDetailResponse> {
                override fun onResponse(
                    call: Call<ProjectDetailResponse>,
                    response: Response<ProjectDetailResponse>
                ) {
                    showLoading(false)
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
//                    Toast.makeText(requireContext(),"res ${response.toString()}", Toast.LENGTH_SHORT).show()
                        displayProjectDetails(response.body()?.data!!)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Project not found or failed to fetch!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ProjectDetailResponse>, t: Throwable) {
                    showLoading(false)
                    if (!isAdded) return
                    Log.d("PROJECT_DETAILS", "Error fetching project! ${t.toString()}")
                    Toast.makeText(
                        requireContext(),
                        "Check internet connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /** Display project data in UI */
    @SuppressLint("SetTextI18n")
    private fun displayProjectDetails(project: ProjectDataByProject) {
        with(binding) {
            this?.projectTitleText?.text = project.title
            this?.projectDescriptionText?.text = project.description
            this?.projectDeadlineText?.text =
                "Deadline: ${formatDateTime(project.deadline) ?: "N/A"}"
            this?.projectAmountText?.text = "₹${project.expectedAmount.toString() ?: 0}"

            // Status color badge
            this?.projectStatusText?.text = project.status
            val statusBg = this?.projectStatusText?.background?.mutate()
            val color = when (project.status.lowercase()) {
                "approved" -> R.color.status_active
                "on hold" -> R.color.orange_200
                else -> R.color.gray
            }
            statusBg?.setTint(ContextCompat.getColor(requireContext(), color))
            this?.projectStatusText?.background = statusBg

            // Links visibility setup
            this?.projectDataLink?.visibility =
                if (project.invoiceId?.paid == false) View.VISIBLE else View.GONE
            if (this?.projectDataLink?.isVisible == true)
                this.projectDataLink.setOnClickListener { openUrl(project.dataLink) }
            // also not able to download if not  paid

            this?.projectAttachLink?.visibility =
                if (project.invoiceId?.paid == true) View.VISIBLE else View.GONE
            if (this?.projectAttachLink?.isVisible == true)
                this.projectAttachLink.setOnClickListener { openUrl(project.deliverableUrl) }

            //set amount at least half or something will decide later
            this?.payUpfrontButton?.text = "Pay Upfront of ${project.expectedAmount.toString()}"
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

    // formate deadline date to readable
    private fun formatDateTime(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return ""

        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(isoDate)

            val formatter = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault())
            formatter.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            isoDate
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
