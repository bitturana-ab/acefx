package com.example.acefx_app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.R
import com.example.acefx_app.data.ProjectDataByForPaid
import com.example.acefx_app.data.ProjectDetailResponse
import com.example.acefx_app.databinding.FragmentClientProjectDetailsBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientProjectDetailsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.payUpfrontButton?.isEnabled = false
        projectId?.let { fetchProjectDetails(it) }

        binding?.apply {
            backBtn.setOnClickListener { findNavController().popBackStack() }

            addProjectButton.setOnClickListener {
                val action = ClientProjectDetailsFragmentDirections
                    .actionClientProjectDetailsFragmentToClientAddProjectFragment()
                findNavController().navigate(action)
            }


            payUpfrontButton.setOnClickListener {
                showLoading(true)
                shimmerLayout.visibility = View.VISIBLE
                shimmerLayout.startShimmer()
                payUpfrontButton.isEnabled = false

                lifecycleScope.launch {
                    delay(2000)
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    payUpfrontButton.isEnabled = true


                    val projectTitle = projectTitleText.text.toString()
                    val amount = projectAmountText.text.toString().replace("₹", "").trim()
                    val bundle = Bundle().apply {
                        putString("projectId", projectId)
                        putDouble("amount", amount.toDoubleOrNull() ?: 0.0)
                        putString("projectName", projectTitle)
                    }

                    findNavController().navigate(
                        R.id.action_clientProjectDetailsFragment_to_paymentFragment,
                        bundle
                    )
                }
            }
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
                        displayProjectDetails(response.body()!!.data)
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
                    Log.e("PROJECT_DETAILS", "Error fetching project: ${t.message}")
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
    private fun displayProjectDetails(project: ProjectDataByForPaid) {
        with(binding) {
            this?.apply {
                val amount = if (project.actualAmount != 0.0)
                    project.actualAmount
                else
                    project.expectedAmount

                // --- Project Details ---
                projectTitleText.text = project.title
                projectDescriptionText.text = project.description
                projectDeadlineText.text =
                    "Deadline: ${formatDateTime(project.deadline) ?: "N/A"}"



                projectAmountText.text = "₹${String.format("%.2f", amount ?: 0.0)}"

                // --- Status Display ---
                projectStatusText.text = project.status
                val statusBg = projectStatusText.background?.mutate()
                val color = when (project.status.lowercase()) {
                    "approved" -> R.color.status_active
                    "on hold" -> R.color.orange_200
                    else -> R.color.gray
                }
                statusBg?.setTint(ContextCompat.getColor(requireContext(), color))
                projectStatusText.background = statusBg

                // --- Payment Button Logic ---
                val status = project.paymentId?.status ?: "unpaid"
                val halfAmount = (amount ?: 0.0) / 2

                when (status) {
                    "created", "failed", "unpaid" -> {
                        payUpfrontButton.visibility = View.VISIBLE
                        payUpfrontButton.text =
                            "Pay Upfront ₹${String.format("%.2f", halfAmount)}"
                        payUpfrontButton.isEnabled = true
                    }

                    "half-paid" -> {
                        payUpfrontButton.visibility = View.VISIBLE
                        payUpfrontButton.text =
                            "Pay Remaining ₹${String.format("%.2f", halfAmount)}"
                        payUpfrontButton.isEnabled = true
                    }

                    "success", "full" -> payUpfrontButton.visibility = View.GONE

                    else -> {
                        payUpfrontButton.visibility = View.VISIBLE
                        payUpfrontButton.text =
                            "Pay ₹${String.format("%.2f", amount ?: 0.0)}"
                        payUpfrontButton.isEnabled = true
                    }
                }

                payUpfrontButton.alpha = if (payUpfrontButton.isEnabled) 1.0f else 0.6f

                // --- Data & Download Links ---
                projectDataLink.visibility = View.VISIBLE
                projectAttachLink.visibility = View.VISIBLE

                val isPaid = project.paymentId?.status == "success"
                projectDataLink.isEnabled = isPaid
                projectAttachLink.isEnabled = isPaid

                projectDataLink.alpha = if (isPaid) 1.0f else 0.5f
                projectAttachLink.alpha = if (isPaid) 1.0f else 0.5f

                if (isPaid) {
                    projectDataLink.setOnClickListener { openUrl(project.dataLink) }
                    projectAttachLink.setOnClickListener { openUrl(project.deliverableUrl) }
                } else {
                    projectDataLink.setOnClickListener {
                        Toast.makeText(
                            requireContext(),
                            "Pay to unlock data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    projectAttachLink.setOnClickListener {
                        Toast.makeText(
                            requireContext(),
                            "Pay to download file",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
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

    /** Loading overlay animation */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding?.loadingOverlay?.fadeIn()
            binding?.progressBar?.fadeIn()
        } else {
            binding?.progressBar?.fadeOut()
            binding?.loadingOverlay?.fadeOut()
        }
    }

    /** Format ISO date to readable format */
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

    /** Fade animations for smooth transitions */
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
